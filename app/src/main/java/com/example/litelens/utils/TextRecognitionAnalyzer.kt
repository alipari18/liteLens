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
    private val onTextRecognized: (String) -> Unit,
    private val textRecognitionManager: TextRecognitionManager,
    private val languageIdentificationManager: LanguageIdentificationManager,
    private val textTranslationManager: TextTranslationManager,
    private val context: Context,
    private val screenWidth: Int,
    private val screenHeight: Int
) : ImageAnalysis.Analyzer {

    // Define your bounding box as percentages of the screen dimensions (for cropping)
    private val boxWidthPercentage = 0.8f  // Example: 80% of the screen width
    private val boxHeightPercentage = 0.2f // Example: 20% of the screen height

    private var frameSkipCounter = 0

    @OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {

        frameSkipCounter++
        if(frameSkipCounter % 15 == 0){
            val rotationDegrees = imageProxy.imageInfo.rotationDegrees
            val bitmap = imageProxy.toBitmap()  // Utility function to convert ImageProxy to Bitmap

            // Crop the bitmap to only include the area inside the bounding box
            val rotatedBitmap = bitmap.rotateIfRequired(rotationDegrees)
            val croppedBitmap = cropBitmapToOverlay(
                rotatedBitmap,
                screenWidth,
                screenHeight
            )
            val preprocessedBitmap = preprocessBitmapForTextRecognition(croppedBitmap)

            // Perform text recognition on the cropped bitmap
            recognizeTextFromBitmap(preprocessedBitmap, rotationDegrees).addOnCompleteListener{
                imageProxy.close()
            }
        }
        imageProxy.close()


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

        // Create and return the cropped bitmap without scaling
        return Bitmap.createBitmap(
            sourceBitmap,
            safeStartX,
            safeStartY,
            cropBitmapWidth,
            cropBitmapHeight
        )
    }

    private fun recognizeTextFromBitmap(bitmap: Bitmap, rotationDegrees: Int): Task<Text> {
        return textRecognitionManager.recognizeTextInBitmap(
            bitmap,
            rotationDegrees = rotationDegrees,
            onSuccess = { detectedTextList ->
                // Process the list of detected text
                if(detectedTextList?.text != null){
                    Log.d("APP_LENS", "Detected Text: ${detectedTextList.text}")
                    if(detectedTextList.text.isNotEmpty()){
                        identifyLanguage(detectedTextList.text)
                    }else{
                        Log.d("APP_LENS", "No text detected")
                    }
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
            }
        )
    }

    private fun getErrorMessage(exception: Exception): String? {
        val mlKitException = exception as? MlKitException ?: return exception.message
        return if (mlKitException.errorCode == MlKitException.UNAVAILABLE) {
            "Waiting for text recognition model to be downloaded"
        } else exception.message
    }

    private fun Bitmap.rotateIfRequired(rotationDegrees: Int): Bitmap {
        if (rotationDegrees == 0) return this
        val matrix = Matrix().apply { postRotate(rotationDegrees.toFloat()) }
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