package com.dnavarro.neospectro.ui.mainScreen

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.dnavarro.neospectro.data.SettingsRepository
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MainViewModel(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val appContext = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]!!
                MainViewModel(SettingsRepository.getInstance(appContext))
            }
        }

    }

    val uiState: StateFlow<MainUiState> = settingsRepository.uiState

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
