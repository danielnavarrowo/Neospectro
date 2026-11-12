package com.dnavarro.neospectro.ui.theme

import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

object NeospectroShapeDefaults {
    val topListItemShape: RoundedCornerShape
        @Composable get() {
            val currentShapes = shapes
            return remember(currentShapes) {
                RoundedCornerShape(
                    topStart = currentShapes.largeIncreased.topStart,
                    topEnd = currentShapes.largeIncreased.topEnd,
                    bottomStart = currentShapes.extraSmall.bottomStart,
                    bottomEnd = currentShapes.extraSmall.bottomEnd
                )
            }
        }

    //val middleListItemShape: RoundedCornerShape
      //  @Composable get() = RoundedCornerShape(shapes.extraSmall.topStart)

    val bottomListItemShape: RoundedCornerShape
        @Composable get() {
            val currentShapes = shapes
            return remember(currentShapes) {
                RoundedCornerShape(
                    topStart = currentShapes.extraSmall.topStart,
                    topEnd = currentShapes.extraSmall.topEnd,
                    bottomStart = currentShapes.largeIncreased.bottomStart,
                    bottomEnd = currentShapes.largeIncreased.bottomEnd
                )
            }
        }

    val cardShape: CornerBasedShape
        @Composable get() = shapes.largeIncreased
}