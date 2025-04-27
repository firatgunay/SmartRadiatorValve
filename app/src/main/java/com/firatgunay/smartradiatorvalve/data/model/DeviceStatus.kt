package com.firatgunay.smartradiatorvalve.data.model

import kotlinx.serialization.Serializable

@Serializable
data class DeviceStatus(
    val currentTemperature: Float = 0f,
    val outsideTemperature: Float = 0f,
    val humidity: Float = 0f,
    val isHeating: Boolean = false,
    val targetTemperature: Float = 21f,
    val lastUpdate: Long = System.currentTimeMillis(),
    val isConnected: Boolean = false
) 