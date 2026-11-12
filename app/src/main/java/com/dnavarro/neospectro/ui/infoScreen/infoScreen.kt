package com.dnavarro.neospectro.ui.infoScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dnavarro.neospectro.R
import com.dnavarro.neospectro.ui.FormFactorPreviews
import com.dnavarro.neospectro.ui.mainScreen.MainViewModel
import com.dnavarro.neospectro.ui.theme.CustomColors.listItemColors
import com.dnavarro.neospectro.ui.theme.CustomColors.switchColors
import com.dnavarro.neospectro.ui.theme.NeospectroShapeDefaults.cardShape
import com.dnavarro.neospectro.ui.theme.NeospectroTheme

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun InfoScreen(
    contentPadding: PaddingValues,
    viewModel: MainViewModel = viewModel(
        factory = MainViewModel.Factory
    )
) {
    val uiState by viewModel.uiState.collectAsState()
    val isCustomThemeEnabled = uiState.isCustomThemeEnabled

    val colors = colorScheme
    val typo = typography
    val cardContainerColor = listItemColors.containerColor
    val cardCornerShape = cardShape
    val itemSwitchColors = switchColors
    val uriHandler = LocalUriHandler.current
    val layoutDirection = LocalLayoutDirection.current
    val gridContentPadding = PaddingValues(
        start = contentPadding.calculateStartPadding(layoutDirection) + 16.dp,
        top = contentPadding.calculateTopPadding(),
        end = contentPadding.calculateEndPadding(layoutDirection) + 16.dp,
        bottom = contentPadding.calculateBottomPadding() + 96.dp
    )

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 340.dp),
        contentPadding = gridContentPadding,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
            .fillMaxSize()
            .background(colors.surfaceDim)
    ) {
        item(
            key = 0,
            contentType = "theme_toggle_card",
            span = { GridItemSpan(maxLineSpan) }
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .background(cardContainerColor, cardCornerShape)
                    .clickable { viewModel.updateCustomThemeEnabled(!isCustomThemeEnabled) }
                    .padding(16.dp)
            ) {
                Icon(
                    painterResource(R.drawable.palette_outlined),
                    tint = colors.primary,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(Modifier.width(16.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        stringResource(R.string.custom_theming),
                        style = typo.titleMedium,
                        color = colors.onSurface
                    )
                    Text(
                        stringResource(R.string.custom_theming_desc),
                        style = typo.bodySmall,
                        color = colors.onSurfaceVariant
                    )
                }
                Switch(
                    checked = isCustomThemeEnabled,
                    onCheckedChange = { viewModel.updateCustomThemeEnabled(it) },
                    thumbContent = {
                        if (isCustomThemeEnabled) {
                            Icon(
                                painter = painterResource(R.drawable.check),
                                contentDescription = null,
                                modifier = Modifier.size(SwitchDefaults.IconSize),
                            )
                        } else {
                            Icon(
                                painter = painterResource(R.drawable.clear),
                                contentDescription = null,
                                modifier = Modifier.size(SwitchDefaults.IconSize),
                            )
                        }
                    },
                    colors = itemSwitchColors
                )
            }
        }
        item(
            key = 1,
            contentType = "info_card"
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .background(cardContainerColor, cardCornerShape)
                    .clickable(onClick = {})
                    .padding(16.dp)
            ) {
                Icon(
                    painterResource(R.drawable.ic_launcher_monochrome),
                    tint = colors.onPrimaryContainer,
                    contentDescription = null,
                    modifier = Modifier
                        .size(64.dp)
                        .background(
                            colors.primaryContainer,
                            MaterialShapes.Slanted.toShape()
                        )
                )
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(
                        stringResource(R.string.app_name),
                        color = colors.onSurface,
                        style = typo.titleLarge
                    )
                    Text(
                        stringResource(R.string.wallpaper_description),
                        style = typo.labelLarge,
                        color = colors.primary
                    )
                }
                Spacer(Modifier.weight(1f))
                FilledTonalIconButton(
                    onClick = { uriHandler.openUri("https://github.com/danielnavarrowo/neospectro") },
                    shapes = IconButtonDefaults.shapes()
                ) {
                    Icon(
                        painterResource(R.drawable.github),
                        contentDescription = "GitHub",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
        item(
            key = 2,
            contentType = "info_card"
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .background(cardContainerColor, cardCornerShape)
                    .clickable(onClick = {})
                    .padding(16.dp)
            ) {
                Icon(
                    painterResource(R.drawable.developer),
                    tint = colors.onSecondaryContainer,
                    contentDescription = null,
                    modifier = Modifier
                        .size(64.dp)
                        .background(
                            colors.secondaryContainer,
                            MaterialShapes.Pill.toShape()
                        )
                        .padding(8.dp)
                )
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(
                        stringResource(R.string.author),
                        style = typo.titleLarge,
                        color = colors.onSurface,
                    )
                    Text(
                        stringResource(R.string.developer),
                        style = typo.labelLarge,
                        color = colors.secondary
                    )
                }
                Spacer(Modifier.weight(1f))
            }
        }
        item(
            key = 3,
            contentType = "attribution_card"
        ) {
            Column(
                modifier = Modifier
                    .background(cardContainerColor, cardCornerShape)
                    .clickable(onClick = {})
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painterResource(R.drawable.attribution),
                        tint = colors.onTertiaryContainer,
                        contentDescription = null,
                        modifier = Modifier
                            .size(32.dp)
                            .background(
                                colors.tertiaryContainer,
                                MaterialShapes.Clover8Leaf.toShape()
                            )
                            .padding(6.dp)
                    )
                    Spacer(Modifier.width(16.dp))
                    Text(
                        stringResource(R.string.original_code_desc),
                        style = typo.labelMedium,
                        color = colors.tertiary
                    )
                    Spacer(Modifier.weight(1f))
                }
                Spacer(Modifier.height(8.dp))
                Row {
                    Spacer(Modifier.width((32 + 16).dp))
                    FilledTonalIconButton(
                        onClick = { uriHandler.openUri("http://www.apache.org/licenses/LICENSE-2.0") },
                        shapes = IconButtonDefaults.shapes()
                    ) {
                        Text("2.0", style = typo.labelSmall)
                    }
                    Spacer(Modifier.width(4.dp))
                    FilledTonalIconButton(
                        onClick = { uriHandler.openUri("https://android.googlesource.com/platform/packages/wallpapers/MusicVisualization") },
                        shapes = IconButtonDefaults.shapes()
                    ) {
                        Text("AOSP", style = typo.labelSmall)
                    }
                }
            }
        }
    }
}

@FormFactorPreviews
@Composable
fun InfoScreenPreview() {
    NeospectroTheme {
        Surface {
            InfoScreen(
                contentPadding = PaddingValues()
            )
        }
    }
}