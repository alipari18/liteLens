package com.example.litelens.data.manager.textRecognition

import android.content.Context
import android.graphics.Bitmap
import android.graphics.RectF
import com.example.litelens.domain.model.Detection
import com.example.litelens.domain.model.DetectionType
import com.example.litelens.domain.repository.textRecognition.TextRecognitionManager
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import javax.inject.Inject

class TextRecognitionManagerImpl @Inject constructor(
    private val context: Context
): TextRecognitionManager {

    private val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    override fun recognizeTextInBitmap(
        bitmap: Bitmap,
        onSuccess: (List<Detection>) -> Unit,
        onError: (String) -> Unit
    ) {
        val image = InputImage.fromBitmap(bitmap, 0)

        textRecognizer.process(image)
            .addOnSuccessListener { visionText ->
                val detectedTextList = mutableListOf<Detection>()

                for(block in visionText.textBlocks){
                    for(line in block.lines){
                        for(element in line.elements){
                            val boundingBox = element.boundingBox?.let{
                                RectF(it)
                            } ?: RectF()

                            detectedTextList.add(
                                Detection(
                                    boundingBox = boundingBox,
                                    detectedObjectName = element.text,
                                    confidenceScore = 1f,
                                    tensorImageWidth = bitmap.width,
                                    tensorImageHeight = bitmap.height,
                                    type = DetectionType.TEXT_DETECTION
                                )
                            )

                            onSuccess(detectedTextList)
                        }
                    }
                }

            }
            .addOnFailureListener { e ->
                onError(e.localizedMessage ?: "Error processing text")
            }
    }
}