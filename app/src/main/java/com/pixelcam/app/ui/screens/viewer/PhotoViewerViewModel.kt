package com.pixelcam.app.ui.screens.viewer

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pixelcam.app.domain.usecase.ObserveSettingsUseCase
import com.pixelcam.app.domain.usecase.SavePixelArtUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PhotoViewerViewModel @Inject constructor(
    private val savePixelArt: SavePixelArtUseCase,
    private val observeSettings: ObserveSettingsUseCase
) : ViewModel() {

    /** "Save again" - writes a fresh copy in the user's preferred format(s). */
    fun saveAgain(bitmap: Bitmap) {
        viewModelScope.launch {
            val settings = observeSettings().first()
            savePixelArt(bitmap, settings.format)
            bitmap.recycle()
        }
    }
}
