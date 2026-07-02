package com.pixelcam.app.gl

import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.SurfaceTexture
import android.opengl.GLES11Ext
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import com.pixelcam.app.domain.model.PixelMode
import com.pixelcam.app.domain.model.PixelPalette
import com.pixelcam.app.domain.model.RenderParams
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.util.concurrent.atomic.AtomicBoolean
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * Real-time GPU Pixel Art renderer.
 *
 * The camera feeds a [SurfaceTexture] (GL_TEXTURE_EXTERNAL_OES). Every frame is
 * transformed by a single fragment shader that performs, in order:
 * pixelation -> sharpen -> brightness/contrast/saturation -> palette
 * quantization -> edge darkening -> (optional) voxel-style 3D beveling.
 *
 * Capture works by reading back the exact framebuffer that is on screen,
 * so the saved file is always identical to the live preview.
 */
class PixelArtRenderer(
    private val onSurfaceTextureReady: (SurfaceTexture) -> Unit,
    private val requestRender: () -> Unit
) : GLSurfaceView.Renderer, SurfaceTexture.OnFrameAvailableListener {

    // ---- Public, thread-safe state (set from UI thread, read on GL thread) ----
    @Volatile var params: RenderParams = RenderParams()
    @Volatile var palette: PixelPalette = PixelPalette.PASTEL
    @Volatile var mode: PixelMode = PixelMode.MEDIUM
    @Volatile var mirror: Boolean = false // front camera preview mirroring

    private val frameAvailable = AtomicBoolean(false)
    private val capturePending = AtomicBoolean(false)
    @Volatile private var captureCallback: ((Bitmap) -> Unit)? = null

    // ---- GL objects ----
    private var program = 0
    private var oesTextureId = 0
    private var surfaceTexture: SurfaceTexture? = null
    private val texMatrix = FloatArray(16)

    private var viewWidth = 1
    private var viewHeight = 1

    private var aPosition = 0
    private var aTexCoord = 0
    private var uTexMatrix = 0
    private var uTexture = 0
    private var uResolution = 0
    private var uPixels = 0
    private var uPalStrength = 0
    private var uContrast = 0
    private var uBrightness = 0
    private var uSaturation = 0
    private var uSharpness = 0
    private var uEdge = 0
    private var uDepth = 0
    private var uShadow = 0
    private var uHighlight = 0
    private var uMode3D = 0
    private var uMirror = 0
    private var uPalette = 0
    private var uPalSize = 0

    private val vertexBuffer: FloatBuffer = floatBufferOf(
        // x, y (full-screen quad, triangle strip)
        -1f, -1f,
        1f, -1f,
        -1f, 1f,
        1f, 1f
    )
    private val texBuffer: FloatBuffer = floatBufferOf(
        0f, 0f,
        1f, 0f,
        0f, 1f,
        1f, 1f
    )

    /** Requests an exact-preview capture; callback is invoked off the GL thread with the bitmap. */
    fun capture(callback: (Bitmap) -> Unit) {
        captureCallback = callback
        capturePending.set(true)
        requestRender()
    }

    fun release() {
        surfaceTexture?.release()
        surfaceTexture = null
    }

    override fun onFrameAvailable(st: SurfaceTexture) {
        frameAvailable.set(true)
        requestRender()
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        program = buildProgram(VERTEX_SHADER, FRAGMENT_SHADER)

        aPosition = GLES20.glGetAttribLocation(program, "aPosition")
        aTexCoord = GLES20.glGetAttribLocation(program, "aTexCoord")
        uTexMatrix = GLES20.glGetUniformLocation(program, "uTexMatrix")
        uTexture = GLES20.glGetUniformLocation(program, "uTexture")
        uResolution = GLES20.glGetUniformLocation(program, "uResolution")
        uPixels = GLES20.glGetUniformLocation(program, "uPixels")
        uPalStrength = GLES20.glGetUniformLocation(program, "uPalStrength")
        uContrast = GLES20.glGetUniformLocation(program, "uContrast")
        uBrightness = GLES20.glGetUniformLocation(program, "uBrightness")
        uSaturation = GLES20.glGetUniformLocation(program, "uSaturation")
        uSharpness = GLES20.glGetUniformLocation(program, "uSharpness")
        uEdge = GLES20.glGetUniformLocation(program, "uEdge")
        uDepth = GLES20.glGetUniformLocation(program, "uDepth")
        uShadow = GLES20.glGetUniformLocation(program, "uShadow")
        uHighlight = GLES20.glGetUniformLocation(program, "uHighlight")
        uMode3D = GLES20.glGetUniformLocation(program, "uMode3D")
        uMirror = GLES20.glGetUniformLocation(program, "uMirror")
        uPalette = GLES20.glGetUniformLocation(program, "uPalette")
        uPalSize = GLES20.glGetUniformLocation(program, "uPalSize")

        val tex = IntArray(1)
        GLES20.glGenTextures(1, tex, 0)
        oesTextureId = tex[0]
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, oesTextureId)
        GLES20.glTexParameteri(
            GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
            GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR
        )
        GLES20.glTexParameteri(
            GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
            GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR
        )
        GLES20.glTexParameteri(
            GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
            GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE
        )
        GLES20.glTexParameteri(
            GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
            GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE
        )

        surfaceTexture?.release()
        surfaceTexture = SurfaceTexture(oesTextureId).also {
            it.setOnFrameAvailableListener(this)
            onSurfaceTextureReady(it)
        }

        GLES20.glClearColor(0.06f, 0.05f, 0.1f, 1f)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        viewWidth = width
        viewHeight = height
        GLES20.glViewport(0, 0, width, height)
    }

    override fun onDrawFrame(gl: GL10?) {
        val st = surfaceTexture ?: return
        if (frameAvailable.compareAndSet(true, false)) {
            st.updateTexImage()
            st.getTransformMatrix(texMatrix)
        }

        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        GLES20.glUseProgram(program)

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, oesTextureId)
        GLES20.glUniform1i(uTexture, 0)
        GLES20.glUniformMatrix4fv(uTexMatrix, 1, false, texMatrix, 0)

        val p = params
        val pal = palette
        val m = mode

        // pixelSize 0..1 maps around the mode's base grid (0.5 = default).
        val grid = (m.defaultGrid * (0.5f + p.pixelSize)).coerceIn(16f, 256f)

        GLES20.glUniform2f(uResolution, viewWidth.toFloat(), viewHeight.toFloat())
        GLES20.glUniform1f(uPixels, grid)
        GLES20.glUniform1f(uPalStrength, p.paletteStrength)
        GLES20.glUniform1f(uContrast, 0.5f + p.contrast)          // 0.5..1.5
        GLES20.glUniform1f(uBrightness, (p.brightness - 0.5f) * 0.6f)
        GLES20.glUniform1f(uSaturation, p.saturation * 2f)        // 0..2
        GLES20.glUniform1f(uSharpness, p.sharpness * 1.5f)
        GLES20.glUniform1f(uEdge, p.edgeStrength)
        GLES20.glUniform1f(uDepth, p.depth3D)
        GLES20.glUniform1f(uShadow, p.shadow3D)
        GLES20.glUniform1f(uHighlight, p.highlight3D)
        GLES20.glUniform1i(uMode3D, if (m.is3D) 1 else 0)
        GLES20.glUniform1f(uMirror, if (mirror) 1f else 0f)

        val palArray = pal.toFloatArray()
        GLES20.glUniform3fv(uPalette, pal.colors.size, palArray, 0)
        GLES20.glUniform1i(uPalSize, pal.colors.size)

        GLES20.glEnableVertexAttribArray(aPosition)
        GLES20.glVertexAttribPointer(aPosition, 2, GLES20.GL_FLOAT, false, 0, vertexBuffer)
        GLES20.glEnableVertexAttribArray(aTexCoord)
        GLES20.glVertexAttribPointer(aTexCoord, 2, GLES20.GL_FLOAT, false, 0, texBuffer)

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)

        GLES20.glDisableVertexAttribArray(aPosition)
        GLES20.glDisableVertexAttribArray(aTexCoord)

        if (capturePending.compareAndSet(true, false)) {
            readPixelsToBitmap()?.let { bmp -> captureCallback?.invoke(bmp) }
            captureCallback = null
        }
    }

    /** Reads the current framebuffer (exactly what the user sees). */
    private fun readPixelsToBitmap(): Bitmap? {
        return try {
            val w = viewWidth
            val h = viewHeight
            val buf = ByteBuffer.allocateDirect(w * h * 4).order(ByteOrder.nativeOrder())
            GLES20.glReadPixels(0, 0, w, h, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, buf)
            buf.rewind()
            val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
            bmp.copyPixelsFromBuffer(buf)
            // GL origin is bottom-left; flip vertically for Android bitmaps.
            val flip = Matrix().apply { preScale(1f, -1f) }
            val flipped = Bitmap.createBitmap(bmp, 0, 0, w, h, flip, false)
            if (flipped != bmp) bmp.recycle()
            flipped
        } catch (t: Throwable) {
            null
        }
    }

    private fun buildProgram(vs: String, fs: String): Int {
        val v = compileShader(GLES20.GL_VERTEX_SHADER, vs)
        val f = compileShader(GLES20.GL_FRAGMENT_SHADER, fs)
        val prog = GLES20.glCreateProgram()
        GLES20.glAttachShader(prog, v)
        GLES20.glAttachShader(prog, f)
        GLES20.glLinkProgram(prog)
        val status = IntArray(1)
        GLES20.glGetProgramiv(prog, GLES20.GL_LINK_STATUS, status, 0)
        check(status[0] == GLES20.GL_TRUE) { "Program link failed: ${GLES20.glGetProgramInfoLog(prog)}" }
        GLES20.glDeleteShader(v)
        GLES20.glDeleteShader(f)
        return prog
    }

    private fun compileShader(type: Int, src: String): Int {
        val shader = GLES20.glCreateShader(type)
        GLES20.glShaderSource(shader, src)
        GLES20.glCompileShader(shader)
        val status = IntArray(1)
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, status, 0)
        check(status[0] == GLES20.GL_TRUE) { "Shader compile failed: ${GLES20.glGetShaderInfoLog(shader)}" }
        return shader
    }

    private fun floatBufferOf(vararg values: Float): FloatBuffer =
        ByteBuffer.allocateDirect(values.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .apply { put(values); position(0) }

    companion object {
        private const val VERTEX_SHADER = """
            attribute vec4 aPosition;
            attribute vec2 aTexCoord;
            varying vec2 vCoord;
            void main() {
                gl_Position = aPosition;
                vCoord = aTexCoord;
            }
        """

        private const val FRAGMENT_SHADER = """
            #extension GL_OES_EGL_image_external : require
            precision highp float;

            varying vec2 vCoord;

            uniform samplerExternalOES uTexture;
            uniform mat4 uTexMatrix;
            uniform vec2 uResolution;
            uniform float uPixels;
            uniform float uPalStrength;
            uniform float uContrast;
            uniform float uBrightness;
            uniform float uSaturation;
            uniform float uSharpness;
            uniform float uEdge;
            uniform float uDepth;
            uniform float uShadow;
            uniform float uHighlight;
            uniform int uMode3D;
            uniform float uMirror;
            uniform vec3 uPalette[16];
            uniform int uPalSize;

            vec3 sampleCam(vec2 screen) {
                vec2 c = screen;
                if (uMirror > 0.5) { c.x = 1.0 - c.x; }
                vec2 uv = (uTexMatrix * vec4(c, 0.0, 1.0)).xy;
                return texture2D(uTexture, uv).rgb;
            }

            float luma(vec3 c) { return dot(c, vec3(0.299, 0.587, 0.114)); }

            vec3 grade(vec3 col) {
                col = col + (col - vec3(0.5)) * (uContrast - 1.0);
                col = col + uBrightness;
                float l = luma(col);
                col = mix(vec3(l), col, uSaturation);
                return clamp(col, 0.0, 1.0);
            }

            vec3 quantize(vec3 col) {
                if (uPalSize <= 0 || uPalStrength <= 0.001) { return col; }
                vec3 best = uPalette[0];
                float bestD = 1e9;
                for (int i = 0; i < 16; i++) {
                    if (i >= uPalSize) { break; }
                    vec3 d = col - uPalette[i];
                    float dist = dot(d, d);
                    if (dist < bestD) { bestD = dist; best = uPalette[i]; }
                }
                return mix(col, best, uPalStrength);
            }

            vec3 processCell(vec2 center, vec2 cellPx) {
                vec3 col = sampleCam(center);
                if (uSharpness > 0.001) {
                    vec3 n = sampleCam(center + vec2(0.0, cellPx.y));
                    vec3 s = sampleCam(center - vec2(0.0, cellPx.y));
                    vec3 e = sampleCam(center + vec2(cellPx.x, 0.0));
                    vec3 w = sampleCam(center - vec2(cellPx.x, 0.0));
                    vec3 blur = (n + s + e + w + col) * 0.2;
                    col = clamp(col + (col - blur) * uSharpness, 0.0, 1.0);
                }
                col = grade(col);
                col = quantize(col);
                return col;
            }

            void main() {
                float aspect = uResolution.x / max(uResolution.y, 1.0);
                vec2 grid = vec2(floor(uPixels * aspect + 0.5), uPixels);
                vec2 cellPx = 1.0 / grid;

                vec2 cellId = floor(vCoord * grid);
                vec2 center = (cellId + 0.5) * cellPx;

                vec3 col = processCell(center, cellPx);

                // Edge outline (cell-resolution Sobel-like gradient).
                if (uEdge > 0.001) {
                    float ln = luma(grade(sampleCam(center + vec2(0.0, cellPx.y))));
                    float ls = luma(grade(sampleCam(center - vec2(0.0, cellPx.y))));
                    float le = luma(grade(sampleCam(center + vec2(cellPx.x, 0.0))));
                    float lw = luma(grade(sampleCam(center - vec2(cellPx.x, 0.0))));
                    float g = length(vec2(le - lw, ln - ls));
                    float edge = smoothstep(0.08, 0.45, g);
                    col *= 1.0 - edge * uEdge * 0.85;
                }

                // Voxel / Minecraft-style block shading.
                if (uMode3D == 1) {
                    vec2 f = fract(vCoord * grid);
                    float h = luma(col);
                    float height = h * uDepth;

                    float bevel = 0.16 + 0.10 * height;

                    // Light from top-left: highlight top & left faces.
                    float hlTop  = 1.0 - smoothstep(0.0, bevel, 1.0 - f.y);
                    float hlLeft = 1.0 - smoothstep(0.0, bevel, f.x);
                    float hl = max(hlTop, hlLeft * 0.7);

                    // Shadow on bottom & right faces.
                    float shBottom = 1.0 - smoothstep(0.0, bevel, f.y);
                    float shRight  = 1.0 - smoothstep(0.0, bevel, 1.0 - f.x);
                    float sh = max(shBottom, shRight * 0.8);

                    col += hl * uHighlight * (0.20 + 0.35 * height);
                    col -= sh * uShadow * (0.22 + 0.30 * height);

                    // Taller neighbor above casts a contact shadow on this block.
                    vec3 upCol = processCell(center + vec2(0.0, cellPx.y), cellPx);
                    float upH = luma(upCol) * uDepth;
                    float cast = clamp(upH - height, 0.0, 1.0);
                    float topBand = 1.0 - smoothstep(0.0, 0.35, 1.0 - f.y);
                    col -= cast * topBand * uShadow * 0.35;

                    // Thin dark seams between blocks for the diorama look.
                    float seamW = 0.045;
                    float seam = smoothstep(0.0, seamW, f.x)
                               * smoothstep(0.0, seamW, f.y)
                               * smoothstep(0.0, seamW, 1.0 - f.x)
                               * smoothstep(0.0, seamW, 1.0 - f.y);
                    col *= mix(1.0 - 0.5 * uShadow, 1.0, seam);
                }

                gl_FragColor = vec4(clamp(col, 0.0, 1.0), 1.0);
            }
        """
    }
}
