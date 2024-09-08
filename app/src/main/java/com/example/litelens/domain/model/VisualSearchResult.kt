package com.example.litelens.domain.model

import com.google.firebase.firestore.PropertyName

data class VisualSearchResult(
    @PropertyName("title") val title: String? = null,
    @PropertyName("url") val url: String? = null,
    @PropertyName("snippet") val snippet: String? = null,
    @PropertyName("actionType") val actionType: String? = null,
    @PropertyName("thumbnailUrl") val thumbnailUrl: String? = null,
    @PropertyName("contentUrl") val contentUrl: String? = null,
    @PropertyName("contentSize") val contentSize: String? = null,
    @PropertyName("encodingFormat") val encodingFormat: String? = null,
    @PropertyName("width") val width: Int? = null,
    @PropertyName("height") val height: Int? = null,
    @PropertyName("imageUrl") val imageUrl: String? = null,
    @PropertyName("timestamp") val timestamp: com.google.firebase.Timestamp? = null,
    @PropertyName("documentId") val documentId: String? = null,
    @PropertyName("type") val type: String? = null

) {
    // No-argument constructor required by Firestore
    constructor() : this(null, null, null)
}