package com.pixelcam.app.ui.screens.camera

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.pixelcam.app.domain.model.LensFacing
import com.pixelcam.app.domain.model.PixelMode
import com.pixelcam.app.domain.model.PixelPalette
import com.pixelcam.app.gl.PixelGLSurfaceView
import com.pixelcam.app.ui.components.PixelButton
import com.pixelcam.app.ui.components.PixelChip
import com.pixelcam.app.ui.components.PixelIcon
import com.pixelcam.app.ui.components.PixelIconType
import com.pixelcam.app.ui.components.PixelPanel
import com.pixelcam.app.ui.components.PixelSlider
import com.pixelcam.app.ui.theme.PastelBlue
import com.pixelcam.app.ui.theme.PastelMint
import com.pixelcam.app.ui.theme.PastelPink
import com.pixelcam.app.ui.theme.PastelPurple
import com.pixelcam.app.ui.theme.PastelYellow
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraScreen(
    onOpenGallery: () -> Unit,
    onOpenSettings: () -> Unit,
    viewModel: CameraViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current
    val haptics = LocalHapticFeedback.current
    val context = LocalContext.current

    val cameraPermission = rememberPermissionState(android.Manifest.permission.CAMERA)
    var glView by remember { mutableStateOf<PixelGLSurfaceView?>(null) }
    var shutterFlash by remember { mutableStateOf(false) }

    // Rebind camera when lens changes or permission is granted.
    LaunchedEffect(cameraPermission.status.isGranted, state.lens, glView, state.initialized) {
        if (cameraPermission.status.isGranted && glView != null && state.initialized) {
            viewModel.cameraController.start(lifecycleOwner, state.lens)
        }
    }

    // Push live parameters into the GL renderer on every change.
    LaunchedEffect(state.params, state.palette, state.mode, state.lens, glView) {
        glView?.let { v ->
            v.renderer.params = state.params
            v.renderer.palette = state.palette
            v.renderer.mode = state.mode
            v.renderer.mirror = state.lens == LensFacing.FRONT
            v.requestRender()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is CameraEvent.ShutterFlash -> {
                    shutterFlash = true
                    delay(140)
                    shutterFlash = false
                }
                is CameraEvent.Saved -> android.widget.Toast
                    .makeText(context, "PIXEL ART SAVED!", android.widget.Toast.LENGTH_SHORT).show()
                CameraEvent.SaveFailed -> android.widget.Toast
                    .makeText(context, "SAVE FAILED", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (cameraPermission.status.isGranted) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    PixelGLSurfaceView(ctx) { texture ->
                        viewModel.cameraController.attachSurfaceTexture(texture)
                    }.also { glView = it }
                }
            )
        } else {
            PermissionPrompt(onRequest = { cameraPermission.launchPermissionRequest() })
        }

        // Retro shutter flash overlay
        val flashAlpha by animateFloatAsState(
            targetValue = if (shutterFlash) 0.85f else 0f,
            animationSpec = tween(durationMillis = 90),
            label = "shutterFlash"
        )
        if (flashAlpha > 0.01f) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color.White.copy(alpha = flashAlpha))
            )
        }

        Column(
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(12.dp)
        ) {
            TopBar(
                torchOn = state.torchOn,
                onToggleTorch = { viewModel.toggleTorch() },
                onOpenSettings = onOpenSettings,
                onTogglePalettes = { viewModel.togglePalettePicker() },
                onToggleSliders = { viewModel.toggleControls() }
            )

            Spacer(Modifier.weight(1f))

            AnimatedVisibility(
                visible = state.palettePickerVisible,
                enter = slideInVertically(tween(160)) { it / 2 } + fadeIn(tween(160)),
                exit = slideOutVertically(tween(120)) { it / 2 } + fadeOut(tween(120))
            ) {
                PalettePicker(
                    selected = state.palette,
                    onSelect = { viewModel.setPalette(it) }
                )
            }

            AnimatedVisibility(
                visible = state.controlsVisible,
                enter = slideInVertically(tween(160)) { it / 2 } + fadeIn(tween(160)),
                exit = slideOutVertically(tween(120)) { it / 2 } + fadeOut(tween(120))
            ) {
                ParamsPanel(viewModel = viewModel, is3D = state.mode.is3D)
            }

            Spacer(Modifier.height(10.dp))

            ModeSelector(
                current = state.mode,
                onSelect = {
                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    viewModel.setMode(it)
                }
            )

            Spacer(Modifier.height(12.dp))

            BottomBar(
                capturing = state.capturing,
                onGallery = onOpenGallery,
                onFlip = { viewModel.toggleLens() },
                onShutter = {
                    if (state.settings.hapticFeedback) {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    }
                    glView?.renderer?.capture { bitmap -> viewModel.onCaptured(bitmap) }
                }
            )
        }
    }
}

@Composable
private fun TopBar(
    torchOn: Boolean,
    onToggleTorch: () -> Unit,
    onOpenSettings: () -> Unit,
    onTogglePalettes: () -> Unit,
    onToggleSliders: () -> Unit
) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            SmallPixelButton(color = PastelYellow, onClick = onToggleTorch) {
                PixelIcon(
                    if (torchOn) PixelIconType.FLASH_ON else PixelIconType.FLASH_OFF,
                    Modifier.size(22.dp)
                )
            }
            SmallPixelButton(color = PastelPink, onClick = onTogglePalettes) {
                PixelIcon(PixelIconType.PALETTE, Modifier.size(22.dp))
            }
            SmallPixelButton(color = PastelMint, onClick = onToggleSliders) {
                PixelIcon(PixelIconType.SLIDERS, Modifier.size(22.dp))
            }
        }
        SmallPixelButton(color = PastelBlue, onClick = onOpenSettings) {
            PixelIcon(PixelIconType.SETTINGS, Modifier.size(22.dp))
        }
    }
}

@Composable
private fun SmallPixelButton(
    color: Color,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    PixelButton(
        onClick = onClick,
        color = color,
        modifier = Modifier.size(46.dp),
        shadowOffset = 3.dp
    ) { content() }
}

@Composable
private fun ModeSelector(current: PixelMode, onSelect: (PixelMode) -> Unit) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        PixelMode.entries.forEach { mode ->
            PixelChip(
                text = mode.label.uppercase(),
                selected = mode == current,
                onClick = { onSelect(mode) },
                modifier = Modifier.weight(1f),
                selectedColor = when (mode) {
                    PixelMode.SMALL -> PastelBlue
                    PixelMode.MEDIUM -> PastelPink
                    PixelMode.VOXEL -> PastelPurple
                }
            )
        }
    }
}

@Composable
private fun BottomBar(
    capturing: Boolean,
    onGallery: () -> Unit,
    onFlip: () -> Unit,
    onShutter: () -> Unit
) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        PixelButton(
            onClick = onGallery,
            color = PastelMint,
            modifier = Modifier.size(58.dp)
        ) { PixelIcon(PixelIconType.GALLERY, Modifier.size(26.dp)) }

        // Big pixel shutter
        PixelButton(
            onClick = onShutter,
            enabled = !capturing,
            color = PastelPink,
            modifier = Modifier.size(88.dp),
            shadowOffset = 6.dp
        ) {
            Box(
                Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White)
                    .border(3.dp, Color(0xFF2E2A45), RoundedCornerShape(8.dp))
            )
        }

        PixelButton(
            onClick = onFlip,
            color = PastelBlue,
            modifier = Modifier.size(58.dp)
        ) { PixelIcon(PixelIconType.FLIP_CAMERA, Modifier.size(26.dp)) }
    }
}

@Composable
private fun PalettePicker(selected: PixelPalette, onSelect: (PixelPalette) -> Unit) {
    PixelPanel(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp)) {
            Text("PALETTES", style = MaterialTheme.typography.labelMedium)
            Spacer(Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(PixelPalette.ALL) { palette ->
                    PaletteSwatch(
                        palette = palette,
                        selected = palette.id == selected.id,
                        onClick = { onSelect(palette) }
                    )
                }
            }
        }
    }
}

@Composable
private fun PaletteSwatch(palette: PixelPalette, selected: Boolean, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(
            Modifier
                .clip(RoundedCornerShape(6.dp))
                .border(
                    width = if (selected) 4.dp else 3.dp,
                    color = if (selected) Color(0xFFA694F0) else Color(0xFF2E2A45),
                    shape = RoundedCornerShape(6.dp)
                )
                .clickable(onClick = onClick)
        ) {
            palette.colors.take(5).forEach { c ->
                Box(
                    Modifier
                        .size(width = 12.dp, height = 34.dp)
                        .background(Color(0xFF000000 or c.toLong()))
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(palette.label.uppercase(), style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun ParamsPanel(viewModel: CameraViewModel, is3D: Boolean) {
    val state by viewModel.state.collectAsState()
    PixelPanel(Modifier.fillMaxWidth()) {
        Column(
            Modifier
                .padding(12.dp)
                .heightInMax()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PixelSlider("PIXEL SIZE", state.params.pixelSize,
                { v -> viewModel.updateParams { it.copy(pixelSize = v) } }, fillColor = PastelBlue)
            PixelSlider("PALETTE STRENGTH", state.params.paletteStrength,
                { v -> viewModel.updateParams { it.copy(paletteStrength = v) } }, fillColor = PastelPink)
            PixelSlider("CONTRAST", state.params.contrast,
                { v -> viewModel.updateParams { it.copy(contrast = v) } }, fillColor = PastelPurple)
            PixelSlider("BRIGHTNESS", state.params.brightness,
                { v -> viewModel.updateParams { it.copy(brightness = v) } }, fillColor = PastelYellow)
            PixelSlider("SATURATION", state.params.saturation,
                { v -> viewModel.updateParams { it.copy(saturation = v) } }, fillColor = PastelMint)
            PixelSlider("SHARPNESS", state.params.sharpness,
                { v -> viewModel.updateParams { it.copy(sharpness = v) } }, fillColor = PastelBlue)
            PixelSlider("EDGE STRENGTH", state.params.edgeStrength,
                { v -> viewModel.updateParams { it.copy(edgeStrength = v) } }, fillColor = PastelPink)
            if (is3D) {
                PixelSlider("3D DEPTH", state.params.depth3D,
                    { v -> viewModel.updateParams { it.copy(depth3D = v) } }, fillColor = PastelPurple)
                PixelSlider("3D SHADOW", state.params.shadow3D,
                    { v -> viewModel.updateParams { it.copy(shadow3D = v) } }, fillColor = PastelYellow)
                PixelSlider("3D HIGHLIGHT", state.params.highlight3D,
                    { v -> viewModel.updateParams { it.copy(highlight3D = v) } }, fillColor = PastelMint)
            }
        }
    }
}

private fun Modifier.heightInMax(): Modifier =
    this.then(Modifier.height(280.dp))

@Composable
private fun PermissionPrompt(onRequest: () -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "PIXELCAM NEEDS\nTHE CAMERA",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(Modifier.height(24.dp))
        PixelButton(
            onClick = onRequest,
            color = PastelPink,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text("GRANT ACCESS", style = MaterialTheme.typography.titleMedium)
        }
    }
}
