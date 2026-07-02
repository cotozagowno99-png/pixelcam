package com.pixelcam.app.data.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.pixelcam.app.domain.model.AppSettings
import com.pixelcam.app.domain.model.OutputFormat
import com.pixelcam.app.domain.model.OutputQuality
import com.pixelcam.app.domain.model.PixelMode
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "pixelcam_settings")

@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val PALETTE = stringPreferencesKey("default_palette")
        val MODE = stringPreferencesKey("default_mode")
        val QUALITY = stringPreferencesKey("quality")
        val FORMAT = stringPreferencesKey("format")
        val AUTO_SAVE = booleanPreferencesKey("auto_save")
        val DARK_PIXEL = booleanPreferencesKey("dark_pixel_mode")
        val HAPTICS = booleanPreferencesKey("haptics")
        val SHUTTER_SOUND = booleanPreferencesKey("shutter_sound")
    }

    val settings: Flow<AppSettings> = context.dataStore.data.map { p ->
        AppSettings(
            defaultPaletteId = p[Keys.PALETTE] ?: AppSettings().defaultPaletteId,
            defaultMode = enumOr(p[Keys.MODE], PixelMode.MEDIUM),
            quality = enumOr(p[Keys.QUALITY], OutputQuality.HIGH),
            format = enumOr(p[Keys.FORMAT], OutputFormat.BOTH),
            autoSave = p[Keys.AUTO_SAVE] ?: true,
            darkPixelMode = p[Keys.DARK_PIXEL] ?: false,
            hapticFeedback = p[Keys.HAPTICS] ?: true,
            shutterSound = p[Keys.SHUTTER_SOUND] ?: true
        )
    }

    suspend fun update(transform: (AppSettings) -> AppSettings) {
        context.dataStore.edit { p ->
            val current = AppSettings(
                defaultPaletteId = p[Keys.PALETTE] ?: AppSettings().defaultPaletteId,
                defaultMode = enumOr(p[Keys.MODE], PixelMode.MEDIUM),
                quality = enumOr(p[Keys.QUALITY], OutputQuality.HIGH),
                format = enumOr(p[Keys.FORMAT], OutputFormat.BOTH),
                autoSave = p[Keys.AUTO_SAVE] ?: true,
                darkPixelMode = p[Keys.DARK_PIXEL] ?: false,
                hapticFeedback = p[Keys.HAPTICS] ?: true,
                shutterSound = p[Keys.SHUTTER_SOUND] ?: true
            )
            val next = transform(current)
            p[Keys.PALETTE] = next.defaultPaletteId
            p[Keys.MODE] = next.defaultMode.name
            p[Keys.QUALITY] = next.quality.name
            p[Keys.FORMAT] = next.format.name
            p[Keys.AUTO_SAVE] = next.autoSave
            p[Keys.DARK_PIXEL] = next.darkPixelMode
            p[Keys.HAPTICS] = next.hapticFeedback
            p[Keys.SHUTTER_SOUND] = next.shutterSound
        }
    }

    private inline fun <reified T : Enum<T>> enumOr(name: String?, fallback: T): T =
        name?.let { runCatching { enumValueOf<T>(it) }.getOrNull() } ?: fallback
}
