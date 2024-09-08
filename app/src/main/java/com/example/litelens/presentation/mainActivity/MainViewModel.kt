package com.example.litelens.presentation.mainActivity

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.litelens.domain.usecases.datastore.UserConfigData
import com.example.litelens.presentation.navgraph.Route
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

/**
 * Facilitates rerouting of the application to the proper screens depending on
 * the value obtained from readUserConfig via userConfigUseCases.
 */
@HiltViewModel
class MainViewModel @Inject constructor(
    userConfig: UserConfigData
): ViewModel() {
    companion object {
        private val TAG: String? = MainViewModel::class.simpleName
    }

    // State to obtain the userConfig status stored in Datastore via delegates
    private var redirectFlagState: Boolean = true

    // Initializing Start-Destination via delegates to obtain value only once
    var startDestination by mutableStateOf(Route.HomeNavigation.route)

    /**
     * Retrieve the boolean Flag from Datastore to decide on Navigation of application
     */
    init {
        userConfig.readUserConfig().onEach {
            Log.d(TAG, "init() called with shouldStartFromHomeScreen flag = $it")
            Log.d(TAG, "redirectFlagState = $redirectFlagState")

        }.launchIn(viewModelScope)      // Start collecting the flow & handle each emitted item within the coroutine scope
    }
}