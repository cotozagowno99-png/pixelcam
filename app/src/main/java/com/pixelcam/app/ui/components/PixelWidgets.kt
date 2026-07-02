package com.pixelcam.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

private val PixelShape = RoundedCornerShape(6.dp)
private val Ink = Color(0xFF2E2A45)

/**
 * Chunky 8-bit button: rounded pixel body, hard offset shadow,
 * short retro "press down" animation.
 */
@Composable
fun PixelButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = Ink,
    shadowOffset: Dp = 4.dp,
    enabled: Boolean = true,
    content: @Composable () -> Unit
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val press by animateFloatAsState(
        targetValue = if (pressed) 1f else 0f,
        animationSpec = tween(durationMillis = 80),
        label = "pixelPress"
    )
    val offsetPx = shadowOffset * press

    Box(modifier = modifier) {
        // Hard pixel shadow
        Box(
            Modifier
                .matchParentSize()
                .offset(x = shadowOffset, y = shadowOffset)
                .clip(PixelShape)
                .background(Ink.copy(alpha = 0.9f))
        )
        Box(
            Modifier
                .matchParentSize()
                .offset(x = offsetPx, y = offsetPx)
                .clip(PixelShape)
                .background(if (enabled) color else color.copy(alpha = 0.4f))
                .border(3.dp, Ink, PixelShape)
                .clickable(
                    interactionSource = interaction,
                    indication = null,
                    enabled = enabled,
                    onClick = onClick
                ),
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.runtime.CompositionLocalProvider(
                androidx.compose.material3.LocalContentColor provides contentColor
            ) {
                content()
            }
        }
    }
}

/** Rounded pixel panel with a thick ink border. */
@Composable
fun PixelPanel(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.surface,
    content: @Composable () -> Unit
) {
    Box(
        modifier
            .clip(PixelShape)
            .background(color)
            .border(3.dp, Ink, PixelShape)
    ) { content() }
}

/**
 * Retro slider: blocky track filled with square "pixels" and a chunky knob.
 */
@Composable
fun PixelSlider(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    fillColor: Color = MaterialTheme.colorScheme.primary
) {
    Column(modifier = modifier) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween
        ) {
            Text(label, style = MaterialTheme.typography.labelMedium)
            Text(
                "${(value * 100).roundToInt()}",
                style = MaterialTheme.typography.labelMedium,
                color = Ink.copy(alpha = 0.6f)
            )
        }
        Box(
            Modifier
                .padding(top = 4.dp)
                .fillMaxWidth()
                .height(26.dp)
        ) {
            Box(
                Modifier
                    .matchParentSize()
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color.White)
                    .border(3.dp, Ink, RoundedCornerShape(4.dp))
                    .pointerInput(Unit) {
                        detectTapGestures { pos ->
                            val w = size.width.toFloat()
                            if (w > 0f) onValueChange((pos.x / w).coerceIn(0f, 1f))
                        }
                    }
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { pos ->
                                val w = size.width.toFloat()
                                if (w > 0f) onValueChange((pos.x / w).coerceIn(0f, 1f))
                            },
                            onDrag = { change, _ ->
                                change.consume()
                                val w = size.width.toFloat()
                                if (w > 0f) onValueChange((change.position.x / w).coerceIn(0f, 1f))
                            }
                        )
                    }
            ) {
                // Blocky fill segments
                Row(
                    Modifier
                        .matchParentSize()
                        .padding(5.dp)
                ) {
                    val segments = 20
                    val filled = (value * segments).roundToInt()
                    repeat(segments) { i ->
                        Box(
                            Modifier
                                .weight(1f)
                                .fillMaxSize()
                                .padding(horizontal = 1.dp)
                                .background(
                                    if (i < filled) fillColor else Color.Transparent,
                                    RoundedCornerShape(1.dp)
                                )
                        )
                    }
                }
            }
        }
    }
}

/** Square 8-bit toggle. */
@Composable
fun PixelToggle(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val knob by animateFloatAsState(
        targetValue = if (checked) 1f else 0f,
        animationSpec = tween(durationMillis = 120),
        label = "toggle"
    )
    Box(
        modifier
            .size(width = 56.dp, height = 30.dp)
            .clip(PixelShape)
            .background(if (checked) PastelGreen else Color(0xFFE8E2F2))
            .border(3.dp, Ink, PixelShape)
            .clickable { onCheckedChange(!checked) }
    ) {
        Box(
            Modifier
                .padding(4.dp)
                .size(20.dp)
                .offset { IntOffset((knob * 24.dp.toPx()).roundToInt(), 0) }
                .clip(RoundedCornerShape(4.dp))
                .background(Color.White)
                .border(2.dp, Ink, RoundedCornerShape(4.dp))
        )
    }
}

private val PastelGreen = Color(0xFF8FE3B4)

/** Selectable pixel chip (modes, palettes, enum settings). */
@Composable
fun PixelChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selectedColor: Color = MaterialTheme.colorScheme.secondary
) {
    Box(
        modifier
            .clip(PixelShape)
            .background(if (selected) selectedColor else Color.White)
            .border(3.dp, Ink, PixelShape)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text,
            style = MaterialTheme.typography.labelMedium,
            color = Ink,
            textAlign = TextAlign.Center
        )
    }
}
