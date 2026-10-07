package com.example.recipeapplication.data

import android.content.Context
import android.net.Uri
import java.io.File

object ImageStorage {
    fun copy(context: Context, uri: Uri): String {
        val file = File(context.filesDir, "recipe_${System.currentTimeMillis()}.jpg")
        context.contentResolver.openInputStream(uri)!!.use { input ->
            file.outputStream().use { input.copyTo(it) }
        }
        return file.absolutePath
    }

    fun delete(path: String?) {
        if (!path.isNullOrEmpty()) File(path).delete()
    }
}