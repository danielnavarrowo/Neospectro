package com.dnavarro.neospectro.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.material3.ListItemColors
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.SwitchColors
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeInput
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.blur.HazeBlurStyle
import dev.chrisbanes.haze.blur.HazeColorEffect
import dev.chrisbanes.haze.blur.hazeBlur

object GlassDefaults {
    val PrimaryContentColor = Color(0xFFF5F7FA)
    val SecondaryContentColor = Color(0xFFB8C1D1)
    val DividerColor = Color.White.copy(alpha = 0.16f)

    val BorderBrush = Brush.linearGradient(
        colors = listOf(
            Color.White.copy(alpha = 0.24f),
            Color.White.copy(alpha = 0.06f)
        )
    )

    val SelectedBorderBrush = Brush.linearGradient(
        colors = listOf(
            Color.White.copy(alpha = 0.55f),
            Color.White.copy(alpha = 0.18f)
        )
    )

    val DarkGlassBlurStyle = HazeBlurStyle {
        blurRadius(24.dp)
        noiseFactor(0.10f)
        backgroundColor(Color(0xFF070A0F))
        colorEffects(
            listOf(
                HazeColorEffect.tint(Color(0xFF0D1119).copy(alpha = 0.56f))
            )
        )
        fallbackColorEffect(
            HazeColorEffect.tint(Color(0xFF0D1119).copy(alpha = 0.80f))
        )
    }

    val SelectedGlassBlurStyle = HazeBlurStyle {
        blurRadius(24.dp)
        noiseFactor(0.10f)
        backgroundColor(Color(0xFF141A24))
        colorEffects(
            listOf(
                HazeColorEffect.tint(Color(0xFF1B2330).copy(alpha = 0.50f)),
                HazeColorEffect.tint(Color.White.copy(alpha = 0.14f))
            )
        )
        fallbackColorEffect(
            HazeColorEffect.tint(Color(0xFF1F2735).copy(alpha = 0.88f))
        )
    }

    val glassListItemColors: ListItemColors
        @Composable get() = ListItemDefaults.colors(
            containerColor = Color.Transparent,
            headlineColor = PrimaryContentColor,
            leadingIconColor = PrimaryContentColor,
            trailingIconColor = SecondaryContentColor,
            supportingColor = SecondaryContentColor
        )

    val glassSwitchColors: SwitchColors
        @Composable get() = SwitchDefaults.colors(
            checkedThumbColor = colorScheme.onPrimary,
            checkedTrackColor = colorScheme.primary,
            checkedIconColor = colorScheme.primary,
            uncheckedThumbColor = SecondaryContentColor,
            uncheckedTrackColor = Color.White.copy(alpha = 0.10f),
            uncheckedBorderColor = Color.White.copy(alpha = 0.30f),
            uncheckedIconColor = Color(0xFF11161F)
        )
}

fun Modifier.frostedGlass(
    hazeState: HazeState?,
    shape: Shape,
    selected: Boolean = false
): Modifier {
    val clipped = this.clip(shape)
    val blurred = if (hazeState != null) {
        clipped.hazeBlur(
            input = HazeInput.Sources(hazeState),
            style = if (selected) GlassDefaults.SelectedGlassBlurStyle else GlassDefaults.DarkGlassBlurStyle
        )
    } else {
        clipped.background(
            if (selected) Color(0xCC222A38) else Color(0xCC0D1119)
        )
    }
    return blurred.border(
        width = if (selected) 1.25.dp else 1.dp,
        brush = if (selected) GlassDefaults.SelectedBorderBrush else GlassDefaults.BorderBrush,
        shape = shape
    )
}
