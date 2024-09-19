package com.example.litelens.utils

import android.graphics.Bitmap
import com.example.litelens.domain.model.VisualSearchResult
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import java.util.UUID

class FirebaseStorageManager {
    private val storage = FirebaseStorage.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    suspend fun saveImageAndResult(bitmap: Bitmap, searchResult: VisualSearchResult): Result<String> {
        return try{

            // Upload image to Cloud Storage
            val imageUrl = uploadImageToStorage(bitmap)

            // save search result and image URL to firestore
            val documentId = saveToFirestore(imageUrl, searchResult)

            Result.success(documentId)

        }catch (e: Exception){
            Result.failure(e)
        }
    }

    private suspend fun uploadImageToStorage(bitmap: Bitmap): String {
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()

        val filename = UUID.randomUUID().toString()
        val storageRef = storage.reference.child("images/$filename.jpg")

        return storageRef.putBytes(data).await().storage.downloadUrl.await().toString()
    }

    private suspend fun saveToFirestore(imageUrl: String, searchResult: VisualSearchResult): String {
        val document = firestore.collection("searchResults").document()

        val searchResultMap = if(searchResult.type == "VisualSearch") {
            hashMapOf(
                "documentId" to document.id,
                "imageUrl" to imageUrl,
                "title" to searchResult.title,
                "thumbnailUrl" to searchResult.thumbnailUrl,
                "url" to searchResult.url,
                "timestamp" to com.google.firebase.Timestamp.now(),
                "type" to searchResult.type
            )
        }else{
            hashMapOf(
                "documentId" to document.id,
                "imageUrl" to imageUrl,
                "sourceLanguage" to searchResult.sourceLanguage,
                "translatedText" to searchResult.translatedText,
                "targetLanguage" to searchResult.targetLanguage,
                "originalText" to searchResult.originalText,
                "type" to searchResult.type,
                "timestamp" to com.google.firebase.Timestamp.now()
            )
        }

        document.set(searchResultMap).await()
        return document.id
    }

    suspend fun getSearchResult(): Result<List<VisualSearchResult>> {
        return try {
            val documents = firestore.collection("searchResults").get().await()
            val data = documents.mapNotNull { document ->

                val result = document.toObject(VisualSearchResult::class.java)
                result
            }
            Result.success(data)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteSearchResult(documentId: String): Result<Unit> {
        return try {
            firestore.collection("searchResults").document(documentId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}