package com.firatgunay.smartradiatorvalve.ml

import android.content.Context
import org.tensorflow.lite.Interpreter
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
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
            val modelFile = File(context.getExternalFilesDir(null), "temperature_model.tflite")
            interpreter = Interpreter(modelFile)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    fun predictOptimalTemperature(
        currentTemp: Float,
        outsideTemp: Float,
        humidity: Float,
        hour: Int
    ): Float {
        val inputBuffer = ByteBuffer.allocateDirect(4 * 4) // 4 input features * 4 bytes per float
        inputBuffer.order(ByteOrder.nativeOrder())
        
        inputBuffer.putFloat(currentTemp)
        inputBuffer.putFloat(outsideTemp)
        inputBuffer.putFloat(humidity)
        inputBuffer.putFloat(hour.toFloat())
        
        val outputBuffer = ByteBuffer.allocateDirect(4) // 1 output * 4 bytes per float
        outputBuffer.order(ByteOrder.nativeOrder())
        
        interpreter?.run(inputBuffer, outputBuffer)
        
        outputBuffer.rewind()
        return outputBuffer.float
    }
    
    fun cleanup() {
        interpreter?.close()
    }
} 