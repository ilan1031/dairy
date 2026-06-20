package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.screens.MainAppScreen
import com.example.ui.viewmodel.DairyViewModel

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      val viewModel: DairyViewModel = viewModel()
      val isLightTheme by viewModel.isLightTheme.collectAsState()
      MyApplicationTheme(darkTheme = !isLightTheme) {
        Surface(
          modifier = Modifier.fillMaxSize()
        ) {
          MainAppScreen(viewModel = viewModel)
        }
      }
    }
  }
}
