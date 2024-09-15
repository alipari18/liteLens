package com.example.litelens.domain.repository.textTranslation

import com.example.litelens.domain.model.VisualSearchResult

interface TextTranslationManager {
    fun translateText(
        text: String,
        sourceLanguage: String,
        targetLanguage: String = "it",  // Default to Italian
        onSuccess: (List<VisualSearchResult>) -> Unit,
        onFailure: (Exception) -> Unit
    )
}