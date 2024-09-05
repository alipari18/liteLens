package com.example.litelens.domain.usecases.bingVisualSearch

import android.graphics.Bitmap
import com.example.litelens.domain.model.VisualSearchResult
import com.example.litelens.domain.repository.bingVisualSearch.BingVisualSearchRepository
import javax.inject.Inject

class PerformVisualSearchUseCase @Inject constructor(
    private val repository: BingVisualSearchRepository
) {
    suspend operator fun invoke(image: Bitmap): Result<List<VisualSearchResult>> {
        return repository.searchImage(image)
    }
}