package com.firatgunay.smartradiatorvalve.ml

import android.content.Context
import android.util.Log
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import javax.inject.Inject

class TemperaturePredictor @Inject constructor(
    private val context: Context
) {
    private var interpreter: Interpreter? = null
    
    init {
        loadModel()
    }

    private fun loadModel() {
        try {
            val modelFile = "temperature_model.tflite"
            interpreter = Interpreter(loadModelFile(modelFile))
        } catch (e: Exception) {
            Log.e("TemperaturePredictor", "Model yüklenirken hata", e)
        }
    }

    private fun loadModelFile(modelFile: String): MappedByteBuffer {
        val fileDescriptor = context.assets.openFd(modelFile)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    fun predictOptimalTemperature(
        currentTemp: Float,
        outsideTemp: Float,
        humidity: Float,
        timeOfDay: Int,
        dayOfWeek: Int
    ): Float {
        try {
            // Model input boyutlarına göre düzenleyin
            val inputArray = floatArrayOf(
                currentTemp,
                outsideTemp,
                humidity,
                timeOfDay.toFloat(),
                dayOfWeek.toFloat()
            )
            
            val outputArray = FloatArray(1) // Çıktı boyutuna göre ayarlayın
            
            interpreter?.run(inputArray, outputArray)
            
            return outputArray[0]
        } catch (e: Exception) {
            Log.e("TemperaturePredictor", "Tahmin hatası", e)
            // Hata durumunda basit hesaplama
            return (currentTemp + outsideTemp) / 2
        }
    }

    fun cleanup() {
        try {
            interpreter?.close()
            interpreter = null
        } catch (e: Exception) {
            Log.e("TemperaturePredictor", "Cleanup hatası", e)
        }
    }
} 