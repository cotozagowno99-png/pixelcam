package com.pixelcam.app.domain.usecase

import android.graphics.Bitmap
import android.net.Uri
import com.pixelcam.app.data.photo.PhotoRepository
import com.pixelcam.app.data.settings.SettingsRepository
import com.pixelcam.app.domain.model.AppSettings
import com.pixelcam.app.domain.model.OutputFormat
import com.pixelcam.app.domain.model.PixelPhoto
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/** Saves a rendered Pixel Art bitmap in the requested format(s). */
class SavePixelArtUseCase @Inject constructor(
    private val photoRepository: PhotoRepository
) {
    suspend operator fun invoke(bitmap: Bitmap, format: OutputFormat): List<Uri> =
        photoRepository.save(bitmap, format)
}

/** Streams the Pixel Art gallery, newest first. */
class GetPhotosUseCase @Inject constructor(
    private val photoRepository: PhotoRepository
) {
    suspend operator fun invoke(): List<PixelPhoto> = photoRepository.listPhotos()
}

/** Deletes a Pixel Art photo. */
class DeletePhotoUseCase @Inject constructor(
    private val photoRepository: PhotoRepository
) {
    suspend operator fun invoke(photo: PixelPhoto): Boolean = photoRepository.delete(photo)
}

/** Observes persisted user settings. */
class ObserveSettingsUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    operator fun invoke(): Flow<AppSettings> = settingsRepository.settings
}

/** Applies a settings mutation. */
class UpdateSettingsUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke(transform: (AppSettings) -> AppSettings) =
        settingsRepository.update(transform)
}
