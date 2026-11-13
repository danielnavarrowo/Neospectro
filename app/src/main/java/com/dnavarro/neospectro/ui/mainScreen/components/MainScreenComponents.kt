package com.dnavarro.neospectro.ui.mainScreen.components

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.dnavarro.neospectro.Constants
import com.dnavarro.neospectro.R
import com.dnavarro.neospectro.data.ThemeRepository
import com.dnavarro.neospectro.data.WaveTheme
import com.dnavarro.neospectro.ui.theme.GlassDefaults
import com.dnavarro.neospectro.ui.theme.frostedGlass
import dev.chrisbanes.haze.HazeState

@StringRes
private fun themeLabelRes(themeId: String): Int = when (themeId) {
    Constants.THEME_ICE -> R.string.ice
    Constants.THEME_ACID -> R.string.acid
    Constants.THEME_FIRE -> R.string.fire
    Constants.THEME_PINK -> R.string.pink
    Constants.THEME_CYAN -> R.string.cyan
    Constants.THEME_YELLOW -> R.string.yellow
    Constants.THEME_PURPLE -> R.string.purple
    Constants.THEME_CUSTOM -> R.string.custom
    else -> R.string.ice
}

/**
 * Horizontal frosted-glass pill strip for picking preset themes or opening the custom color picker.
 */
@Composable
fun ThemeSelectorStrip(
    selectedTheme: String,
    isCustomThemeEnabled: Boolean,
    reverseColors: Boolean,
    customEdgeColor: Int,
    customMiddleColor: Int,
    customCenterColor: Int,
    hazeState: HazeState?,
    onPresetSelected: (String) -> Unit,
    onCustomClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp)
) {
    val customTheme = remember(customEdgeColor, customMiddleColor, customCenterColor) {
        WaveTheme(
            id = Constants.THEME_CUSTOM,
            edgeColor = customEdgeColor,
            middleColor = customMiddleColor,
            centerColor = customCenterColor
        )
    }
    val allThemes = remember(customTheme) {
        ThemeRepository.themes + customTheme
    }

    val selectedIndex = remember(allThemes, selectedTheme, isCustomThemeEnabled) {
        if (isCustomThemeEnabled) {
            allThemes.lastIndex
        } else {
            allThemes.indexOfFirst { it.id == selectedTheme }.coerceAtLeast(0)
        }
    }

    val listState = rememberLazyListState(initialFirstVisibleItemIndex = selectedIndex)

    LaunchedEffect(selectedIndex) {
        val visibleItems = listState.layoutInfo.visibleItemsInfo
        val isFullyVisible = visibleItems.any { item ->
            item.index == selectedIndex &&
                item.offset >= listState.layoutInfo.viewportStartOffset &&
                item.offset + item.size <= listState.layoutInfo.viewportEndOffset
        }
        if (!isFullyVisible) {
            listState.animateScrollToItem(selectedIndex)
        }
    }

    LazyRow(
        state = listState,
        modifier = modifier.fillMaxWidth(),
        contentPadding = contentPadding,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        itemsIndexed(
            items = allThemes,
            key = { _, item -> item.id }
        ) { _, item ->
            val isCustom = item.id == Constants.THEME_CUSTOM
            val isSelected = if (isCustom) {
                isCustomThemeEnabled
            } else {
                !isCustomThemeEnabled && item.id == selectedTheme
            }
            val edge = if (reverseColors) item.centerColor else item.edgeColor
            val center = if (reverseColors) item.edgeColor else item.centerColor

            ThemePill(
                label = stringResource(themeLabelRes(item.id)),
                edgeColor = edge,
                middleColor = item.middleColor,
                centerColor = center,
                isCustom = isCustom,
                selected = isSelected,
                hazeState = hazeState,
                onClick = {
                    if (isCustom) {
                        onCustomClick()
                    } else {
                        onPresetSelected(item.id)
                    }
                }
            )
        }
    }
}

@Composable
private fun ThemePill(
    label: String,
    edgeColor: Int,
    middleColor: Int,
    centerColor: Int,
    isCustom: Boolean,
    selected: Boolean,
    hazeState: HazeState?,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .frostedGlass(
                hazeState = hazeState,
                shape = CircleShape,
                selected = selected
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .drawWithCache {
                    val brush = Brush.verticalGradient(
                        listOf(
                            Color(edgeColor),
                            Color(middleColor),
                            Color(centerColor),
                            Color(middleColor),
                            Color(edgeColor)
                        )
                    )
                    onDrawBehind {
                        drawRect(brush)
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            if (isCustom) {
                Icon(
                    painter = painterResource(R.drawable.palette_filled),
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.9f),
                    modifier = Modifier.size(12.dp)
                )
            }
        }

        Text(
            text = label,
            color = if (selected) {
                GlassDefaults.PrimaryContentColor
            } else {
                GlassDefaults.SecondaryContentColor
            },
            style = MaterialTheme.typography.labelLarge,
            maxLines = 1
        )
    }
}
