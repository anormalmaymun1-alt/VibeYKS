package com.example.yksaisinavkocu.service.scanner

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class PdfBitmapRenderer(private val context: Context) {

    /**
     * PDF URI'sindeki sayfaları okur ve yüksek çözünürlüklü Bitmap listesi döndürür.
     * @param uri PDF belgesinin Content URI'si
     * @param maxPages İşlenecek maksimum sayfa sayısı (varsayılan 5)
     */
    suspend fun renderPdfPages(uri: Uri, maxPages: Int = 5): List<Bitmap> = withContext(Dispatchers.IO) {
        val tempFile = File(context.cacheDir, "temp_exam_${System.currentTimeMillis()}.pdf")
        val bitmaps = mutableListOf<Bitmap>()

        try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                tempFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            } ?: return@withContext emptyList()

            val pfd = ParcelFileDescriptor.open(tempFile, ParcelFileDescriptor.MODE_READ_ONLY)
            val renderer = PdfRenderer(pfd)
            val count = minOf(renderer.pageCount, maxPages)

            for (i in 0 until count) {
                val page = renderer.openPage(i)
                // 2.2f ölçek: OCR doğruluğu için optimum keskinlik ve bellek dengesi
                val scale = 2.2f
                val width = (page.width * scale).toInt()
                val height = (page.height * scale).toInt()
                val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                bitmap.eraseColor(Color.WHITE)
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                bitmaps.add(bitmap)
                page.close()
            }

            renderer.close()
            pfd.close()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            if (tempFile.exists()) {
                tempFile.delete()
            }
        }

        bitmaps
    }
}
