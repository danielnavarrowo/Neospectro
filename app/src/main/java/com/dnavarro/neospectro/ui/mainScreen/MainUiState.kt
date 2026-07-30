package com.dnavarro.neospectro.ui.mainScreen

import com.dnavarro.neospectro.Constants

data class MainUiState(
    val selectedTheme: String = Constants.THEME_ICE,
    val reverseColors: Boolean = false,
    val audioVizEnabled: Boolean = false,
    val hasBgImage: Boolean = false,
    val bgImageTrigger: Int = 0
)
