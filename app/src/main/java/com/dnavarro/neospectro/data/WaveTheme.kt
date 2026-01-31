package com.dnavarro.neospectro.data

import android.graphics.Color
import com.dnavarro.neospectro.Constants

data class WaveTheme(
    val id: String,
    val edgeColor: Int,
    val middleColor: Int,
    val centerColor: Int
)

object ThemeRepository {
    private val DefaultTheme = WaveTheme(
        Constants.THEME_ICE,
        Color.rgb(3, 3, 255),
        Color.rgb(0, 128, 255),
        Color.WHITE
    )

    val themes = listOf(
        DefaultTheme,
        WaveTheme(
            Constants.THEME_FIRE,
            Color.RED,
            Color.rgb(255, 128, 0),
            Color.YELLOW
        ),
        WaveTheme(
            Constants.THEME_ACID,
            Color.GREEN,
            Color.rgb(144, 238, 144),
            Color.WHITE
        )
    )

    fun getTheme(id: String): WaveTheme {
        return themes.find { it.id == id } ?: DefaultTheme
    }
}
