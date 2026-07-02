package com.pixelcam.app.camera

import android.content.Context
import android.graphics.SurfaceTexture
import android.util.Size
import android.view.Surface
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.pixelcam.app.domain.model.LensFacing
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Binds a CameraX [Preview] use case to the OpenGL pipeline.
 * Frames go straight into the renderer's [SurfaceTexture] - the app never
 * shows (or stores) an unprocessed camera image.
 */
@Singleton
class CameraController @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var cameraProvider: ProcessCameraProvider? = null
    private var camera: Camera? = null
    private var surfaceTexture: SurfaceTexture? = null
    private var surface: Surface? = null

    fun attachSurfaceTexture(texture: SurfaceTexture) {
        surfaceTexture = texture
    }

    suspend fun start(lifecycleOwner: LifecycleOwner, facing: LensFacing) {
        val provider = cameraProvider ?: awaitProvider().also { cameraProvider = it }
        val texture = surfaceTexture ?: return

        val preview = Preview.Builder()
            .setTargetResolution(Size(1280, 720))
            .build()

        preview.setSurfaceProvider(ContextCompat.getMainExecutor(context)) { request ->
            texture.setDefaultBufferSize(request.resolution.width, request.resolution.height)
            surface?.release()
            val s = Surface(texture)
            surface = s
            request.provideSurface(s, ContextCompat.getMainExecutor(context)) { }
        }

        val selector = when (facing) {
            LensFacing.BACK -> CameraSelector.DEFAULT_BACK_CAMERA
            LensFacing.FRONT -> CameraSelector.DEFAULT_FRONT_CAMERA
        }

        provider.unbindAll()
        camera = provider.bindToLifecycle(lifecycleOwner, selector, preview)
    }

    fun setTorch(enabled: Boolean) {
        camera?.cameraControl?.enableTorch(enabled)
    }

    fun hasTorch(): Boolean = camera?.cameraInfo?.hasFlashUnit() == true

    fun stop() {
        cameraProvider?.unbindAll()
        camera = null
        surface?.release()
        surface = null
    }

    private suspend fun awaitProvider(): ProcessCameraProvider =
        suspendCancellableCoroutine { cont ->
            val future = ProcessCameraProvider.getInstance(context)
            future.addListener(
                {
                    try {
                        cont.resume(future.get())
                    } catch (t: Throwable) {
                        cont.resumeWithException(t)
                    }
                },
                ContextCompat.getMainExecutor(context)
            )
        }
}
