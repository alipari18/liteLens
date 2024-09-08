package com.example.litelens.data.manager.bingVisualSearch

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.litelens.domain.model.VisualSearchResult
import com.example.litelens.domain.repository.bingVisualSearch.BingVisualSearchRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class BingVisualSearchRepositoryImpl @Inject constructor(
    private val client: OkHttpClient,
    private val context: Context
) : BingVisualSearchRepository {

    override suspend fun searchImage(image: Bitmap): Result<List<VisualSearchResult>> = withContext(Dispatchers.IO) {
        try {
            val byteArrayOutputStream = ByteArrayOutputStream()
            image.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
            val imageBytes = byteArrayOutputStream.toByteArray()

            val allowedSites = mutableListOf<String>()
            allowedSites.add("amazon.com")
            allowedSites.add("ebay.com")
            allowedSites.add("wikipedia.org")

            val knowledgeRequest = JSONObject().apply {
                put("knowledgeRequest", JSONObject().apply {
                    put("filters", JSONObject().apply {
                        put("site", allowedSites.joinToString(","))
                    })
                })
            }

            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(
                    "image",
                    "image.jpg",
                    imageBytes.toRequestBody("image/jpeg".toMediaTypeOrNull())
                )
                //.addFormDataPart("knowledgeRequest", knowledgeRequest.toString())
                .build()

            val request = Request.Builder()
                .url("https://api.bing.microsoft.com/v7.0/images/visualsearch?mkt=en-US&safesearch=Strict")
                .addHeader("Ocp-Apim-Subscription-Key", "bf4fd86d53204777bb564ef06022d873")
                .post(requestBody)
                .build()

            Log.d("BingVisualSearch", "Request URL: ${request.url}")
            Log.d("BingVisualSearch", "Request Headers: ${request.headers}")

            val response = client.newCall(request).execute()

            val jsonResponse = JSONObject(response.body?.string() ?: "")

            Log.d("BingVisualSearch", "Response headers: ${response.headers}")

            Log.d("BingVisualSearchRepositoryImpl", "searchImage: $jsonResponse")

            val results = mutableListOf<VisualSearchResult>()
            val tags = jsonResponse.optJSONArray("tags") ?: return@withContext Result.success(emptyList())

            for (i in 0 until tags.length()) {
                val tag = tags.getJSONObject(i)
                val actions = tag.optJSONArray("actions") ?: continue

                for (j in 0 until actions.length()) {
                    val action = actions.getJSONObject(j)
                    val actionType = action.optString("actionType")

                    when (actionType) {
                        "PagesIncluding" -> {
                            val data = action.optJSONObject("data")
                            val value = data?.optJSONArray("value") ?: continue
                            for (k in 0 until value.length()) {
                                val item = value.getJSONObject(k)
                                results.add(
                                    VisualSearchResult(
                                        title = item.optString("name", ""),
                                        url = item.optString("hostPageUrl", ""),
                                        snippet = item.optString("snippet", ""),
                                        actionType = actionType,
                                        thumbnailUrl = item.optString("thumbnailUrl", ""),
                                        contentUrl = item.optString("contentUrl", ""),
                                        contentSize = item.optString("contentSize", ""),
                                        encodingFormat = item.optString("encodingFormat", ""),
                                        width = item.optInt("width", 0),
                                        height = item.optInt("height", 0)
                                    )
                                )
                            }
                        }
                        "VisualSearch" -> {
                            val data = action.optJSONObject("data")
                            val value = data?.optJSONArray("value") ?: continue
                            for (k in 0 until value.length()) {
                                val item = value.getJSONObject(k)
                                results.add(
                                    VisualSearchResult(
                                        title = item.optString("name", ""),
                                        url = item.optString("hostPageUrl", ""),
                                        snippet = item.optString("snippet", ""),
                                        actionType = actionType,
                                        thumbnailUrl = item.optString("thumbnailUrl", ""),
                                        contentUrl = item.optString("contentUrl", ""),
                                        contentSize = item.optString("contentSize", ""),
                                        encodingFormat = item.optString("encodingFormat", ""),
                                        width = item.optInt("width", 0),
                                        height = item.optInt("height", 0)
                                    )
                                )
                            }
                        }
                        "RelatedSearches" -> {
                            val data = action.optJSONObject("data")
                            val value = data?.optJSONArray("value") ?: continue
                            for (k in 0 until value.length()) {
                                val item = value.getJSONObject(k)
                                results.add(
                                    VisualSearchResult(
                                        title = item.optString("displayText", ""),
                                        url = item.optString("webSearchUrl", ""),
                                        snippet = "Related search",
                                        actionType = actionType
                                    )
                                )
                            }
                        }
                        // Exclude "MoreSizes" and "ImageById" actions
                    }
                }
            }
            if(results.isEmpty()){
                Log.d("BingVisualSearchRepositoryImpl", "searchImage: No results found")
            }
            Log.d("BingVisualSearchRepositoryImpl", "searchImage after: $results")
            Result.success(results)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun saveImageToFile(image: Bitmap): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "DEBUG_IMAGE_$timeStamp.jpg"

        // Use internal cache directory
        val directory = context.cacheDir
        val file = File(directory, fileName)

        FileOutputStream(file).use { out ->
            image.compress(Bitmap.CompressFormat.JPEG, 100, out)
        }

        return file
    }

    private fun shareDebugImage(file: File) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/jpeg"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(Intent.createChooser(intent, "Share Debug Image").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }
}