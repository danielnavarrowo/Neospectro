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
        Color.rgb(123, 123, 255),
        Color.rgb(241,241,255)
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
        ),
        WaveTheme(
            Constants.THEME_PINK,
            Color.MAGENTA,
            Color.rgb(255, 112, 245),
            Color.rgb(255, 240, 254)
        ),
        WaveTheme(
            Constants.THEME_CYAN,
            Color.CYAN,
            Color.rgb(99, 255, 255),
            Color.rgb(235, 252, 252)
        ),
        WaveTheme(
            Constants.THEME_YELLOW,
            Color.rgb(255, 255, 0),
            Color.rgb(255, 255, 90),
            Color.WHITE
        )
        ,
        WaveTheme(
            Constants.THEME_PURPLE,
            Color.rgb(128, 0, 255),
            Color.rgb(179, 102, 255),
            Color.WHITE
        )
    )

    fun getTheme(id: String): WaveTheme {
        return themes.find { it.id == id } ?: DefaultTheme
    }
}
