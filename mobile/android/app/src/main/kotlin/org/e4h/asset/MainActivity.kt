package org.e4h.asset

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.android.RenderMode
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.atomic.AtomicInteger
import kotlinx.coroutines.*
import android.os.Handler
import android.os.Looper

class MainActivity : FlutterActivity() {
    private val imageCompressionScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val mainHandler = Handler(Looper.getMainLooper())
    private val imageCounter = AtomicInteger(0)

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
                    imageCompressionScope.launch {
                        try {
                            val outPath = compressImage(path, maxW, maxH, quality)
                            mainHandler.post {
                                result.success(outPath)
                            }
                        } catch (e: CompressionException) {
                            mainHandler.post {
                                result.error(e.code, e.message, null)
                            }
                        } catch (e: Exception) {
                            mainHandler.post {
                                result.error("COMPRESS_FAILED", e.message ?: "Image compression failed", null)
                            }
                        }
                    }
                } else {
                    result.notImplemented()
                }
            }
    }

    override fun onDestroy() {
        imageCompressionScope.cancel()
        super.onDestroy()
    }

    private fun compressImage(inputPath: String, maxW: Int, maxH: Int, quality: Int): String {
        val inFile = File(inputPath)
        if (!inFile.exists()) {
            throw CompressionException("INPUT_NOT_FOUND", "Input file not found")
        }

        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(inputPath, bounds)
        val srcW = bounds.outWidth
        val srcH = bounds.outHeight
        if (srcW <= 0 || srcH <= 0) {
            throw CompressionException("INVALID_BOUNDS", "Invalid image bounds")
        }

        var inSampleSize = 1
        while ((srcW / inSampleSize) > maxW || (srcH / inSampleSize) > maxH) {
            inSampleSize *= 2
        }

        var bmp: Bitmap? = null
        try {
            val decodedBitmap = decodeBitmapWithFallback(inputPath, inSampleSize)
            bmp = decodedBitmap
            var workingBitmap: Bitmap = decodedBitmap

            val w = workingBitmap.width
            val h = workingBitmap.height
            val scale = minOf(maxW.toFloat() / w.toFloat(), maxH.toFloat() / h.toFloat(), 1f)
            if (scale < 1f) {
                val nw = (w * scale).toInt().coerceAtLeast(1)
                val nh = (h * scale).toInt().coerceAtLeast(1)
                val scaled = Bitmap.createScaledBitmap(workingBitmap, nw, nh, true)
                if (scaled != workingBitmap) {
                    workingBitmap.recycle()
                    workingBitmap = scaled
                    bmp = scaled
                }
            }

            val outFile = File(
                cacheDir,
                "img_${System.currentTimeMillis()}_${imageCounter.incrementAndGet()}.jpg"
            )
            FileOutputStream(outFile).use { fos ->
                val success = workingBitmap.compress(Bitmap.CompressFormat.JPEG, quality, fos)
                fos.flush()
                if (!success) {
                    throw CompressionException("COMPRESS_WRITE_FAILED", "Failed to write compressed image")
                }
            }
            return outFile.absolutePath
        } finally {
            bmp?.recycle()
        }
    }

    private fun decodeBitmapWithFallback(inputPath: String, initialSampleSize: Int): Bitmap {
        var sampleSize = initialSampleSize.coerceAtLeast(1)
        repeat(4) {
            try {
                val opts = BitmapFactory.Options().apply {
                    inSampleSize = sampleSize
                    inPreferredConfig = Bitmap.Config.RGB_565
                }
                return BitmapFactory.decodeFile(inputPath, opts)
                    ?: throw CompressionException("DECODE_FAILED", "Failed to decode image")
            } catch (oom: OutOfMemoryError) {
                sampleSize *= 2
            }
        }
        throw CompressionException(
            "DECODE_OOM",
            "Image decode ran out of memory after fallback attempts"
        )
    }
}

private class CompressionException(
    val code: String,
    override val message: String
) : Exception(message)
