package com.pixelcam.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color

/**
 * Hand-drawn 8-bit icons on a 12x12 virtual pixel grid.
 * No Material icons anywhere in the app - everything is custom Pixel Art.
 */
enum class PixelIconType {
    FLASH_ON, FLASH_OFF, FLIP_CAMERA, GALLERY, SETTINGS, BACK,
    SHARE, TRASH, SAVE, CHECK, CLOSE, SLIDERS, PALETTE
}

@Composable
fun PixelIcon(
    type: PixelIconType,
    modifier: Modifier = Modifier,
    tint: Color = Color(0xFF2E2A45)
) {
    Canvas(modifier = modifier) {
        val cell = size.minDimension / 12f
        fun px(x: Int, y: Int, w: Int = 1, h: Int = 1, color: Color = tint) {
            drawRect(
                color = color,
                topLeft = Offset(x * cell, y * cell),
                size = Size(w * cell, h * cell)
            )
        }
        fun flash() {
            px(6, 1, 2, 1); px(5, 2, 2, 1); px(4, 3, 3, 1)
            px(3, 4, 3, 2); px(4, 6, 5, 1)
            px(6, 7, 2, 1); px(5, 8, 2, 1); px(4, 9, 2, 2)
        }
        when (type) {
            PixelIconType.FLASH_ON -> flash()
            PixelIconType.FLASH_OFF -> {
                flash()
                // diagonal strike-through
                for (i in 1..10) px(i, i)
            }
            PixelIconType.FLIP_CAMERA -> {
                px(2, 3, 8, 1); px(2, 8, 8, 1)
                px(2, 4, 1, 2); px(9, 6, 1, 2)
                px(9, 2, 1, 3); px(8, 2); px(10, 3)  // top-right arrow head
                px(2, 7, 1, 3); px(1, 8); px(3, 9)   // bottom-left arrow head
            }
            PixelIconType.GALLERY -> {
                px(1, 2, 10, 8)
                px(2, 3, 8, 6, Color.White.copy(alpha = 0.85f))
                px(3, 6, 3, 3, tint)           // mountain
                px(6, 5, 3, 4, tint.copy(alpha = 0.7f))
                px(8, 4, 1, 1, tint)           // sun
            }
            PixelIconType.SETTINGS -> {
                // pixel gear
                px(5, 1, 2, 2); px(5, 9, 2, 2)
                px(1, 5, 2, 2); px(9, 5, 2, 2)
                px(2, 2, 2, 2); px(8, 2, 2, 2)
                px(2, 8, 2, 2); px(8, 8, 2, 2)
                px(4, 4, 4, 4)
                px(5, 5, 2, 2, Color.White.copy(alpha = 0.9f))
            }
            PixelIconType.BACK -> {
                px(3, 5, 7, 2)
                px(4, 3, 2, 2); px(3, 4, 2, 2)
                px(3, 6, 2, 2); px(4, 7, 2, 2)
            }
            PixelIconType.SHARE -> {
                px(8, 1, 3, 3); px(1, 5, 3, 3); px(8, 8, 3, 3)
                px(4, 5, 1, 1); px(5, 4, 1, 1); px(6, 3, 1, 1); px(7, 2, 1, 1)
                px(5, 7, 1, 1); px(6, 8, 1, 1); px(7, 9, 1, 1); px(4, 6, 1, 1)
            }
            PixelIconType.TRASH -> {
                px(3, 2, 6, 1); px(5, 1, 2, 1)
                px(2, 3, 8, 1)
                px(3, 4, 6, 7)
                px(4, 5, 1, 5, Color.White.copy(alpha = 0.85f))
                px(6, 5, 1, 5, Color.White.copy(alpha = 0.85f))
            }
            PixelIconType.SAVE -> {
                px(2, 2, 8, 8)
                px(4, 2, 4, 3, Color.White.copy(alpha = 0.85f))
                px(3, 6, 6, 3, Color.White.copy(alpha = 0.85f))
                px(6, 2, 1, 3, tint)
            }
            PixelIconType.CHECK -> {
                px(2, 6); px(3, 7); px(4, 8); px(5, 7); px(6, 6)
                px(7, 5); px(8, 4); px(9, 3)
                px(3, 6); px(4, 7); px(6, 5); px(7, 4); px(8, 3)
            }
            PixelIconType.CLOSE -> {
                for (i in 2..9) { px(i, i); px(i, 11 - i) }
            }
            PixelIconType.SLIDERS -> {
                px(1, 3, 10, 1); px(1, 6, 10, 1); px(1, 9, 10, 1)
                px(3, 2, 2, 3); px(7, 5, 2, 3); px(4, 8, 2, 3)
            }
            PixelIconType.PALETTE -> {
                px(2, 2, 8, 8)
                px(3, 3, 3, 3, Color(0xFFFFB5D8))
                px(6, 3, 3, 3, Color(0xFF8AD4F5))
                px(3, 6, 3, 3, Color(0xFFFFE08A))
                px(6, 6, 3, 3, Color(0xFF8FE3B4))
            }
        }
    }
}

