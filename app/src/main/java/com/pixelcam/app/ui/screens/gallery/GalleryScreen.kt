package com.pixelcam.app.ui.screens.gallery

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.pixelcam.app.domain.model.PixelPhoto
import com.pixelcam.app.ui.components.PixelButton
import com.pixelcam.app.ui.components.PixelIcon
import com.pixelcam.app.ui.components.PixelIconType
import com.pixelcam.app.ui.theme.PastelBlue

@Composable
fun GalleryScreen(
    onBack: () -> Unit,
    onOpenPhoto: (Long) -> Unit,
    viewModel: GalleryViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) { viewModel.refresh() }

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .padding(horizontal = 12.dp)
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PixelButton(
                onClick = onBack,
                color = PastelBlue,
                modifier = Modifier.size(46.dp),
                shadowOffset = 3.dp
            ) { PixelIcon(PixelIconType.BACK, Modifier.size(22.dp)) }
            Spacer(Modifier.size(12.dp))
            Text("PIXEL GALLERY", style = MaterialTheme.typography.headlineMedium)
        }

        when {
            state.loading -> CenterMessage("LOADING...")
            state.photos.isEmpty() -> CenterMessage("NO PIXEL ART YET\nGO TAKE SOME!")
            else -> LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 24.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(state.photos, key = { it.id }) { photo ->
                    GalleryTile(photo = photo, onClick = { onOpenPhoto(photo.id) })
                }
            }
        }
    }
}

@Composable
private fun GalleryTile(photo: PixelPhoto, onClick: () -> Unit) {
    Box(
        Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(6.dp))
            .border(3.dp, Color(0xFF2E2A45), RoundedCornerShape(6.dp))
            .clickable(onClick = onClick)
    ) {
        AsyncImage(
            model = photo.uri,
            contentDescription = photo.displayName,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
private fun CenterMessage(text: String) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}
