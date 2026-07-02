package com.pixelcam.app.data.photo

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.pixelcam.app.domain.model.OutputFormat
import com.pixelcam.app.domain.model.PixelPhoto
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Stores and lists Pixel Art photos through MediaStore under Pictures/PixelCam.
 * Only the rendered Pixel Art is ever written - never the raw camera frame.
 */
@Singleton
class PhotoRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val resolver get() = context.contentResolver

    suspend fun save(bitmap: Bitmap, format: OutputFormat, jpegQuality: Int = 95): List<Uri> =
        withContext(Dispatchers.IO) {
            val stamp = SimpleDateFormat("yyyyMMdd_HHmmss_SSS", Locale.US).format(Date())
            val uris = mutableListOf<Uri>()
            if (format == OutputFormat.PNG || format == OutputFormat.BOTH) {
                writeBitmap(bitmap, "PixelCam_$stamp.png", "image/png") { out ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                }?.let(uris::add)
            }
            if (format == OutputFormat.JPEG || format == OutputFormat.BOTH) {
                writeBitmap(bitmap, "PixelCam_$stamp.jpg", "image/jpeg") { out ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, jpegQuality, out)
                }?.let(uris::add)
            }
            uris
        }

    private fun writeBitmap(
        bitmap: Bitmap,
        displayName: String,
        mime: String,
        compress: (java.io.OutputStream) -> Boolean
    ): Uri? {
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, displayName)
            put(MediaStore.Images.Media.MIME_TYPE, mime)
            put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis())
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/PixelCam")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
        }
        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        } else {
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        }
        val uri = resolver.insert(collection, values) ?: return null
        return try {
            resolver.openOutputStream(uri)?.use { out ->
                if (!compress(out)) error("Bitmap compression failed")
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                values.clear()
                values.put(MediaStore.Images.Media.IS_PENDING, 0)
                resolver.update(uri, values, null, null)
            }
            uri
        } catch (t: Throwable) {
            resolver.delete(uri, null, null)
            null
        }
    }

    suspend fun listPhotos(): List<PixelPhoto> = withContext(Dispatchers.IO) {
        val photos = mutableListOf<PixelPhoto>()
        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.DATE_TAKEN,
            MediaStore.Images.Media.MIME_TYPE
        )
        val selection: String
        val args: Array<String>
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            selection = "${MediaStore.Images.Media.RELATIVE_PATH} LIKE ?"
            args = arrayOf("%Pictures/PixelCam%")
        } else {
            selection = "${MediaStore.Images.Media.DISPLAY_NAME} LIKE ?"
            args = arrayOf("PixelCam_%")
        }
        resolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection, selection, args,
            "${MediaStore.Images.Media.DATE_TAKEN} DESC"
        )?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val nameCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
            val dateCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_TAKEN)
            val mimeCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.MIME_TYPE)
            while (cursor.moveToNext()) {
                val id = cursor.getLong(idCol)
                photos += PixelPhoto(
                    id = id,
                    uri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id),
                    displayName = cursor.getString(nameCol) ?: "PixelCam",
                    dateTakenMillis = cursor.getLong(dateCol),
                    mimeType = cursor.getString(mimeCol) ?: "image/png"
                )
            }
        }
        photos
    }

    suspend fun delete(photo: PixelPhoto): Boolean = withContext(Dispatchers.IO) {
        try {
            resolver.delete(photo.uri, null, null) > 0
        } catch (t: Throwable) {
            false
        }
    }
}
