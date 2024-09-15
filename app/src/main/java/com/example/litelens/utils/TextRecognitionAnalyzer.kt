package com.example.litelens.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Rect
import android.util.Log
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.lifecycle.MutableLiveData
import com.example.litelens.domain.model.Detection
import com.example.litelens.domain.model.VisualSearchResult
import com.example.litelens.domain.repository.languageIdentification.LanguageIdentificationManager
import com.example.litelens.domain.repository.textRecognition.TextRecognitionManager
import com.example.litelens.domain.repository.textTranslation.TextTranslationManager
import com.google.android.gms.tasks.Task
import com.google.mlkit.common.MlKitException
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.lang.Exception
import kotlin.math.roundToInt

class TextRecognitionAnalyzer(
    private val onTextRecognized: (List<VisualSearchResult>) -> Unit,
    private val textRecognitionManager: TextRecognitionManager,
    private val languageIdentificationManager: LanguageIdentificationManager,
    private val textTranslationManager: TextTranslationManager,
    private val context: Context,
    private val screenWidth: Int,
    private val screenHeight: Int,
    private val shouldAnalyzeFrame: () -> Boolean,
    private val resetFrameAnalysis: () -> Unit,
    private var targetLanguage: String,
    private val onObjectDetectionResults: (List<Detection>) -> Unit
) : ImageAnalysis.Analyzer {

    // Define your bounding box as percentages of the screen dimensions (for cropping)
    private val boxWidthPercentage = 0.8f  // Example: 80% of the screen width
    private val boxHeightPercentage = 0.2f // Example: 20% of the screen height

    private var frameSkipCounter = 0

    @OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        if (!shouldAnalyzeFrame()) {
            imageProxy.close()
            return
        }

        val rotationDegrees = imageProxy.imageInfo.rotationDegrees
        val bitmap = imageProxy.toBitmap()

        val correctedBitmap = bitmap.rotateIfRequired(rotationDegrees)

        val croppedBitmap = cropBitmapToOverlay(correctedBitmap, screenWidth, screenHeight)

        val preprocessedBitmap = preprocessBitmapForTextRecognition(croppedBitmap)

        // Perform text recognition on the preprocessed bitmap
        recognizeTextFromBitmap(croppedBitmap, 0).addOnCompleteListener {
            resetFrameAnalysis()
            imageProxy.close()
        }
    }

    private fun cropBitmapToOverlay(
        sourceBitmap: Bitmap,
        screenWidth: Int,
        screenHeight: Int
    ): Bitmap {
        val bitmapWidth = sourceBitmap.width
        val bitmapHeight = sourceBitmap.height

        // Calculate the aspect ratio of the overlay box
        val overlayAspectRatio = (boxWidthPercentage * screenWidth) / (boxHeightPercentage * screenHeight)

        // Calculate the dimensions of the crop area in bitmap coordinates
        var cropBitmapWidth = (bitmapWidth * boxWidthPercentage).roundToInt()
        var cropBitmapHeight = (bitmapHeight * boxHeightPercentage).roundToInt()

        // Adjust the crop dimensions to match the overlay aspect ratio
        if (cropBitmapWidth / cropBitmapHeight > overlayAspectRatio) {
            // Too wide, adjust width
            cropBitmapWidth = (cropBitmapHeight * overlayAspectRatio).roundToInt()
        } else {
            // Too tall, adjust height
            cropBitmapHeight = (cropBitmapWidth / overlayAspectRatio).roundToInt()
        }

        // Calculate the starting point (top-left corner) of the crop area
        val startX = (bitmapWidth - cropBitmapWidth) / 2
        val startY = (bitmapHeight - cropBitmapHeight) / 2

        // Ensure the crop area is within the bounds of the source bitmap
        val safeStartX = startX.coerceIn(0, bitmapWidth - cropBitmapWidth)
        val safeStartY = startY.coerceIn(0, bitmapHeight - cropBitmapHeight)

        // Ensure the crop area is centered and maintains aspect ratio
        val cropRect = Rect(
            safeStartX,
            safeStartY,
            safeStartX + cropBitmapWidth,
            safeStartY + cropBitmapHeight
        )

        // Add some padding to the crop area
        val padding = (cropBitmapWidth * 0.1).toInt() // 10% padding
        cropRect.inset(-padding, -padding)

        // Ensure the cropRect is within the bounds of the source bitmap
        cropRect.intersect(0, 0, sourceBitmap.width, sourceBitmap.height)

        return Bitmap.createBitmap(
            sourceBitmap,
            cropRect.left,
            cropRect.top,
            cropRect.width(),
            cropRect.height()
        )
    }

    private fun recognizeTextFromBitmap(bitmap: Bitmap, rotationDegrees: Int): Task<Text> {
        return textRecognitionManager.recognizeTextInBitmap(
            bitmap,
            rotationDegrees = rotationDegrees,
            onSuccess = { detectedTextList ->
                // Process the list of detected text
                    Log.d("APP_LENS", "Detected Text: ${detectedTextList.detectedObjectName}")
                    if(detectedTextList.detectedObjectName.isNotEmpty()){
                        identifyLanguage(detectedTextList.detectedObjectName)
                        onObjectDetectionResults(listOf(detectedTextList))
                    }else{
                        Log.d("APP_LENS", "No text detected")
                        onTextRecognized(emptyList())
                    }

            },
            onError = { exception ->
                // Task failed with an exception
                Log.e("APP_LENS", "Text recognition error")
                val message = getErrorMessage(exception)
                message?.let {
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    // Function to identify language before translation
    private fun identifyLanguage(text: String) {
        languageIdentificationManager.identifyLanguage(
            text,
            onSuccess = { languageCode ->
                Log.d("APP_LENS", "Detected language: $languageCode")
                if (languageCode != "und") {
                    // Perform translation if the language is identified and not 'und'
                    translateDetectedText(text, languageCode)
                } else {
                    Log.d("APP_LENS", "Language could not be identified")
                    onTextRecognized(emptyList())
                }
            },
            onFailure = { exception ->
                // Task failed with an exception
                Log.e("APP_LENS", "Text recognition error")
                val message = getErrorMessage(exception)
                message?.let {
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    // Function to translate recognized text
    private fun translateDetectedText(text: String, sourceLanguage: String) {

        targetLanguage = if(targetLanguage == "English"){
            "en"
        }else{
            "it"
        }

        Log.d("APP_LENS", "Translating text to: $targetLanguage")

        textTranslationManager.translateText(
            text,
            sourceLanguage,
            onSuccess = { translatedText ->
                Log.d("APP_LENS", "Translated Text: $translatedText")
                onTextRecognized(translatedText) // Pass translated text to UI
            },
            onFailure = { exception ->
                Log.e("APP_LENS", "Translation error: ${exception.message}")
                Toast.makeText(context, "Translation failed", Toast.LENGTH_SHORT).show()
            },
            targetLanguage = targetLanguage
        )
    }

    private fun getErrorMessage(exception: Exception): String? {
        val mlKitException = exception as? MlKitException ?: return exception.message
        return if (mlKitException.errorCode == MlKitException.UNAVAILABLE) {
            "Waiting for text recognition model to be downloaded"
        } else exception.message
    }

    private fun Bitmap.rotateIfRequired(rotationDegrees: Int): Bitmap {
        if (rotationDegrees == 0) {
            // If no rotation is needed, we still need to flip the image vertically
            // This is often necessary due to the camera's natural orientation
            val matrix = Matrix().apply {
                postScale(1f, -1f, width / 2f, height / 2f)
            }
            return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
        }

        val matrix = Matrix().apply {
            postRotate(rotationDegrees.toFloat())
        }
        return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
    }
}


fun preprocessBitmapForTextRecognition(bitmap: Bitmap): Bitmap {
    val output = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(output)
    val paint = Paint()

    // Increase contrast
    val colorMatrix = ColorMatrix(floatArrayOf(
        2f, 0f, 0f, 0f, -25f,
        0f, 2f, 0f, 0f, -25f,
        0f, 0f, 2f, 0f, -25f,
        0f, 0f, 0f, 1f, 0f
    ))

    paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
    canvas.drawBitmap(bitmap, 0f, 0f, paint)

    return output
}