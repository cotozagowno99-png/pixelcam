package com.pixelcam.app.gl

import android.content.Context
import android.graphics.SurfaceTexture
import android.opengl.GLSurfaceView

/**
 * GLSurfaceView hosting [PixelArtRenderer].
 * RENDERMODE_WHEN_DIRTY keeps GPU/battery usage minimal: we only draw
 * when the camera delivers a new frame or parameters change.
 */
class PixelGLSurfaceView(
    context: Context,
    onSurfaceTextureReady: (SurfaceTexture) -> Unit
) : GLSurfaceView(context) {

    val renderer: PixelArtRenderer = PixelArtRenderer(
        onSurfaceTextureReady = onSurfaceTextureReady,
        requestRender = { requestRender() }
    )

    init {
        setEGLContextClientVersion(2)
        setRenderer(renderer)
        renderMode = RENDERMODE_WHEN_DIRTY
        preserveEGLContextOnPause = true
    }
}
