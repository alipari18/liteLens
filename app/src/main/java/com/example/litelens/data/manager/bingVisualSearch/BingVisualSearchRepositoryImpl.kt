package com.example.litelens.data.manager.bingVisualSearch

import android.graphics.Bitmap
import android.util.Log
import com.example.litelens.domain.model.VisualSearchResult
import com.example.litelens.domain.repository.bingVisualSearch.BingVisualSearchRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import javax.inject.Inject

class BingVisualSearchRepositoryImpl @Inject constructor(
    private val client: OkHttpClient
) : BingVisualSearchRepository {

    override suspend fun searchImage(image: Bitmap): Result<List<VisualSearchResult>> = withContext(Dispatchers.IO) {
        try {
            val byteArrayOutputStream = ByteArrayOutputStream()
            image.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
            val imageBytes = byteArrayOutputStream.toByteArray()

            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(
                    "image",
                    "image.jpg",
                    imageBytes.toRequestBody("image/jpeg".toMediaTypeOrNull())
                )
                .build()

            val request = Request.Builder()
                .url("https://api.bing.microsoft.com/v7.0/images/visualsearch")
                .addHeader("Ocp-Apim-Subscription-Key", "YOUR_SUBSCRIPTION_KEY")
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            val jsonResponse = JSONObject(response.body?.string() ?: "")

            Log.d("BingVisualSearchRepositoryImpl", "searchImage: $jsonResponse")

            val results = mutableListOf<VisualSearchResult>()
            val tags = jsonResponse.getJSONArray("tags")
            for (i in 0 until tags.length()) {
                val tag = tags.getJSONObject(i)
                if (tag.has("actions")) {
                    val actions = tag.getJSONArray("actions")
                    for (j in 0 until actions.length()) {
                        val action = actions.getJSONObject(j)
                        if (action.getString("actionType") == "PagesIncluding") {
                            val data = action.getJSONObject("data")
                            val value = data.getJSONArray("value")
                            for (k in 0 until value.length()) {
                                val item = value.getJSONObject(k)
                                results.add(
                                    VisualSearchResult(
                                        title = item.getString("name"),
                                        url = item.getString("hostPageUrl"),
                                        snippet = item.getString("snippet")
                                    )
                                )
                            }
                            break
                        }
                    }
                }
            }
            Result.success(results)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}