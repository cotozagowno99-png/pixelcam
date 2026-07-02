package com.pixelcam.app.ui.screens.viewer

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.pixelcam.app.ui.components.PixelButton
import com.pixelcam.app.ui.components.PixelIcon
import com.pixelcam.app.ui.components.PixelIconType
import com.pixelcam.app.ui.screens.gallery.GalleryViewModel
import com.pixelcam.app.ui.theme.PastelBlue
import com.pixelcam.app.ui.theme.PastelMint
import com.pixelcam.app.ui.theme.PastelPink
import com.pixelcam.app.ui.theme.PastelYellow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun PhotoViewerScreen(
    photoId: Long,
    onBack: () -> Unit,
    viewModel: PhotoViewerViewModel = hiltViewModel(),
    galleryViewModel: GalleryViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val state by galleryViewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        if (state.photos.isEmpty()) galleryViewModel.refresh()
    }

    val photo = state.photos.firstOrNull { it.id == photoId }

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
            Text("PIXEL ART", style = MaterialTheme.typography.headlineMedium)
        }

        Spacer(Modifier.size(12.dp))

        Box(
            Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(8.dp))
                .border(3.dp, Color(0xFF2E2A45), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (photo != null) {
                AsyncImage(
                    model = photo.uri,
                    contentDescription = photo.displayName,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Text("LOADING...", style = MaterialTheme.typography.titleMedium)
            }
        }

        Spacer(Modifier.size(14.dp))

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            PixelButton(
                onClick = {
                    photo?.let {
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = it.mimeType
                            putExtra(Intent.EXTRA_STREAM, it.uri)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(Intent.createChooser(intent, "Share Pixel Art"))
                    }
                },
                color = PastelMint,
                modifier = Modifier.size(58.dp)
            ) { PixelIcon(PixelIconType.SHARE, Modifier.size(26.dp)) }

            PixelButton(
                onClick = {
                    photo?.let { p ->
                        scope.launch {
                            val bitmap: Bitmap? = withContext(Dispatchers.IO) {
                                context.contentResolver.openInputStream(p.uri)
                                    ?.use { BitmapFactory.decodeStream(it) }
                            }
                            bitmap?.let { viewModel.saveAgain(it) }
                        }
                    }
                },
                color = PastelYellow,
                modifier = Modifier.size(58.dp)
            ) { PixelIcon(PixelIconType.SAVE, Modifier.size(26.dp)) }

            PixelButton(
                onClick = {
                    photo?.let { galleryViewModel.delete(it) }
                    onBack()
                },
                color = PastelPink,
                modifier = Modifier.size(58.dp)
            ) { PixelIcon(PixelIconType.TRASH, Modifier.size(26.dp)) }
        }
    }
}
