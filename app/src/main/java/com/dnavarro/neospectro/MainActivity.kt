package com.dnavarro.neospectro

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import com.dnavarro.neospectro.ui.AppScreen
import com.dnavarro.neospectro.ui.theme.NeospectroTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.enableEdgeToEdge(window)
        setContent {
            NeospectroTheme {
                AppScreen()
            }
        }
    }
}
