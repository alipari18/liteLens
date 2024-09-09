package com.example.litelens.presentation.mainActivity

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.litelens.presentation.navgraph.Route
import dagger.hilt.android.lifecycle.HiltViewModel

import javax.inject.Inject

/**
 * Facilitates rerouting of the application to the proper screens depending on
 * the value obtained from readUserConfig via userConfigUseCases.
 */
@HiltViewModel
class MainViewModel @Inject constructor(
): ViewModel() {
    companion object {
        private val TAG: String? = MainViewModel::class.simpleName
    }


    // Initializing Start-Destination via delegates to obtain value only once
    var startDestination by mutableStateOf(Route.HomeNavigation.route)


}