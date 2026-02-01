package com.dnavarro.neospectro.ui

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

sealed class Screen : NavKey {
    @Serializable
    object Main : Screen()

    @Serializable
    object Info : Screen()

}

data class NavItem(
    val route: Screen,
    @param:DrawableRes val unselectedIcon: Int,
    @param:DrawableRes val selectedIcon: Int,
    @param:StringRes val label: Int
)

