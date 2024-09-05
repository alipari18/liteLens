package com.example.litelens.data.manager.languageIdentification

import android.util.Log
import com.example.litelens.domain.repository.languageIdentification.LanguageIdentificationManager
import com.google.mlkit.nl.languageid.LanguageIdentification
import javax.inject.Inject

class LanguageIdentificationManagerImpl @Inject constructor() : LanguageIdentificationManager {

    private val languageIdentifier = LanguageIdentification.getClient()

    override fun identifyLanguage(
        text: String,
        onSuccess: (String) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        Log.d("LanguageIdentificationManagerImpl", "Identifying language for text: $text")
        languageIdentifier.identifyLanguage(text)
            .addOnSuccessListener { languageCode ->
                if (languageCode != "und") {
                    Log.i("LanguageIdentificationManagerImpl", "Identified language: $languageCode")
                    onSuccess(languageCode)
                } else {
                    // return default
                    onSuccess("en")
                    //onFailure(Exception("Language could not be identified"))
                }
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }

}
