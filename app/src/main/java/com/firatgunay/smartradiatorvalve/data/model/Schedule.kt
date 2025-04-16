package com.firatgunay.smartradiatorvalve.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "schedules")
data class Schedule(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val dayOfWeek: Int,
    val startTime: String,
    val endTime: String,
    val targetTemperature: Float,
    val isEnabled: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun isActiveAt(time: String): Boolean {
        return startTime <= time && time <= endTime
    }

    fun overlaps(other: Schedule): Boolean {
        return dayOfWeek == other.dayOfWeek &&
                !((endTime <= other.startTime) || (startTime >= other.endTime))
    }

    companion object {
        fun createDefault(dayOfWeek: Int): Schedule {
            return Schedule(
                dayOfWeek = dayOfWeek,
                startTime = "08:00",
                endTime = "17:00",
                targetTemperature = 21.0f
            )
        }
    }
}

enum class DayOfWeek(val turkishName: String) {
    MONDAY("Pazartesi"),
    TUESDAY("Salı"),
    WEDNESDAY("Çarşamba"),
    THURSDAY("Perşembe"),
    FRIDAY("Cuma"),
    SATURDAY("Cumartesi"),
    SUNDAY("Pazar")
} 