package com.example.litelens.presentation.savedSearches

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.litelens.R
import com.example.litelens.domain.model.VisualSearchResult
import com.example.litelens.presentation.common.AlertDialogComposable
import java.net.URI
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SavedSearches() {

    val TAG: String = "SavedSearches"

    val viewModel: SavedSearchesViewModel = hiltViewModel()

    val visualSearchResults by viewModel.visualSearchResults.collectAsState()

    val isLoading by viewModel.isLoading.collectAsState()

    val context = LocalContext.current

    var showDialog by remember { mutableStateOf(false) }
    var selectedResult by remember { mutableStateOf<VisualSearchResult?>(null) }

    LaunchedEffect(Unit) {
        viewModel.retrieveSavedSearches()
    }

    // Display the saved searches
    Log.d(TAG, "Saved Searches: $visualSearchResults")

    when {
        isLoading -> {
            Box(modifier = Modifier.fillMaxSize()) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = colorResource(id = R.color.white)
                )
            }
        }
        visualSearchResults.isEmpty() -> {
            Box(modifier = Modifier.fillMaxSize()) {
                Text(
                    text = "No saved searches",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp)
                )
            }
        }
        else -> {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp)
            ) {
                items(visualSearchResults) { search ->
                    if(search.type == "VisualSearch") {
                        SavedSearchItem(
                            imageUrl = search.imageUrl ?: "",
                            thumbnailUrl = search.thumbnailUrl ?: "",
                            title = search.title ?: "No title",
                            url = search.url ?: "No URL",
                            timestamp = search.timestamp?.toDate()?.time ?: 0,
                            onViewClick = {
                                viewModel.openUrlInBrowser(context = context, search.url)
                            },
                            onDeleteClick = {
                                selectedResult = search
                                showDialog = true
                            }
                        )
                    } else if(search.type == "TextSearch") {

                        TextSearchItem(
                            search = search,
                            onDeleteClick = {
                                selectedResult = search
                                showDialog = true
                            }
                        )
                    }
                }
            }

            if(showDialog){
                AlertDialogComposable(
                    title = "Delete Search",
                    message = "Confirm that you want to delete the search?",
                    firstButtonText = "Delete",
                    icon = Icons.Default.Delete,
                    result = selectedResult,
                    onDismissRequest = { showDialog = false },
                    onSaveImage = {
                        Toast.makeText(context, "Deleting the search..", Toast.LENGTH_SHORT).show()
                        viewModel.deleteSearchResult(context, selectedResult?.documentId ?: "")
                        showDialog = false
                    },
                    isLoading = false,
                    onThirdButtonClick = {}
                )
            }
        }
    }


}


@Composable
fun SavedSearchItem(
    imageUrl: String,
    thumbnailUrl: String,
    title: String,
    url: String,
    timestamp: Long,
    onViewClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column {
            // Main image
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = "Search Image",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentScale = ContentScale.Crop
            )

            Column(modifier = Modifier.padding(16.dp)) {
                // Title
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))

                // URL domain
                Text(
                    text = try {
                        URI(url).host ?: url
                    } catch (e: Exception) {
                        url
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Timestamp
                Text(
                    text = SimpleDateFormat("MMM d, yyyy HH:mm", Locale.getDefault()).format(Date(timestamp)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(onClick = onViewClick) {
                        Text("View in browser")
                    }
                    TextButton(onClick = onDeleteClick) {
                        Text("Delete")
                    }
                }


            }
        }
    }
}

@Composable
fun TextSearchItem(
    search: VisualSearchResult,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column {
            // Main image
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(search.imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = "Search Image",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentScale = ContentScale.Crop
            )

            Column(modifier = Modifier.padding(16.dp)) {
                // Original Text and Source Language
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Original (${search.sourceLanguage})",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = search.originalText ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Translated (${search.targetLanguage})",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = search.translatedText ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Timestamp
                Text(
                    text = SimpleDateFormat("MMM d, yyyy HH:mm", Locale.getDefault()).format(Date(search.timestamp?.toDate()?.time ?: 0)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDeleteClick) {
                        Text("Delete")
                    }
                }
            }
        }
    }
}