package com.dnavarro.neospectro.ui.mainScreen

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.dnavarro.neospectro.data.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    companion object {
        fun provideFactory(context: Context): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return MainViewModel(SettingsRepository.getInstance(context.applicationContext)) as T
            }
        }
    }

    val uiState: StateFlow<MainUiState> = combine(
        settingsRepository.selectedTheme,
        settingsRepository.reverseColors,
        settingsRepository.audioVizEnabled,
        settingsRepository.hasBgImage,
        settingsRepository.bgImageTrigger,
        settingsRepository.isCustomThemeEnabled,
        settingsRepository.customEdgeColor,
        settingsRepository.customMiddleColor,
        settingsRepository.customCenterColor
    ) { args: Array<Any> ->
        MainUiState(
            selectedTheme = args[0] as String,
            reverseColors = args[1] as Boolean,
            audioVizEnabled = args[2] as Boolean,
            hasBgImage = args[3] as Boolean,
            bgImageTrigger = args[4] as Int,
            isCustomThemeEnabled = args[5] as Boolean,
            customEdgeColor = args[6] as Int,
            customMiddleColor = args[7] as Int,
            customCenterColor = args[8] as Int
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MainUiState(
            selectedTheme = settingsRepository.getTheme(),
            reverseColors = settingsRepository.isReverseColors(),
            audioVizEnabled = settingsRepository.isAudioVizEnabled(),
            hasBgImage = settingsRepository.hasBgImage(),
            isCustomThemeEnabled = settingsRepository.isCustomThemeEnabled(),
            customEdgeColor = settingsRepository.getCustomEdgeColor(),
            customMiddleColor = settingsRepository.getCustomMiddleColor(),
            customCenterColor = settingsRepository.getCustomCenterColor()
        )
    )

    fun updateCustomThemeEnabled(enabled: Boolean) {
        settingsRepository.updateCustomThemeEnabled(enabled)
    }

    fun updateCustomColors(edge: Int, middle: Int, center: Int) {
        settingsRepository.updateCustomColors(edge, middle, center)
    }

    fun updateTheme(theme: String) {
        settingsRepository.updateTheme(theme)
    }

    fun updateReverseColors(reverse: Boolean) {
        settingsRepository.updateReverseColors(reverse)
    }

    fun updateAudioVizEnabled(enabled: Boolean) {
        settingsRepository.updateAudioVizEnabled(enabled)
    }

    fun updateHasBgImage(hasImage: Boolean) {
        settingsRepository.updateHasBgImage(hasImage)
    }

    fun handleBgImagePicked(uri: Uri?) {
        if (uri != null) {
            viewModelScope.launch {
                settingsRepository.saveBackgroundImage(uri)
            }
        }
    }
}
