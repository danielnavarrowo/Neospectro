package com.dnavarro.neospectro.ui.mainScreen

import android.graphics.Color
import com.dnavarro.neospectro.Constants

data class MainUiState(
    val selectedTheme: String = Constants.THEME_ICE,
    val reverseColors: Boolean = false,
    val audioVizEnabled: Boolean = false,
    val hasBgImage: Boolean = false,
    val bgImageTrigger: Int = 0,
    val isCustomThemeEnabled: Boolean = false,
    val customEdgeColor: Int = Color.rgb(3, 3, 255),
    val customMiddleColor: Int = Color.rgb(123, 123, 255),
    val customCenterColor: Int = Color.rgb(241, 241, 255)
)
