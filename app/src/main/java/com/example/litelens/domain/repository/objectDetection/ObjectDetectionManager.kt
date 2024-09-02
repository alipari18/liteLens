package com.example.litelens.domain.repository.objectDetection

import android.graphics.Bitmap
import com.example.litelens.domain.model.Detection

interface ObjectDetectionManager {
    fun detectObjectsInCurrentFrame(
        bitmap: Bitmap,
        rotation: Int,
        confidenceThreshold: Float
    ): List<Detection>
}