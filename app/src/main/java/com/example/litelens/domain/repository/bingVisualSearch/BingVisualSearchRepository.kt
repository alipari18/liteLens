package com.example.litelens.domain.repository.bingVisualSearch

import android.graphics.Bitmap
import com.example.litelens.domain.model.VisualSearchResult

interface BingVisualSearchRepository {

    suspend fun searchImage(image: Bitmap): Result<List<VisualSearchResult>>

}