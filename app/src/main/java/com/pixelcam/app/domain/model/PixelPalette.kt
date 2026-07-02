package com.pixelcam.app.domain.model

/**
 * A fixed color palette used by the GPU shader for color quantization.
 * Colors are packed as 0xRRGGBB ints; max 16 entries (shader uniform limit).
 */
data class PixelPalette(
    val id: String,
    val label: String,
    val colors: List<Int>
) {
    init {
        require(colors.isNotEmpty() && colors.size <= 16) { "Palette must contain 1..16 colors" }
    }

    /** Flattened RGB floats (r,g,b per color) for glUniform3fv. */
    fun toFloatArray(): FloatArray {
        val out = FloatArray(colors.size * 3)
        colors.forEachIndexed { i, c ->
            out[i * 3] = ((c shr 16) and 0xFF) / 255f
            out[i * 3 + 1] = ((c shr 8) and 0xFF) / 255f
            out[i * 3 + 2] = (c and 0xFF) / 255f
        }
        return out
    }

    companion object {
        val PASTEL = PixelPalette(
            "pastel", "Pastel",
            listOf(
                0xFDF6FF, 0xFFD3E8, 0xFFB5D8, 0xB8E8FC, 0x8AD4F5, 0xC9B6F8,
                0xA694F0, 0xBDF4D8, 0x8FE3B4, 0xFFF6BF, 0xFFE08A, 0xFFD7BA,
                0xFFB68A, 0x8C7BB8, 0x594F80, 0x2E2A45
            )
        )
        val GAME_BOY = PixelPalette(
            "gameboy", "Game Boy",
            listOf(0x0F380F, 0x306230, 0x8BAC0F, 0x9BBC0F)
        )
        val NES = PixelPalette(
            "nes", "NES",
            listOf(
                0x000000, 0xFCFCFC, 0xF8F8F8, 0x7C7C7C, 0xA81000, 0xE45C10,
                0xF8B800, 0x00A800, 0x00B800, 0x0058F8, 0x3CBCFC, 0x6844FC,
                0xD800CC, 0xF878F8, 0x503000, 0xFCE0A8
            )
        )
        val RETRO = PixelPalette(
            "retro", "Retro",
            listOf(
                0x1A1C2C, 0x5D275D, 0xB13E53, 0xEF7D57, 0xFFCD75, 0xA7F070,
                0x38B764, 0x257179, 0x29366F, 0x3B5DC9, 0x41A6F6, 0x73EFF7,
                0xF4F4F4, 0x94B0C2, 0x566C86, 0x333C57
            )
        )
        val PINK_DREAM = PixelPalette(
            "pink", "Pink Dream",
            listOf(
                0xFFF0F7, 0xFFD6EB, 0xFFB3DA, 0xFF8CC6, 0xF25FAF, 0xC94F9B,
                0x9C3F82, 0x6E3266, 0xFFE3EF, 0xFFC7E3, 0x47234A, 0x2B1531
            )
        )
        val BLUE_DREAM = PixelPalette(
            "blue", "Blue Dream",
            listOf(
                0xEFF9FF, 0xC9EEFF, 0x9BDCFF, 0x6EC4F5, 0x4BA3E3, 0x3B7FC4,
                0x2F5E9E, 0x274678, 0x1E3057, 0x151E3A, 0xD9F2FF, 0x86D0F0
            )
        )
        val CANDY = PixelPalette(
            "candy", "Candy",
            listOf(
                0xFFF7F0, 0xFFC1CC, 0xFF8FAB, 0xFB6F92, 0xB388EB, 0x8093F1,
                0x72DDF7, 0xF7AEF8, 0xFDE68A, 0xFCA5A5, 0x86EFAC, 0x475569
            )
        )
        val CYBER = PixelPalette(
            "cyber", "Cyber",
            listOf(
                0x0D0221, 0x241734, 0x2E2157, 0xFD3777, 0xF706CF, 0xFD1D53,
                0xF9C80E, 0x00F0FF, 0x00B8C4, 0x7122FA, 0x560A86, 0xEDF5FD
            )
        )
        val SOFT = PixelPalette(
            "soft", "Soft",
            listOf(
                0xFAF7F2, 0xEFE6DA, 0xE3D3C2, 0xD1B79E, 0xB69B84, 0x94806E,
                0x6F6258, 0x4C453F, 0xDCE5DC, 0xB9CCB9, 0x9AB2A8, 0x7A8F88
            )
        )
        val MONO = PixelPalette(
            "mono", "Mono",
            listOf(0x111111, 0x333333, 0x555555, 0x777777, 0x999999, 0xBBBBBB, 0xDDDDDD, 0xF7F7F7)
        )

        val ALL = listOf(
            PASTEL, GAME_BOY, NES, RETRO, PINK_DREAM,
            BLUE_DREAM, CANDY, CYBER, SOFT, MONO
        )

        fun byId(id: String): PixelPalette = ALL.firstOrNull { it.id == id } ?: PASTEL
    }
}
