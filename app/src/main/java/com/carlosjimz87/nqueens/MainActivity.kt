package com.carlosjimz87.nqueens

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.carlosjimz87.nqueens.ui.screens.board.BoardScreen
import com.carlosjimz87.nqueens.ui.theme.NQueensTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NQueensTheme {
                BoardScreen(modifier = Modifier.fillMaxSize())
            }
        }
    }
}