package com.firatgunay.smartradiatorvalve.ml

import android.content.Context
import javax.inject.Inject

class TemperaturePredictor @Inject constructor(
    private val context: Context
) {
    fun predictOptimalTemperature(
        currentTemp: Float,
        outsideTemp: Float,
        humidity: Float,
        hour: Int
    ): Float {
        // Basit bir sıcaklık tahmin algoritması
        // Bu algoritmayı ihtiyaçlarınıza göre geliştirebilirsiniz
        
        // Gece saatlerinde (22:00 - 06:00) sıcaklığı düşür
        val isNightTime = hour in 22..23 || hour in 0..6
        val nightReduction = if (isNightTime) 2f else 0f
        
        // Dış sıcaklık çok düşükse iç sıcaklığı biraz artır
        val coldCompensation = if (outsideTemp < 5) 1f else 0f
        
        // Nem oranı yüksekse sıcaklığı biraz düşür
        val humidityCompensation = if (humidity > 70) -0.5f else 0f
        
        // Baz sıcaklık 21 derece
        val baseTemperature = 21f
        
        return baseTemperature + coldCompensation + humidityCompensation - nightReduction
    }

    fun cleanup() {
        // Gerekirse kaynakları temizle
    }
} 