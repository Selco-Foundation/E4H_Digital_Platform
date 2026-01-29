package org.e4h.asset

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.android.RenderMode
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel
import java.io.File
import java.io.FileOutputStream

class MainActivity : FlutterActivity() {
    override fun getRenderMode(): RenderMode = RenderMode.texture

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)

        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, "org.e4h.asset/image_tools")
            .setMethodCallHandler { call, result ->
                if (call.method == "compressImage") {
                    val path = call.argument<String>("path")
                    if (path.isNullOrBlank()) {
                        result.error("ARG", "path is required", null)
                        return@setMethodCallHandler
                    }

                    val maxW = call.argument<Int>("maxWidth") ?: 1600
                    val maxH = call.argument<Int>("maxHeight") ?: 1600
                    val quality = (call.argument<Int>("quality") ?: 70).coerceIn(0, 100)

                    try {
                        val outPath = compressImage(path, maxW, maxH, quality)
                        result.success(outPath)
                    } catch (e: Exception) {
                        result.error("COMPRESS_FAILED", e.message, null)
                    }
                } else {
                    result.notImplemented()
                }
            }
    }

    private fun compressImage(inputPath: String, maxW: Int, maxH: Int, quality: Int): String {
        val inFile = File(inputPath)
        require(inFile.exists()) { "Input file not found" }

        // 1) Decode bounds
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(inputPath, bounds)
        val srcW = bounds.outWidth
        val srcH = bounds.outHeight
        require(srcW > 0 && srcH > 0) { "Invalid image bounds" }

        // 2) Compute inSampleSize (power-of-2)
        var inSampleSize = 1
        while ((srcW / inSampleSize) > maxW || (srcH / inSampleSize) > maxH) {
            inSampleSize *= 2
        }

        // 3) Decode sampled bitmap (lower memory)
        val opts = BitmapFactory.Options().apply {
            this.inSampleSize = inSampleSize
            this.inPreferredConfig = Bitmap.Config.RGB_565
        }
        var bmp = BitmapFactory.decodeFile(inputPath, opts)
            ?: throw IllegalStateException("Failed to decode image")

        // 4) Final scale if still above target
        val w = bmp.width
        val h = bmp.height
        val scale = minOf(maxW.toFloat() / w.toFloat(), maxH.toFloat() / h.toFloat(), 1f)
        if (scale < 1f) {
            val nw = (w * scale).toInt().coerceAtLeast(1)
            val nh = (h * scale).toInt().coerceAtLeast(1)
            val scaled = Bitmap.createScaledBitmap(bmp, nw, nh, true)
            if (scaled != bmp) {
                bmp.recycle()
                bmp = scaled
            }
        }

        // 5) Write JPEG into cache
        val outFile = File(cacheDir, "img_${System.currentTimeMillis()}.jpg")
        FileOutputStream(outFile).use { fos ->
            bmp.compress(Bitmap.CompressFormat.JPEG, quality, fos)
            fos.flush()
        }
        bmp.recycle()

        return outFile.absolutePath
    }
}
