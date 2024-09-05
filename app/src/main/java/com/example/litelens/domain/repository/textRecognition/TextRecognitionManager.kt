package com.example.litelens.domain.repository.textRecognition

import android.graphics.Bitmap
import com.example.litelens.domain.model.Detection
import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.text.Text

interface TextRecognitionManager {
    fun recognizeTextInBitmap(
        bitmap: Bitmap,
        rotationDegrees: Int,
        onSuccess: (Text?) -> Unit,
        onError: (Exception) -> Unit
    ): Task<Text>
}