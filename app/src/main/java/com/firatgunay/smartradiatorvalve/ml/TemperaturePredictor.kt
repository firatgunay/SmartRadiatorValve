package com.firatgunay.smartradiatorvalve.ml

import android.content.Context
import android.util.Log
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TemperaturePredictor @Inject constructor(
    private val context: Context
) {
    private var interpreter: Interpreter? = null
    private val modelName = "temperature_model.tflite"

    init {
        loadModel()
    }

    private fun loadModel() {
        try {
            val modelBuffer = loadModelFile()
            interpreter = Interpreter(modelBuffer)
            Log.d(TAG, "Model başarıyla yüklendi")
        } catch (e: Exception) {
            Log.e(TAG, "Model yüklenirken hata oluştu", e)
        }
    }

    private fun loadModelFile(): MappedByteBuffer {
        val modelPath = context.assets.openFd(modelName)
        val inputStream = FileInputStream(modelPath.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = modelPath.startOffset
        val declaredLength = modelPath.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    fun predictOptimalTemperature(
        currentTemp: Float,
        outsideTemp: Float,
        humidity: Float,
        hour: Int
    ): Float {
        if (interpreter == null) {
            Log.e(TAG, "Interpreter yüklenmemiş")
            return DEFAULT_TEMPERATURE
        }

        try {
            // Giriş verilerini hazırla
            val inputArray = FloatArray(INPUT_SIZE) 
            inputArray[0] = currentTemp
            inputArray[1] = outsideTemp
            inputArray[2] = humidity
            inputArray[3] = normalizeHour(hour)

            // Çıkış verisi için dizi
            val outputArray = Array(1) { FloatArray(1) }

            // Modeli çalıştır
            interpreter?.run(arrayOf(inputArray), outputArray)

            // Sonucu döndür
            return outputArray[0][0].coerceIn(MIN_TEMPERATURE, MAX_TEMPERATURE)
        } catch (e: Exception) {
            Log.e(TAG, "Tahmin yapılırken hata oluştu", e)
            return DEFAULT_TEMPERATURE
        }
    }

    private fun normalizeHour(hour: Int): Float {
        return hour.toFloat() / 24.0f  // Saati 0-1 aralığına normalize et
    }

    fun cleanup() {
        try {
            interpreter?.close()
            interpreter = null
            Log.d(TAG, "Model başarıyla kapatıldı")
        } catch (e: Exception) {
            Log.e(TAG, "Model kapatılırken hata oluştu", e)
        }
    }

    companion object {
        private const val TAG = "TemperaturePredictor"
        private const val DEFAULT_TEMPERATURE = 21.0f
        private const val MIN_TEMPERATURE = 16.0f
        private const val MAX_TEMPERATURE = 28.0f
        private const val INPUT_SIZE = 4  // currentTemp, outsideTemp, humidity, hour
    }
} 