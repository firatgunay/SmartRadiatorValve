package com.firatgunay.smartradiatorvalve.data.model

data class Schedule(
    val id: String = "",
    val roomId: String = "",
    val dayOfWeek: Int = 0, // 1 = Pazartesi, 7 = Pazar
    val startTime: String = "00:00",
    val endTime: String = "23:59",
    val targetTemperature: Float = 21.0f,
    val isActive: Boolean = true
)

enum class DayOfWeek(val turkishName: String) {
    MONDAY("Pazartesi"),
    TUESDAY("Salı"),
    WEDNESDAY("Çarşamba"),
    THURSDAY("Perşembe"),
    FRIDAY("Cuma"),
    SATURDAY("Cumartesi"),
    SUNDAY("Pazar")
} 