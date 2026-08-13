package com.obrago.app.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import java.io.ByteArrayOutputStream

/**
 * Encodes picked image Uri into a scaled and compressed JPEG base64 Data URL.
 * Compresses images so that storing profile pics and ID card photos in Firestore
 * does not exceed the 1 MB Firestore document limit.
 */
object ImageUtils {
    fun uriToBase64DataUrl(context: Context, uri: Uri): String? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()
            if (originalBitmap == null) return null

            // Scale down bitmap to max 600px width/height to keep document size < 30KB
            val maxDimension = 600
            val width = originalBitmap.width
            val height = originalBitmap.height
            val scale = if (width > maxDimension || height > maxDimension) {
                val max = maxOf(width, height)
                maxDimension.toFloat() / max.toFloat()
            } else {
                1.0f
            }

            val scaledBitmap = if (scale < 1.0f) {
                Bitmap.createScaledBitmap(
                    originalBitmap,
                    (width * scale).toInt().coerceAtLeast(1),
                    (height * scale).toInt().coerceAtLeast(1),
                    true
                )
            } else {
                originalBitmap
            }

            val outputStream = ByteArrayOutputStream()
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 65, outputStream)
            val bytes = outputStream.toByteArray()
            val encoded = Base64.encodeToString(bytes, Base64.NO_WRAP)
            "data:image/jpeg;base64,$encoded"
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

