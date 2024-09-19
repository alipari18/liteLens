package com.example.litelens.presentation.mainActivity

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import com.example.litelens.presentation.navgraph.NavGraph
import com.example.litelens.theme.LiteLensTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    companion object {
        private val TAG: String? = MainActivity::class.simpleName
    }

    private val viewModel by viewModels<MainViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            LiteLensTheme {
                    Box(modifier = Modifier.background(color = MaterialTheme.colorScheme.background)) {

                        val startDestination = viewModel.startDestination
                        Log.d(TAG, "setContent() called with startDestination = $startDestination ")
                        NavGraph(startDestination = startDestination)
                    }
            }
        }
    }
}