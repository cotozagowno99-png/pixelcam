package com.pixelcam.app.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pixelcam.app.domain.model.OutputFormat
import com.pixelcam.app.domain.model.OutputQuality
import com.pixelcam.app.domain.model.PixelMode
import com.pixelcam.app.domain.model.PixelPalette
import com.pixelcam.app.ui.components.PixelButton
import com.pixelcam.app.ui.components.PixelChip
import com.pixelcam.app.ui.components.PixelIcon
import com.pixelcam.app.ui.components.PixelIconType
import com.pixelcam.app.ui.components.PixelPanel
import com.pixelcam.app.ui.components.PixelToggle
import com.pixelcam.app.ui.theme.PastelBlue

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val settings by viewModel.settings.collectAsState()

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            PixelButton(
                onClick = onBack,
                color = PastelBlue,
                modifier = Modifier.size(46.dp),
                shadowOffset = 3.dp
            ) { PixelIcon(PixelIconType.BACK, Modifier.size(22.dp)) }
            Spacer(Modifier.size(12.dp))
            Text("SETTINGS", style = MaterialTheme.typography.headlineMedium)
        }

        Spacer(Modifier.height(14.dp))

        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            SettingsSection("DEFAULT PALETTE") {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(PixelPalette.ALL) { palette ->
                        PixelChip(
                            text = palette.label.uppercase(),
                            selected = palette.id == settings.defaultPaletteId,
                            onClick = { viewModel.update { it.copy(defaultPaletteId = palette.id) } }
                        )
                    }
                }
            }

            SettingsSection("DEFAULT MODE") {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PixelMode.entries.forEach { mode ->
                        PixelChip(
                            text = mode.label.uppercase(),
                            selected = mode == settings.defaultMode,
                            onClick = { viewModel.update { it.copy(defaultMode = mode) } },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            SettingsSection("QUALITY") {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutputQuality.entries.forEach { q ->
                        PixelChip(
                            text = q.label.uppercase(),
                            selected = q == settings.quality,
                            onClick = { viewModel.update { it.copy(quality = q) } },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            SettingsSection("FORMAT") {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutputFormat.entries.forEach { f ->
                        PixelChip(
                            text = f.label,
                            selected = f == settings.format,
                            onClick = { viewModel.update { it.copy(format = f) } },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            ToggleRow("AUTO SAVE", settings.autoSave) { v ->
                viewModel.update { it.copy(autoSave = v) }
            }
            ToggleRow("DARK MODE PIXEL", settings.darkPixelMode) { v ->
                viewModel.update { it.copy(darkPixelMode = v) }
            }
            ToggleRow("HAPTIC FEEDBACK", settings.hapticFeedback) { v ->
                viewModel.update { it.copy(hapticFeedback = v) }
            }
            ToggleRow("SHUTTER SOUND", settings.shutterSound) { v ->
                viewModel.update { it.copy(shutterSound = v) }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SettingsSection(title: String, content: @Composable () -> Unit) {
    PixelPanel(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp)) {
            Text(title, style = MaterialTheme.typography.labelMedium)
            Spacer(Modifier.height(8.dp))
            content()
        }
    }
}

@Composable
private fun ToggleRow(title: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    PixelPanel(Modifier.fillMaxWidth()) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            PixelToggle(checked = checked, onCheckedChange = onChange)
        }
    }
}
