package hr.foi.squadsync.data.export

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import java.io.File
import java.io.IOException

fun saveCsvToDownloads(context: Context, fileName: String, csv: String) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val resolver = context.contentResolver
        val values = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, fileName)
            put(MediaStore.Downloads.MIME_TYPE, "text/csv")
            put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            put(MediaStore.Downloads.IS_PENDING, 1)
        }
        val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
            ?: throw IOException("Nije moguće stvoriti CSV datoteku.")
        try {
            val stream = resolver.openOutputStream(uri)
                ?: throw IOException("Nije moguće otvoriti CSV datoteku.")
            stream.bufferedWriter(Charsets.UTF_8).use { it.write(csv) }
            val finished = ContentValues().apply { put(MediaStore.Downloads.IS_PENDING, 0) }
            if (resolver.update(uri, finished, null, null) == 0) {
                throw IOException("Nije moguće dovršiti spremanje CSV-a.")
            }
        } catch (exception: Exception) {
            resolver.delete(uri, null, null)
            throw exception
        }
    } else {
        // Android 9 treba dozvolu za pisanje u javnu mapu Download.
        @Suppress("DEPRECATION")
        val directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        if (!directory.exists() && !directory.mkdirs()) {
            throw IOException("Mapa Download nije dostupna.")
        }
        var file = File(directory, fileName)
        var number = 1
        while (file.exists()) {
            file = File(directory, "${fileName.removeSuffix(".csv")} ($number).csv")
            number++
        }
        try {
            file.writeText(csv, Charsets.UTF_8)
        } catch (exception: Exception) {
            file.delete()
            throw exception
        }
    }
}
