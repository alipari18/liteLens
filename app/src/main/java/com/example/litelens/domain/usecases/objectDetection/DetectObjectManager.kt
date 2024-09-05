package com.example.litelens.domain.usecases.objectDetection

import android.graphics.Bitmap
import android.media.Image
import androidx.camera.core.ImageProxy
import com.example.litelens.domain.model.Detection
import com.example.litelens.domain.repository.objectDetection.ObjectDetectionManager
import com.google.mlkit.vision.objects.DetectedObject

class DetectObjectManager(
    private val objectDetectionManager: ObjectDetectionManager
) {
    /**
     * UseCase responsible for executing the object detection process.
     *
     * This function delegates the object detection task to the provided
     * [ObjectDetectionManager] and returns the detected objects.
     *
     * @param bitmap The input image in which objects are to be detected.
     * @param rotation The rotation value of the image to adjust its orientation.
     * @param confidenceThreshold The confidence score for filtering out results from model.
     * @return List of detected objects represented by the [Detection] class.
     */
    fun execute(
        bitmap: Bitmap,
        rotation: Int,
        confidenceThreshold: Float,
        onSuccess: (List<Detection>) -> Unit,
        onError: (String) -> Unit
    ) {
        return objectDetectionManager.detectObjectsInCurrentFrame(
            bitmap = bitmap,
            rotation,
            confidenceThreshold,
            onSuccess,
            onError
        )
    }

}