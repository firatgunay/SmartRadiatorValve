package com.firatgunay.smartradiatorvalve.util

import android.content.Context
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

object ModelUtils {
    fun copyModelToCache(context: Context, modelName: String): File {
        val modelFile = File(context.cacheDir, modelName)
        
        if (!modelFile.exists()) {
            try {
                context.assets.open(modelName).use { inputStream ->
                    FileOutputStream(modelFile).use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
            } catch (e: IOException) {
                Log.e("ModelUtils", "Model kopyalama hatası", e)
                throw e
            }
        }
        
        return modelFile
    }
} 