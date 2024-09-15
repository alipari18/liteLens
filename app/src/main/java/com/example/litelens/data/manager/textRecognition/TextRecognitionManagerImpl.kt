package com.example.litelens.data.manager.textRecognition

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Rect
import android.graphics.RectF
import android.util.Log
import androidx.core.content.FileProvider
import com.example.litelens.domain.model.Detection
import com.example.litelens.domain.model.DetectionType
import com.example.litelens.domain.repository.textRecognition.TextRecognitionManager
import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.chinese.ChineseTextRecognizerOptions
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
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
        onSuccess: (Detection) -> Unit,
        onError: (Exception) -> Unit
    ): Task<Text> {
        val image = InputImage.fromBitmap(bitmap, rotationDegrees)

        // Save image to file for debugging
        //val file = saveImageToFile(bitmap)
        //shareDebugImage(file)
        //Log.d("TextRecognitionManagerImpl", "Saved image to file: ${file.absolutePath}")

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

                if (centerBlock != null) {
                    val detection = Detection(
                        boundingBox = Rect(0,0,0,0),
                        detectedObjectName = centerBlock.text,
                        confidenceScore = 0f,
                        imageHeight = bitmap.height,
                        imageWidth = bitmap.width,
                        type = DetectionType.TEXT_DETECTION,
                        bitmap = bitmap
                    )
                    onSuccess(detection)
                }


            }
            .addOnFailureListener { e ->
                onError(e)
            }
    }


    private fun saveImageToFile(image: Bitmap): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "DEBUG_IMAGE_$timeStamp.jpg"

        // Use internal cache directory
        val directory = context.cacheDir
        val file = File(directory, fileName)

        FileOutputStream(file).use { out ->
            image.compress(Bitmap.CompressFormat.JPEG, 100, out)
        }

        return file
    }

    private fun shareDebugImage(file: File) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/jpeg"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(Intent.createChooser(intent, "Share Debug Image").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }
}