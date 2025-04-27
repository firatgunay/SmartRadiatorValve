package com.firatgunay.smartradiatorvalve.data.model

import kotlinx.serialization.Serializable

@Serializable
data class DeviceStatus(
    val temperature: Float = 0f,        // DHT11'den gelen sıcaklık
    val humidity: Float = 0f,           // DHT11'den gelen nem
    val isValveOpen: Boolean = false,   // Valf durumu
    val targetTemperature: Float = 21f, // Hedef sıcaklık
    val lastUpdate: Long = System.currentTimeMillis(),
    val isConnected: Boolean = false,
    val lcdDisplay: LcdDisplay = LcdDisplay()  // LCD ekran durumu
)

@Serializable
data class LcdDisplay(
    val line1: String = "",
    val line2: String = ""
) 