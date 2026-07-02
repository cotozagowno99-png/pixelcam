package com.pixelcam.app.ui.screens.camera

import android.graphics.Bitmap
import android.media.MediaActionSound
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pixelcam.app.camera.CameraController
import com.pixelcam.app.domain.model.AppSettings
import com.pixelcam.app.domain.model.LensFacing
import com.pixelcam.app.domain.model.PixelMode
import com.pixelcam.app.domain.model.PixelPalette
import com.pixelcam.app.domain.model.RenderParams
import com.pixelcam.app.domain.usecase.ObserveSettingsUseCase
import com.pixelcam.app.domain.usecase.SavePixelArtUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CameraUiState(
    val mode: PixelMode = PixelMode.MEDIUM,
    val palette: PixelPalette = PixelPalette.PASTEL,
    val params: RenderParams = RenderParams(),
    val lens: LensFacing = LensFacing.BACK,
    val torchOn: Boolean = false,
    val controlsVisible: Boolean = false,
    val palettePickerVisible: Boolean = false,
    val capturing: Boolean = false,
    val settings: AppSettings = AppSettings(),
    val initialized: Boolean = false
)

sealed interface CameraEvent {
    data class Saved(val count: Int) : CameraEvent
    data object SaveFailed : CameraEvent
    data object ShutterFlash : CameraEvent
}

@HiltViewModel
class CameraViewModel @Inject constructor(
    val cameraController: CameraController,
    private val observeSettings: ObserveSettingsUseCase,
    private val savePixelArt: SavePixelArtUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(CameraUiState())
    val state: StateFlow<CameraUiState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<CameraEvent>(extraBufferCapacity = 4)
    val events: SharedFlow<CameraEvent> = _events.asSharedFlow()

    private val shutterSound = MediaActionSound().apply { load(MediaActionSound.SHUTTER_CLICK) }

    init {
        viewModelScope.launch {
            // Apply persisted defaults once, then keep settings in sync.
            val initial = observeSettings().first()
            _state.update {
                it.copy(
                    settings = initial,
                    mode = initial.defaultMode,
                    palette = PixelPalette.byId(initial.defaultPaletteId),
                    initialized = true
                )
            }
            observeSettings().collect { s -> _state.update { it.copy(settings = s) } }
        }
    }

    fun setMode(mode: PixelMode) = _state.update { it.copy(mode = mode) }

    fun setPalette(palette: PixelPalette) = _state.update { it.copy(palette = palette) }

    fun updateParams(transform: (RenderParams) -> RenderParams) =
        _state.update { it.copy(params = transform(it.params)) }

    fun toggleControls() = _state.update {
        it.copy(controlsVisible = !it.controlsVisible, palettePickerVisible = false)
    }

    fun togglePalettePicker() = _state.update {
        it.copy(palettePickerVisible = !it.palettePickerVisible, controlsVisible = false)
    }

    fun toggleLens() = _state.update {
        it.copy(
            lens = if (it.lens == LensFacing.BACK) LensFacing.FRONT else LensFacing.BACK,
            torchOn = false
        )
    }

    fun toggleTorch() {
        val next = !_state.value.torchOn
        cameraController.setTorch(next)
        _state.update { it.copy(torchOn = next) }
    }

    /** Called by the screen with the exact rendered frame. */
    fun onCaptured(bitmap: Bitmap) {
        val s = _state.value
        if (s.settings.shutterSound) shutterSound.play(MediaActionSound.SHUTTER_CLICK)
        _events.tryEmit(CameraEvent.ShutterFlash)
        viewModelScope.launch {
            _state.update { it.copy(capturing = true) }
            val uris = savePixelArt(bitmap, s.settings.format)
            bitmap.recycle()
            _state.update { it.copy(capturing = false) }
            if (uris.isEmpty()) _events.tryEmit(CameraEvent.SaveFailed)
            else _events.tryEmit(CameraEvent.Saved(uris.size))
        }
    }

    override fun onCleared() {
        shutterSound.release()
        super.onCleared()
    }
}
