package com.example.litelens.data.manager.objectDetection

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.example.litelens.domain.model.Detection
import com.example.litelens.domain.model.DetectionType
import com.example.litelens.domain.repository.objectDetection.ObjectDetectionManager
import com.example.litelens.utils.Constants
import org.tensorflow.lite.gpu.CompatibilityList
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.task.core.BaseOptions
import org.tensorflow.lite.task.vision.detector.ObjectDetector
import javax.inject.Inject

class ObjectDetectionManagerImpl @Inject constructor(
    private val context: Context
): ObjectDetectionManager {

    private lateinit var detector: ObjectDetector

    init {
        // Initialize the detector on the main thread
        detector = initializeDetector(Constants.INITIAL_CONFIDENCE_SCORE)
    }

    /**
     * Detects objects within the provided bitmap image.
     *
     * @param bitmap The input image in which objects are to be detected.
     * @param rotation The rotation value of the image to adjust its orientation.
     * @param confidenceThreshold The confidence score for filtering out results from model.
     * @return List of detected objects represented by the [Detection] class.
     */
    override fun detectObjectsInCurrentFrame(
        bitmap: Bitmap,
        rotation: Int,
        confidenceThreshold: Float
    ): List<Detection> {

        // Configure image processor for the given rotation.
        val imageProcessor =
            ImageProcessor.Builder()
                .build()

        // Convert the bitmap into a TensorImage for processing.
        val tensorImage: TensorImage = imageProcessor.process(
            TensorImage.fromBitmap(bitmap)
        )

        Log.d("ObjectDetectionManagerImpl", "TensorImage size: ${tensorImage.height} x ${tensorImage.width}")

        // Obtain Results
        synchronized(detector){
            val detectionResults = detector.detect(
                tensorImage
            )

            Log.d("ObjectDetectionManagerImpl", "Detected objects: ${detectionResults?.size}")
            // Map detected objects to 'Detection' and filter by confidence threshold
            return detectionResults?.mapNotNull { detectedObject ->
                if ((detectedObject.categories.firstOrNull()?.score ?: 0f) >= confidenceThreshold) {
                    Log.d("ObjectDetectionManagerImpl", "Detected object: ${detectedObject.categories.firstOrNull()?.label}")
                    Detection(
                        boundingBox = detectedObject.boundingBox,
                        detectedObjectName = detectedObject.categories.firstOrNull()?.label ?: "",
                        confidenceScore = detectedObject.categories.firstOrNull()?.score ?: 0f,
                        tensorImage.height,
                        tensorImage.width,
                        type = DetectionType.OBJECT_DETECTION
                    )
                } else null
            }?.take(Constants.MODEL_MAX_RESULTS_COUNT) ?: emptyList()
        }



    }

    /**
     * Initializes the TensorFlow Lite Object Detector with the given confidence threshold.
     *
     * @param confidenceThreshold The minimum confidence score required for a detected object to be considered.
     */
    private fun initializeDetector(confidenceThreshold: Float): ObjectDetector {
        try {
            val baseOptions = BaseOptions.builder()
                .setNumThreads(2)

            // Using GPU if available
            if (CompatibilityList().isDelegateSupportedOnThisDevice) {
                baseOptions.useGpu()
            } else {
                baseOptions.useNnapi()
            }

            val options = ObjectDetector.ObjectDetectorOptions.builder()
                .setBaseOptions(baseOptions.build())
                .setMaxResults(Constants.MODEL_MAX_RESULTS_COUNT)
                .setScoreThreshold(confidenceThreshold)
                .build()

            return ObjectDetector.createFromFileAndOptions(
                context,
                Constants.MODEL_PATH,
                options
            )
        } catch (exception: IllegalStateException) {
            exception.printStackTrace()
            throw IllegalStateException("Error initializing Object Detector")
        }
    }
}