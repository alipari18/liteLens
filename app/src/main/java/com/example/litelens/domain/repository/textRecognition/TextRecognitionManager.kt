package com.example.litelens.domain.repository.textRecognition

import android.graphics.Bitmap
import com.example.litelens.domain.model.Detection

interface TextRecognitionManager {
    fun recognizeTextInBitmap(
        bitmap: Bitmap,
        onSuccess: (List<Detection>) -> Unit,
        onError: (String) -> Unit
    )
}