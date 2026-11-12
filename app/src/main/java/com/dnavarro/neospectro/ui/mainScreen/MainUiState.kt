package com.dnavarro.neospectro.ui.mainScreen

import com.dnavarro.neospectro.Constants

data class MainUiState(
    val selectedTheme: String = Constants.THEME_ICE,
    val reverseColors: Boolean = false,
    val audioVizEnabled: Boolean = false,
    val hasBgImage: Boolean = false,
    val bgImageTrigger: Int = 0,
    val isCustomThemeEnabled: Boolean = false,
    val customEdgeColor: Int = 0xFF0303FF.toInt(),
    val customMiddleColor: Int = 0xFF7B7BFF.toInt(),
    val customCenterColor: Int = 0xFFF1F1FF.toInt()
)
