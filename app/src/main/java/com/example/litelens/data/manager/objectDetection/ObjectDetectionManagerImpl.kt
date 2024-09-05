package com.example.litelens.data.manager.objectDetection

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.media.Image
import android.util.Log
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageProxy
import androidx.camera.core.impl.utils.MatrixExt.postRotate
import com.example.litelens.domain.model.Detection
import com.example.litelens.domain.model.DetectionType
import com.example.litelens.domain.repository.objectDetection.ObjectDetectionManager
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.ObjectDetector
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.objects.DetectedObject
import javax.inject.Inject

class ObjectDetectionManagerImpl @Inject constructor(
    private val context: Context
): ObjectDetectionManager {

    private lateinit var objectDetector : ObjectDetector;

    init {
        initializeDetector()
    }


    /**
     * Detects objects within the provided bitmap image.
     *
     * @param bitmap The input image in which objects are to be detected.
     * @param rotation The rotation value of the image to adjust its orientation.
     * @param confidenceThreshold The confidence score for filtering out results from model.
     * @return List of detected objects represented by the [Detection] class.
     */
    @OptIn(ExperimentalGetImage::class)
    override fun detectObjectsInCurrentFrame(
        bitmap: Bitmap,
        rotation: Int,
        confidenceThreshold: Float,
        onSuccess: (List<Detection>) -> Unit,
        onError: (String) -> Unit
    ) {

        try{

            // Configure image processor for the given rotation.
            val image = InputImage.fromBitmap(bitmap, rotation)

            objectDetector.process(image)
                .addOnSuccessListener { objects ->
                    Log.d("ObjectDetectionManagerImpl", "Detected objects: ${objects.size}")
                    onSuccess(mapDetections(objects, bitmap.height, bitmap.width, confidenceThreshold))
                }
                .addOnFailureListener {
                    Log.e("ObjectDetectionManagerImpl", "Error detecting objects: ${it.localizedMessage}")
                    onError(it.localizedMessage ?: "Error detecting objects")
                }
        }catch (e: Exception){
            Log.e("ObjectDetectionManagerImpl", "Error detecting objects: ${e.localizedMessage}")
            e.printStackTrace()

        }
    }

    private fun mapDetections(
        detectedObjects: List<DetectedObject>,
        imageHeight: Int,
        imageWidth: Int,
        confidenceThreshold: Float
    ): List<Detection> {
        return detectedObjects.map { detectedObject ->
            val label = detectedObject.labels.firstOrNull()
            val confidence = label?.confidence ?: 0f
            Detection(
                boundingBox = detectedObject.boundingBox,
                detectedObjectName = label?.text ?: "Unknown",
                confidenceScore = confidence,
                imageHeight = imageHeight,
                imageWidth = imageWidth,
                type = DetectionType.OBJECT_DETECTION
            )
        }//.filter { it.confidenceScore >= confidenceThreshold }
    }

    /**
     * Initializes the TensorFlow Lite Object Detector with the given confidence threshold.
     *
     * @param confidenceThreshold The minimum confidence score required for a detected object to be considered.
     */
    private fun initializeDetector() {
        try {

            val options = ObjectDetectorOptions.Builder()
                .setDetectorMode(ObjectDetectorOptions.SINGLE_IMAGE_MODE)
                .build()

            objectDetector = ObjectDetection.getClient(options)

        } catch (exception: IllegalStateException) {
            exception.printStackTrace()
            throw IllegalStateException("Error initializing Object Detector")
        }
    }

}