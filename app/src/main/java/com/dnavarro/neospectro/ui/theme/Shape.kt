package com.dnavarro.neospectro.ui.theme

import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.runtime.Composable

object NeospectroShapeDefaults {
    val topListItemShape: RoundedCornerShape
        @Composable get() =
            RoundedCornerShape(
                topStart = shapes.largeIncreased.topStart,
                topEnd = shapes.largeIncreased.topEnd,
                bottomStart = shapes.extraSmall.bottomStart,
                bottomEnd = shapes.extraSmall.bottomStart
            )

    //val middleListItemShape: RoundedCornerShape
      //  @Composable get() = RoundedCornerShape(shapes.extraSmall.topStart)

    val bottomListItemShape: RoundedCornerShape
        @Composable get() =
            RoundedCornerShape(
                topStart = shapes.extraSmall.topStart,
                topEnd = shapes.extraSmall.topEnd,
                bottomStart = shapes.largeIncreased.bottomStart,
                bottomEnd = shapes.largeIncreased.bottomEnd
            )

    val cardShape: CornerBasedShape
        @Composable get() = shapes.largeIncreased
}