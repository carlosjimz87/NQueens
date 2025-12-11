package com.carlosjimz87.nqueens

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.carlosjimz87.nqueens.ui.screens.board.ColorDemoScreen
import com.carlosjimz87.nqueens.ui.theme.NQueensTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NQueensTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    ColorDemoScreen(Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "Light Theme")
@Composable
fun ColorDemoLightPreview() {
    NQueensTheme(darkTheme = false, dynamicColor = false) {
        ColorDemoScreen()
    }
}

@Preview(showBackground = true, name = "Dark Theme")
@Composable
fun ColorDemoDarkPreview() {
    NQueensTheme(darkTheme = true, dynamicColor = false) {
        ColorDemoScreen()
    }
}

@Preview(showBackground = true, name = "Light Theme - Dynamic")
@Composable
fun ColorDemoLightDynamicPreview() {
    NQueensTheme(darkTheme = false, dynamicColor = true) {
        ColorDemoScreen()
    }
}

@Preview(showBackground = true, name = "Dark Theme - Dynamic")
@Composable
fun ColorDemoDarkDynamicPreview() {
    NQueensTheme(darkTheme = true, dynamicColor = true) {
        ColorDemoScreen()
    }
}