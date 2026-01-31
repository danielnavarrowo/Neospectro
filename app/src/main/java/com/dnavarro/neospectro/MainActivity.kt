package com.dnavarro.neospectro

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import com.dnavarro.neospectro.ui.AppScreen
import com.dnavarro.neospectro.ui.theme.NeospectroTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NeospectroTheme {
                AppScreen()
            }
        }
    }
}
