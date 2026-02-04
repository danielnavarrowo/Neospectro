package com.dnavarro.neospectro.ui.infoScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.dnavarro.neospectro.R
import com.dnavarro.neospectro.ui.theme.CustomColors.listItemColors
import com.dnavarro.neospectro.ui.theme.NeospectroShapeDefaults.bottomListItemShape
import com.dnavarro.neospectro.ui.theme.NeospectroShapeDefaults.cardShape
import com.dnavarro.neospectro.ui.theme.NeospectroShapeDefaults.middleListItemShape
import com.dnavarro.neospectro.ui.theme.NeospectroShapeDefaults.topListItemShape

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun InfoScreen (contentPadding: PaddingValues) {
    Scaffold(
        topBar = { Spacer(Modifier.height(contentPadding.calculateTopPadding())) },
        bottomBar = { Spacer(Modifier.height(contentPadding.calculateBottomPadding())) },
        containerColor = colorScheme.surfaceDim,

        ) { innerPadding ->
        val uriHandler = LocalUriHandler.current
        LazyColumn(contentPadding = innerPadding,
            verticalArrangement = Arrangement.spacedBy(2.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)) {
            item {
                Box(Modifier.background(listItemColors.containerColor, topListItemShape).clickable(onClick = {})) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Icon(
                            painterResource(R.drawable.ic_launcher_monochrome),
                            tint = colorScheme.onPrimaryContainer,
                            contentDescription = null,
                            modifier = Modifier
                                .size(64.dp)
                                .background(
                                    colorScheme.primaryContainer,
                                    MaterialShapes.Slanted.toShape()
                                )
                        )
                        Spacer(Modifier.width(16.dp))
                        Column {
                            Text(
                                stringResource(R.string.app_name),
                                color = colorScheme.onSurface,
                                style = typography.titleLarge
                            )
                            Text(
                                stringResource(R.string.wallpaper_description),
                                style = typography.labelLarge,
                                color = colorScheme.primary
                            )
                        }
                        Spacer(Modifier.weight(1f))
                        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {

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
                }
            }
            item {
                Box(Modifier.background(listItemColors.containerColor, bottomListItemShape).clickable(onClick = {})) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                painterResource(R.drawable.developer),
                                tint = colorScheme.onSecondaryContainer,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(64.dp)
                                    .background(
                                        colorScheme.secondaryContainer,
                                        MaterialShapes.Pill.toShape()
                                    )
                                    .padding(8.dp)
                            )
                            Spacer(Modifier.width(16.dp))
                            Column {
                                Text(
                                    stringResource(R.string.author),
                                    style = typography.titleLarge,
                                    color = colorScheme.onSurface,
                                )
                                Text(
                                    "Developer",
                                    style = typography.labelLarge,
                                    color = colorScheme.secondary
                                )
                            }
                            Spacer(Modifier.weight(1f))
                        }
                        Spacer(Modifier.height(8.dp))
                        Row {
                            Spacer(Modifier.width((64 + 16).dp))

                        }
                    }
                }
            }



            item {
                Spacer(Modifier.height(32.dp))
                Box(Modifier.background(listItemColors.containerColor, cardShape).clickable(onClick = {})) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                painterResource(R.drawable.attribution),
                                tint = colorScheme.onTertiaryContainer,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(
                                        colorScheme.tertiaryContainer,
                                        MaterialShapes.Clover8Leaf.toShape()
                                    )
                                    .padding(6.dp)
                            )
                            Spacer(Modifier.width(16.dp))
                            Column {
                                Text(
                                    stringResource(R.string.original_code_desc),
                                    style = typography.labelMedium,
                                    color = colorScheme.tertiary
                                )
                            }
                            Spacer(Modifier.weight(1f))
                        }
                        Spacer(Modifier.height(8.dp))
                        Row {
                            Spacer(Modifier.width((32+16).dp))
                            FilledTonalIconButton(
                                onClick = { uriHandler.openUri("http://www.apache.org/licenses/LICENSE-2.0") },
                                shapes = IconButtonDefaults.shapes()
                            ) {
                                Text("2.0", style = typography.labelSmall)
                            }
                            Spacer(Modifier.width(4.dp))
                            FilledTonalIconButton(
                                onClick = { uriHandler.openUri("https://android.googlesource.com/platform/packages/wallpapers/MusicVisualization") },
                                shapes = IconButtonDefaults.shapes()
                            ) {
                                Text("AOSP", style = typography.labelSmall)
                            }
                        }
                    }
                }
            }
        }
    }
}