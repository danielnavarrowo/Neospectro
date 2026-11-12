package com.dnavarro.neospectro.ui.mainScreen.components

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.material3.carousel.HorizontalCenteredHeroCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.dnavarro.neospectro.Constants
import com.dnavarro.neospectro.R
import com.dnavarro.neospectro.ui.theme.CustomColors.listItemColors
import com.dnavarro.neospectro.ui.theme.NeospectroShapeDefaults.cardShape
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.withContext
import kotlin.time.Duration.Companion.milliseconds

private data class CarouselItem(
    val id: Int,
    @param:DrawableRes val imageResId: Int,
    @param:StringRes val contentDescriptionRes: Int,
    val theme: String
)

private val CAROUSEL_ITEMS = listOf(
    CarouselItem(0, R.drawable.ice, R.string.ice, Constants.THEME_ICE),
    CarouselItem(1, R.drawable.acid, R.string.acid, Constants.THEME_ACID),
    CarouselItem(2, R.drawable.fire, R.string.fire, Constants.THEME_FIRE),
    CarouselItem(3, R.drawable.pink, R.string.pink, Constants.THEME_PINK),
    CarouselItem(4, R.drawable.cyan, R.string.cyan, Constants.THEME_CYAN),
    CarouselItem(5, R.drawable.yellow, R.string.yellow, Constants.THEME_YELLOW),
    CarouselItem(6, R.drawable.purple, R.string.purple, Constants.THEME_PURPLE)
)

private fun decodeSampledBitmapFromResource(
    res: Resources,
    @DrawableRes resId: Int,
    reqWidth: Int,
    reqHeight: Int
): Bitmap? {
    val options = BitmapFactory.Options().apply {
        inJustDecodeBounds = true
    }
    BitmapFactory.decodeResource(res, resId, options)

    val [height: Int, width: Int] = options.run { outHeight to outWidth }
    var inSampleSize = 1
    if (height > reqHeight || width > reqWidth) {
        val halfHeight: Int = height / 2
        val halfWidth: Int = width / 2
        while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
            inSampleSize *= 2
        }
    }

    options.inSampleSize = inSampleSize
    options.inJustDecodeBounds = false
    return BitmapFactory.decodeResource(res, resId, options)
}

@Composable
fun SelectThemeListItem(
    modifier: Modifier = Modifier,
    selectedTheme: String,
    onThemeSelected: (String) -> Unit
) {
    Column(
        modifier
            .clip(cardShape),
    ) {
        ListItem(
            modifier = Modifier,
            leadingContent = {
                Icon(
                    painter = painterResource(R.drawable.palette_outlined),
                    contentDescription = null,
                )
            },
            supportingContent = { Text(stringResource(R.string.choose_theme_desc)) },
            colors = listItemColors,
            content = {
                Text(
                    stringResource(R.string.choose_theme),
                    style = MaterialTheme.typography.titleMedium
                )
            },
        )
        ThemesCarousel(selectedTheme, onThemeSelected)
    }
}

@Composable
fun CustomThemeListItem(
    modifier: Modifier = Modifier,
    edgeColor: Int,
    middleColor: Int,
    centerColor: Int,
    onEditColorsClick: () -> Unit
) {
    Column(
        modifier
            .clip(cardShape)
            .background(color = colorScheme.surface)
    ) {
        ListItem(
            modifier = Modifier,
            leadingContent = {
                Icon(
                    painter = painterResource(R.drawable.palette_outlined),
                    contentDescription = null,
                )
            },
            supportingContent = { Text(stringResource(R.string.configure_custom_colors_desc)) },
            colors = listItemColors,
            content = {
                Text(
                    stringResource(R.string.custom_theming),
                    style = MaterialTheme.typography.titleMedium
                )
            },
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(cardShape)
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
                    }
            )
            Button(
                onClick = onEditColorsClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.edit_custom_colors))
            }
        }
    }
}

@Composable
fun ThemesCarousel(
    selectedTheme: String,
    onThemeSelected: (String) -> Unit = {},
) {
    val context = LocalContext.current
    val resources = context.resources
    val density = LocalDensity.current
    val targetSizePx = with(density) { 192.dp.roundToPx() }

    val initialIndex = remember(selectedTheme) {
        val index = CAROUSEL_ITEMS.indexOfFirst { it.theme == selectedTheme }
        if (index >= 0) index else 0
    }

    val state = rememberCarouselState(initialItem = initialIndex) { CAROUSEL_ITEMS.size }
    val currentOnThemeSelected by rememberUpdatedState(onThemeSelected)
    val currentSelectedTheme by rememberUpdatedState(selectedTheme)

    LaunchedEffect(state) {
        snapshotFlow { state.currentItem }
            .collectLatest { itemIndex ->
                val theme = CAROUSEL_ITEMS[itemIndex].theme
                if (theme != currentSelectedTheme) {
                    delay(1000.milliseconds)
                    currentOnThemeSelected(theme)
                }
            }
    }

    HorizontalCenteredHeroCarousel(
        state = state,
        modifier = Modifier
            .background(colorScheme.surfaceBright)
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(16.dp),
        itemSpacing = 8.dp,
    ) { i ->
        val item = CAROUSEL_ITEMS[i]
        val isCurrentItem by remember(i, state) {
            derivedStateOf { i == state.currentItem }
        }
        val bitmap by produceState<ImageBitmap?>(
            initialValue = null,
            item.imageResId,
            resources,
            targetSizePx
        ) {
            value = withContext(Dispatchers.IO) {
                decodeSampledBitmapFromResource(
                    res = resources,
                    resId = item.imageResId,
                    reqWidth = targetSizePx,
                    reqHeight = targetSizePx
                )?.asImageBitmap()
            }
        }
        val contentDescription = stringResource(item.contentDescriptionRes)

        Box(
            modifier = Modifier
                .height(192.dp)
                .fillMaxWidth()
                .maskClip(MaterialTheme.shapes.extraLarge)
        ) {
            val currentBitmap = bitmap
            if (currentBitmap != null) {
                Image(
                    modifier = Modifier.matchParentSize(),
                    bitmap = currentBitmap,
                    contentDescription = contentDescription,
                    contentScale = ContentScale.Crop
                )
            }
            AnimatedVisibility(
                visible = isCurrentItem,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Text(
                    text = contentDescription,
                    color = Color.White,
                    style = MaterialTheme.typography.headlineSmall
                )
            }
        }
    }
}


