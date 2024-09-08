package com.example.litelens.presentation.savedSearches

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun SavedSearches() {

    val TAG: String = "SavedSearches"

    val viewModel: SavedSearchesViewModel = hiltViewModel()

    val visualSearchResults by viewModel.visualSearchResults.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.retrieveSavedSearches()
    }

    // Display the saved searches
    Log.d(TAG, "Saved Searches: $visualSearchResults")
}