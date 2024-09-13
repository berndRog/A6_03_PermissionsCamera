package de.rogallab.mobile.data.io

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.core.net.toFile
import de.rogallab.mobile.domain.utilities.logError
import java.io.File
import java.io.IOException
import java.util.*

// read image from internal storage
fun readImageFromStorage(uri: Uri): Bitmap? =
   try {
      BitmapFactory.decodeFile(uri.toFile().absolutePath)
         ?: throw IOException("BitmapFactory.decodeFile() returned null")
   } catch (e: IOException) {
      logError("[readImageFromInternalStorage]", e.localizedMessage)
      throw e
   }

// write image to internal storage
// return absolute path of file (local URL)
fun writeImageToStorage(
   context: Context,
   bitmap: Bitmap
): String? =
   try {
      // directory: .../app_images/...
      val imagesDir: File = context.getDir("images", Context.MODE_PRIVATE)
      // file: .../app_images/{UUID}.jpg
      val file = File(imagesDir, "${UUID.randomUUID()}.jpg")
      // compress bitmap to file and return absolute path
      file.outputStream().use { out ->
         bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
         out.flush()
         file.absolutePath // return absolute path
      }
   } catch (e: IOException) {
      logError("[writeImageToInternalStorage]", e.localizedMessage)
      throw e
   }


fun deleteFileOnStorage(fileName:String) {
   try {
      File(fileName).apply {
         this.absoluteFile.delete()
      }
   } catch(e:IOException ) {
      logError("deleteFileOnInternalStorage","Error deleting file + ${e.localizedMessage}")
   }
}