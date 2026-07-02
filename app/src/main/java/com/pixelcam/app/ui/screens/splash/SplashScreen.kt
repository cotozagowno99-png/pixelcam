package com.pixelcam.app.ui.screens.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.math.min

/**
 * Retro boot animation: the pixel camera logo assembles block by block,
 * then the title types in - like an 8-bit console startup.
 */
@Composable
fun SplashScreen(onFinished: () -> Unit) {
    val progress = remember { Animatable(0f) }
    var typed by remember { mutableFloatStateOf(0f) }
    val title = "PIXELCAM"

    LaunchedEffect(Unit) {
        progress.animateTo(1f, animationSpec = tween(durationMillis = 900))
        // Type the title, one blocky letter at a time.
        for (i in 1..title.length) {
            typed = i.toFloat()
            delay(70)
        }
        delay(450)
        onFinished()
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        PixelLogoCanvas(progress = progress.value, modifier = Modifier.size(140.dp))
        Spacer(Modifier.height(24.dp))
        Text(
            title.take(typed.toInt()) + if (typed.toInt() < title.length) "_" else "",
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
private fun PixelLogoCanvas(progress: Float, modifier: Modifier = Modifier) {
    // Camera drawn on a 12x12 grid; blocks appear in scanline order with progress.
    val blocks = remember {
        buildList {
            // body
            for (y in 4..9) for (x in 1..10) add(Triple(x, y, Color(0xFFA694F0)))
            // top plate
            for (x in 2..9) add(Triple(x, 3, Color(0xFF8C7BB8)))
            // viewfinder bump
            for (x in 4..6) add(Triple(x, 2, Color(0xFFFFB5D8)))
            // lens
            for (y in 5..8) for (x in 4..7) add(Triple(x, y, Color.White))
            for (y in 6..7) for (x in 5..6) add(Triple(x, y, Color(0xFF8AD4F5)))
            // flash dot
            add(Triple(9, 4, Color(0xFFFFE08A)))
        }
    }
    Canvas(modifier) {
        val cell = min(size.width, size.height) / 12f
        val visible = (blocks.size * progress).toInt()
        blocks.take(visible).forEach { (x, y, color) ->
            drawRect(
                color = color,
                topLeft = Offset(x * cell, y * cell),
                size = Size(cell, cell)
            )
        }
    }
}
