package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.AeroStrikeApp
import com.example.ui.theme.AeroStrikeTheme
import com.example.ui.viewmodel.GameViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AeroStrikeTheme {
                val gameViewModel: GameViewModel = viewModel()
                AeroStrikeApp(viewModel = gameViewModel)
            }
        }
    }
}
