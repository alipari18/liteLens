package com.example.litelens.presentation.savedSearches

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.litelens.domain.model.VisualSearchResult
import com.example.litelens.utils.FirebaseStorageManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SavedSearchesViewModel @Inject constructor(
    private val firebaseStorageManager: FirebaseStorageManager
): ViewModel() {

    companion object {
        private val TAG: String? = SavedSearchesViewModel::class.simpleName
    }

    private val _visualSearchResults = MutableStateFlow<List<VisualSearchResult>>(emptyList())
    val visualSearchResults: StateFlow<List<VisualSearchResult>> = _visualSearchResults

    /**
     * Retrieve the saved searches from Firebase Storage
     */
    fun retrieveSavedSearches() {

        viewModelScope.launch {
            firebaseStorageManager.getSearchResult()
                .onSuccess { searchResults ->
                    Log.d(TAG, "Retrieved saved searches: $searchResults")
                    _visualSearchResults.value = searchResults
                }
                .onFailure {
                    Log.d(TAG, "Failed to retrieve saved searches: $it")
                }
        }
    }
}