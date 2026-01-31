package com.dnavarro.neospectro.ui.mainScreen.components

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.carousel.HorizontalCenteredHeroCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.dnavarro.neospectro.Constants
import com.dnavarro.neospectro.R
import com.dnavarro.neospectro.ui.theme.NeospectroShapeDefaults.topListItemShape
import kotlinx.coroutines.delay

@Composable
fun SelectThemeListItem(
    modifier: Modifier = Modifier,
    selectedTheme: String,
    onThemeSelected: (String) -> Unit
) {
    Column(
        modifier
            .clip(topListItemShape),
    ) {
        ListItem(
            leadingContent = {
                    Icon(
                        painter = painterResource(R.drawable.palette),
                        contentDescription = null,
                    )
            },
            headlineContent = { Text(stringResource(R.string.select_theme)) },
        )
        ThemesCarousel(selectedTheme, onThemeSelected)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemesCarousel(
    selectedTheme: String,
    onThemeSelected: (String) -> Unit = {}
) {
    data class CarouselItem(
        val id: Int,
        @DrawableRes val imageResId: Int,
        val contentDescription: String,
        val theme: String
    )

    val items = remember {
        listOf(
            CarouselItem(0, R.drawable.vis3, "Ice", Constants.THEME_ICE),
            CarouselItem(1, R.drawable.vis3, "Acid", Constants.THEME_ACID),
            CarouselItem(2, R.drawable.vis3, "Fire", Constants.THEME_FIRE),
        )
    }

    val initialIndex = remember(selectedTheme) {
        val index = items.indexOfFirst { it.theme == selectedTheme }
        if (index >= 0) index else 0
    }

    val state = rememberCarouselState(initialItem = initialIndex) { items.count() }

    LaunchedEffect(state.currentItem) {
        delay(1000)
        onThemeSelected(items[state.currentItem].theme)
    }

    HorizontalCenteredHeroCarousel(
        state = state,
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(top = 16.dp, bottom = 16.dp),
        itemSpacing = 8.dp,
    ) { i ->
        val item = items[i]
        Box(
            modifier = Modifier
                .height(256.dp)
                .fillMaxWidth()
                .maskClip(MaterialTheme.shapes.extraLarge)
        ) {
            Image(
                modifier = Modifier.matchParentSize(),
                painter = painterResource(id = item.imageResId),
                contentDescription = item.contentDescription,
                contentScale = ContentScale.Crop
            )
            AnimatedVisibility(
                visible = i == state.currentItem,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Text(
                    text = item.contentDescription,
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}