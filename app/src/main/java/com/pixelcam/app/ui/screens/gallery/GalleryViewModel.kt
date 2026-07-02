package com.pixelcam.app.ui.screens.gallery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pixelcam.app.domain.model.PixelPhoto
import com.pixelcam.app.domain.usecase.DeletePhotoUseCase
import com.pixelcam.app.domain.usecase.GetPhotosUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class GalleryUiState(
    val photos: List<PixelPhoto> = emptyList(),
    val loading: Boolean = true
)

@HiltViewModel
class GalleryViewModel @Inject constructor(
    private val getPhotos: GetPhotosUseCase,
    private val deletePhoto: DeletePhotoUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(GalleryUiState())
    val state: StateFlow<GalleryUiState> = _state.asStateFlow()

    fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true) }
            val photos = getPhotos()
            _state.update { GalleryUiState(photos = photos, loading = false) }
        }
    }

    fun delete(photo: PixelPhoto) {
        viewModelScope.launch {
            if (deletePhoto(photo)) {
                _state.update { s -> s.copy(photos = s.photos.filterNot { it.id == photo.id }) }
            }
        }
    }

    fun photoById(id: Long): PixelPhoto? = _state.value.photos.firstOrNull { it.id == id }
}
