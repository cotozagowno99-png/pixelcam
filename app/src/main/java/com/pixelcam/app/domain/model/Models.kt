package com.pixelcam.app.domain.model

import android.net.Uri

/** Camera-facing lens. */
enum class LensFacing { BACK, FRONT }

/** Output file format for captured Pixel Art. */
enum class OutputFormat(val label: String) { PNG("PNG"), JPEG("JPEG"), BOTH("PNG + JPEG") }

/** Output quality preset (controls capture buffer resolution multiplier). */
enum class OutputQuality(val label: String, val scale: Int) {
    STANDARD("Standard", 4),
    HIGH("High", 8),
    ULTRA("Ultra", 12)
}

/** Live rendering mode. */
enum class PixelMode(val label: String, val defaultGrid: Int, val is3D: Boolean) {
    SMALL("Small Pixel", 128, false),
    MEDIUM("Medium Pixel", 64, false),
    VOXEL("3D Pixel", 48, true)
}

/** All tweakable live-render parameters (normalized ranges). */
data class RenderParams(
    val pixelSize: Float = 0.5f,        // 0..1 -> grid density inside the mode range
    val paletteStrength: Float = 1f,    // 0..1
    val contrast: Float = 0.5f,         // 0..1 (0.5 = neutral)
    val brightness: Float = 0.5f,       // 0..1 (0.5 = neutral)
    val saturation: Float = 0.5f,       // 0..1 (0.5 = neutral)
    val sharpness: Float = 0.25f,       // 0..1
    val edgeStrength: Float = 0f,       // 0..1
    val depth3D: Float = 0.6f,          // 0..1
    val shadow3D: Float = 0.5f,         // 0..1
    val highlight3D: Float = 0.5f       // 0..1
)

/** A saved Pixel Art photo (MediaStore backed). */
data class PixelPhoto(
    val id: Long,
    val uri: Uri,
    val displayName: String,
    val dateTakenMillis: Long,
    val mimeType: String
)

/** Persisted user settings. */
data class AppSettings(
    val defaultPaletteId: String = PixelPalette.PASTEL.id,
    val defaultMode: PixelMode = PixelMode.MEDIUM,
    val quality: OutputQuality = OutputQuality.HIGH,
    val format: OutputFormat = OutputFormat.BOTH,
    val autoSave: Boolean = true,
    val darkPixelMode: Boolean = false,
    val hapticFeedback: Boolean = true,
    val shutterSound: Boolean = true
)
