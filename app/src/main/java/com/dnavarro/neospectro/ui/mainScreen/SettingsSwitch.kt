package com.dnavarro.neospectro.ui.mainScreen

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

data class SettingsSwitch(
    val checked: Boolean,
    val enabled: Boolean = true,
    val collapsible: Boolean = false,
    @param:DrawableRes val icon: Int,
    @param:StringRes val label: Int,
    @param:StringRes val description: Int,
    val onClick: (Boolean) -> Unit
)