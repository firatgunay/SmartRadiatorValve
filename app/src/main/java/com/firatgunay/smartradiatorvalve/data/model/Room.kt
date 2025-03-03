package com.firatgunay.smartradiatorvalve.data.model

import com.google.firebase.database.PropertyName

data class Room(
    @get:PropertyName("id") @set:PropertyName("id")
    var id: String = "",
    
    @get:PropertyName("name") @set:PropertyName("name")
    var name: String = "",
    
    @get:PropertyName("currentTemperature") @set:PropertyName("currentTemperature")
    var currentTemperature: Float = 0f,
    
    @get:PropertyName("targetTemperature") @set:PropertyName("targetTemperature")
    var targetTemperature: Float = 0f,
    
    @get:PropertyName("isHeating") @set:PropertyName("isHeating")
    var isHeating: Boolean = false,
    
    @get:PropertyName("humidity") @set:PropertyName("humidity")
    var humidity: Int = 0,
    
    @get:PropertyName("lastUpdate") @set:PropertyName("lastUpdate")
    var lastUpdate: Long = 0
) {
    // Firebase için boş constructor gerekli
    constructor() : this("", "", 0f, 0f, false, 0, 0)
}

enum class RoomType {
    LIVING_ROOM,
    BEDROOM,
    KITCHEN,
    BATHROOM
} 