package com.example.litelens.data.manager.textTranslation

import com.example.litelens.domain.model.VisualSearchResult
import com.example.litelens.domain.repository.textTranslation.TextTranslationManager
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import javax.inject.Inject

class TextTranslationManagerImpl @Inject constructor() : TextTranslationManager {

    private var client: Translator? = null

    override fun translateText(
        text: String,
        sourceLanguage: String,
        targetLanguage: String,
        onSuccess: (List<VisualSearchResult>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {

        if(client == null){
             client = Translation.getClient(
                TranslatorOptions.Builder()
                    .setSourceLanguage(sourceLanguage)
                    .setTargetLanguage(targetLanguage)
                    .build()
            )
        }

        // Ensure the model is downloaded before translating
        client?.downloadModelIfNeeded()
            ?.addOnSuccessListener {
                client?.translate(text)
                    ?.addOnSuccessListener { translatedText ->
                        val results = mutableListOf<VisualSearchResult>()
                        results.add(
                            VisualSearchResult(
                                originalText = text,
                                sourceLanguage = sourceLanguage,
                                translatedText = translatedText,
                                targetLanguage = targetLanguage,
                                type = "TextSearch"
                            )
                        )

                        onSuccess(results)
                    }
                    ?.addOnFailureListener { exception ->
                        onFailure(exception)
                    }
            }

    }
    // Clean up the translator resources when no longer needed
    fun close() {
        client?.close()
    }
}