package com.example.litelens.data.manager.textRecognition

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Rect
import android.graphics.RectF
import com.example.litelens.domain.model.Detection
import com.example.litelens.domain.model.DetectionType
import com.example.litelens.domain.repository.textRecognition.TextRecognitionManager
import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.chinese.ChineseTextRecognizerOptions
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import javax.inject.Inject
import kotlin.math.abs

class TextRecognitionManagerImpl @Inject constructor(
    private val context: Context
): TextRecognitionManager {

    private val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    private val chineseTextRecognizer = TextRecognition.getClient(ChineseTextRecognizerOptions.Builder().build())

    override fun recognizeTextInBitmap(
        bitmap: Bitmap,
        rotationDegrees: Int,
        onSuccess: (Text?) -> Unit,
        onError: (Exception) -> Unit
    ): Task<Text> {
        val image = InputImage.fromBitmap(bitmap, rotationDegrees)

        return textRecognizer.process(image)
            .addOnSuccessListener { visionText ->
                // Process the center-most text block
                val centerBlock = visionText.textBlocks
                    .maxByOrNull { block ->
                        val centerX = bitmap.width / 2
                        val centerY = bitmap.height / 2
                        val blockCenterX = (block.boundingBox?.left ?: 0) + (block.boundingBox?.width() ?: 0) / 2
                        val blockCenterY = (block.boundingBox?.top ?: 0) + (block.boundingBox?.height() ?: 0) / 2
                        -(abs(centerX - blockCenterX) + abs(centerY - blockCenterY))
                    }

                onSuccess(Text(centerBlock?.text ?: "", listOfNotNull(centerBlock)))
            }
            .addOnFailureListener { e ->
                onError(e)
            }
    }
}