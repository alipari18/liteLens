package com.example.litelens.domain.repository.objectDetection

import android.graphics.Bitmap
import android.media.Image
import androidx.camera.core.ImageProxy
import com.example.litelens.domain.model.Detection
import com.google.mlkit.vision.objects.DetectedObject

interface ObjectDetectionManager {
    fun detectObjectsInCurrentFrame(
        bitmap: Bitmap,
        rotation: Int,
        confidenceThreshold: Float,
        onSuccess: (List<Detection>) -> Unit,
        onError: (String) -> Unit
    )
}