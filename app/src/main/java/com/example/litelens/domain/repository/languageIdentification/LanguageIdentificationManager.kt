package com.example.litelens.domain.repository.languageIdentification

interface LanguageIdentificationManager {
    fun identifyLanguage(
        text: String,
        onSuccess: (String) -> Unit,
        onFailure: (Exception) -> Unit
    )
}