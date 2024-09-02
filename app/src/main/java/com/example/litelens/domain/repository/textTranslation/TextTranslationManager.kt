package com.example.litelens.domain.repository.textTranslation

interface TextTranslationManager {
    fun translateText(
        text: String,
        sourceLanguage: String,
        targetLanguage: String = "it",  // Default to Italian
        onSuccess: (String) -> Unit,
        onFailure: (Exception) -> Unit
    )
}