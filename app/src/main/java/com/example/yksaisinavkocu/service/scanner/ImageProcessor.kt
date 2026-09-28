package com.example.yksaisinavkocu.service.scanner

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object ImageProcessor {

    /**
     * URI'den görseli çözer, EXIF oryantasyonunu düzeltir ve gerekirse yeniden boyutlandırır.
     */
    suspend fun loadAndOptimizeBitmap(
        context: Context,
        uri: Uri,
        maxDimension: Int = 1800
    ): Bitmap? = withContext(Dispatchers.IO) {
        try {
            // Boyutları öğren
            var inputStream = context.contentResolver.openInputStream(uri)
            val boundsOptions = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeStream(inputStream, null, boundsOptions)
            inputStream?.close()

            var inSampleSize = 1
            val (origW, origH) = boundsOptions.outWidth to boundsOptions.outHeight
            if (origW > maxDimension || origH > maxDimension) {
                val halfW = origW / 2
                val halfH = origH / 2
                while ((halfW / inSampleSize) >= maxDimension && (halfH / inSampleSize) >= maxDimension) {
                    inSampleSize *= 2
                }
            }

            // Asıl görseli oku
            val decodeOptions = BitmapFactory.Options().apply {
                this.inSampleSize = inSampleSize
            }
            inputStream = context.contentResolver.openInputStream(uri)
            val decodedBitmap = BitmapFactory.decodeStream(inputStream, null, decodeOptions)
            inputStream?.close()

            if (decodedBitmap == null) return@withContext null

            // EXIF oryantasyonunu kontrol et
            val exifStream = context.contentResolver.openInputStream(uri)
            val rotationAngle = exifStream?.use { stream ->
                try {
                    val exif = ExifInterface(stream)
                    when (exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                        ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                        ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                        ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                        else -> 0f
                    }
                } catch (_: Exception) {
                    0f
                }
            } ?: 0f

            if (rotationAngle != 0f) {
                val matrix = Matrix().apply { postRotate(rotationAngle) }
                Bitmap.createBitmap(decodedBitmap, 0, 0, decodedBitmap.width, decodedBitmap.height, matrix, true)
            } else {
                decodedBitmap
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
