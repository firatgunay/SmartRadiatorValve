package com.firatgunay.smartradiatorvalve.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "valve_data")
data class ValveData(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val temperature: Float,
    val outsideTemperature: Float,
    val humidity: Float,
    val isHeating: Boolean,
    val timestamp: Long = System.currentTimeMillis()
) 