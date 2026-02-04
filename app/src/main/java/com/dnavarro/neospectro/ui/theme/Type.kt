package com.dnavarro.neospectro.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.unit.sp
import com.dnavarro.neospectro.R

val TYPOGRAPHY = Typography()

@OptIn(ExperimentalTextApi::class)
val googleFlexDisplay = FontFamily(
    Font(
        R.font.googlesansflex,
        variationSettings = FontVariation.Settings(
            FontVariation.weight(300),
            FontVariation.width(140f),
            FontVariation.slant(-7f),
            FontVariation.grade(100),
        )
    )
)

@OptIn(ExperimentalTextApi::class)
val googleFlexHeadline = FontFamily(
    Font(
        R.font.googlesansflex,
        variationSettings = FontVariation.Settings(
            FontVariation.weight(200),
            FontVariation.width(120f),
            FontVariation.slant(-5f),
            FontVariation.grade(50),
        )
    )
)

@OptIn(ExperimentalTextApi::class)
val googleFlexTitle = FontFamily(
    Font(
        R.font.googlesansflex,
        variationSettings = FontVariation.Settings(
            FontVariation.weight(600),
            FontVariation.width(95f),
            FontVariation.slant(0f),
            FontVariation.grade(50)
        )
    )
)

@OptIn(ExperimentalTextApi::class)
val googleFlexBody = FontFamily(
    Font(
        R.font.googlesansflex,
        variationSettings = FontVariation.Settings(
            FontVariation.weight(450),      // Regular
            FontVariation.width(100f),
            FontVariation.opticalSizing(16.sp)
        )
    )
)

@OptIn(ExperimentalTextApi::class)
val googleFlexLabel = FontFamily(
    Font(
        R.font.googlesansflex,
        variationSettings = FontVariation.Settings(
            FontVariation.weight(500),      // Medium
            FontVariation.width(95f),       // Slightly condensed for labels
            FontVariation.grade(10)
        )
    )
)

val AppTypography = Typography(
    displayLarge = TYPOGRAPHY.displayLarge.copy(
        fontFamily = googleFlexDisplay,
    ),
    displayMedium = TYPOGRAPHY.displayMedium.copy(
        fontFamily = googleFlexDisplay,
    ),
    displaySmall = TYPOGRAPHY.displaySmall.copy(
        fontFamily = googleFlexDisplay,
    ),
    headlineLarge = TYPOGRAPHY.headlineLarge.copy(
        fontFamily = googleFlexHeadline,
    ),
    headlineMedium = TYPOGRAPHY.headlineMedium.copy(
        fontFamily = googleFlexHeadline,
    ),
    headlineSmall = TYPOGRAPHY.headlineSmall.copy(
        fontFamily = googleFlexHeadline,
    ),
    titleLarge = TYPOGRAPHY.titleLarge.copy(
        fontFamily = googleFlexTitle,
    ),
    titleMedium = TYPOGRAPHY.titleMedium.copy(
        fontFamily = googleFlexTitle,
    ),
    titleSmall = TYPOGRAPHY.titleSmall.copy(
        fontFamily = googleFlexTitle,
    ),
    bodyLarge = TYPOGRAPHY.bodyLarge.copy(
        fontFamily = googleFlexBody,
    ),
    bodyMedium = TYPOGRAPHY.bodyMedium.copy(
        fontFamily = googleFlexBody,
    ),
    bodySmall = TYPOGRAPHY.bodySmall.copy(
        fontFamily = googleFlexBody,
    ),
    labelLarge = TYPOGRAPHY.labelLarge.copy(
        fontFamily = googleFlexLabel,
    ),
    labelMedium = TYPOGRAPHY.labelMedium.copy(
        fontFamily = googleFlexLabel,
    ),
    labelSmall = TYPOGRAPHY.labelSmall.copy(
        fontFamily = googleFlexLabel,
    )
)
