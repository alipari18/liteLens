package com.example.litelens.utils

import android.graphics.Bitmap
import android.graphics.Matrix
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.compose.runtime.State
import com.example.litelens.domain.model.Detection
import com.example.litelens.domain.repository.languageIdentification.LanguageIdentificationManager
import com.example.litelens.domain.repository.objectDetection.ObjectDetectionManager
import com.example.litelens.domain.repository.textRecognition.TextRecognitionManager
import com.example.litelens.domain.repository.textTranslation.TextTranslationManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class CameraFrameAnalyzer @Inject constructor(
    private val objectDetectionManager: ObjectDetectionManager,
    private val textRecognitionManager: TextRecognitionManager,
    private val languageIdentificationManager: LanguageIdentificationManager,
    private val translationManager: TextTranslationManager,
    private val onObjectDetectionResults: (List<Detection>) -> Unit,
    private val onTranslationResults: (String) -> Unit,
    private val confidenceScoreState: State<Float>
): ImageAnalysis.Analyzer {
    private var frameSkipCounter = 0

    private val textBuffer = StringBuilder()

    private val coroutineScope = CoroutineScope(Dispatchers.Default)

    override fun analyze(image: ImageProxy) {
        frameSkipCounter++
        // Analyze 1 frame every second
        if(frameSkipCounter % 60 != 0){
            image.close()
            return
        }

        // Rotate and create bitmap only if detection is to be performed
        val rotatedBitmap = image.toRotatedBitmap()

        coroutineScope.launch(Dispatchers.Main) {
            val detectionResults = processImage(rotatedBitmap, image.imageInfo.rotationDegrees)
            onObjectDetectionResults(detectionResults)
        }
        image.close()

    }

    private suspend fun processImage(rotatedBitmap: Bitmap, rotationDegrees: Int): List<Detection> {
        val detectionResults = mutableListOf<Detection>()

        Log.d("CameraFrameAnalyzer", "Processing image")

        val objectDetectionResults = withContext(Dispatchers.Main) {
            objectDetectionManager.detectObjectsInCurrentFrame(
                bitmap = rotatedBitmap,
                rotation = rotationDegrees,
                confidenceThreshold = confidenceScoreState.value
        )}

        Log.d("CameraFrameAnalyzer", "Detected objects: ${objectDetectionResults.size}")

        detectionResults.addAll(objectDetectionResults)

        val detectedTextList = recognizeText(rotatedBitmap)
        detectionResults.addAll(detectedTextList)

        return detectionResults
    }

    private suspend fun recognizeText(bitmap: Bitmap): List<Detection> {
        val detectedTextList = mutableListOf<Detection>()
        withContext(Dispatchers.Default) {
            textRecognitionManager.recognizeTextInBitmap(
                bitmap = bitmap,
                onSuccess = { textList ->
                    detectedTextList.addAll(textList)
                    val combinedText = textList.joinToString(" ") { it.detectedObjectName }
                    textBuffer.append(" ").append(combinedText)

                    processBufferedText()

                },
                onError = { error ->
                    Log.e("TextRecognition", error)
                }
            )
        }
        return detectedTextList
    }

    private fun ImageProxy.toRotatedBitmap(): Bitmap {
        val matrix = Matrix().apply {
            postRotate(this@toRotatedBitmap.imageInfo.rotationDegrees.toFloat())
        }
        return Bitmap.createBitmap(
            this.toBitmap(),
            0,
            0,
            this.width,
            this.height,
            matrix,
            true
        )
    }

    private fun processBufferedText() {
        val textToProcess = textBuffer.toString().trim()
        textBuffer.clear()

        if (textToProcess.isNotEmpty()) {
            languageIdentificationManager.identifyLanguage(
                text = textToProcess,
                onSuccess = { languageCode ->
                    translationManager.translateText(
                        text = textToProcess,
                        sourceLanguage = languageCode,
                        onSuccess = { translatedText ->
                            Log.d("CameraFrameAnalyzer", "Translation success: $translatedText")
                            onTranslationResults(translatedText)
                        },
                        onFailure = { error ->
                            Log.e("TextTranslation", error.message ?: "Error translating text")
                        }
                    )
                },
                onFailure = { error ->
                    Log.e("LanguageIdentification", error.message ?: "Error identifying language")
                }
            )
        }
    }
}