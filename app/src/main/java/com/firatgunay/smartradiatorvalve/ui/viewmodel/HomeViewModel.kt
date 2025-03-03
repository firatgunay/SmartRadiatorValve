package com.firatgunay.smartradiatorvalve.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.firatgunay.smartradiatorvalve.data.model.Room
import com.firatgunay.smartradiatorvalve.data.repository.RoomRepository
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ServerValue
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

sealed class HomeUiState {
    data object Loading : HomeUiState()
    data class Success(val rooms: List<Room>) : HomeUiState()
    data class Error(val message: String) : HomeUiState()
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val database: FirebaseDatabase,
    private val roomRepository: RoomRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState
    
    private var roomsListener: ValueEventListener? = null
    private var espStatusListener: ValueEventListener? = null

    init {
        Log.d("HomeViewModel", "Database URL: ${database.reference.root}")
        loadRooms()
        monitorEspStatus()
    }

    fun loadRooms() {
        viewModelScope.launch {
            try {
                _uiState.value = HomeUiState.Loading
                val roomsRef = database.getReference("rooms")
                
                roomsListener?.let { 
                    roomsRef.removeEventListener(it)
                }
                
                roomsListener = object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        try {
                            Log.d("HomeViewModel", "Veri alındı: ${snapshot.exists()}")
                            if (!snapshot.exists()) {
                                initializeDefaultRooms()
                                return
                            }

                            val rooms = snapshot.children.mapNotNull { child ->
                                try {
                                    child.getValue(Room::class.java)?.copy(id = child.key ?: "")
                                } catch (e: Exception) {
                                    Log.e("HomeViewModel", "Oda dönüştürme hatası: ${e.message}")
                                    null
                                }
                            }
                            
                            _uiState.value = HomeUiState.Success(rooms)
                        } catch (e: Exception) {
                            Log.e("HomeViewModel", "Veri işleme hatası", e)
                            _uiState.value = HomeUiState.Error("Veri işleme hatası: ${e.message}")
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("HomeViewModel", "Firebase hatası: ${error.message}")
                        _uiState.value = HomeUiState.Error(error.message)
                    }
                }
                
                roomsRef.addValueEventListener(roomsListener!!)
                
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Genel hata", e)
                _uiState.value = HomeUiState.Error("Bağlantı hatası: ${e.message}")
            }
        }
    }

    private fun initializeDefaultRooms() {
        viewModelScope.launch {
            try {
                Log.d("HomeViewModel", "Varsayılan odalar oluşturuluyor")
                val roomsRef = database.getReference("rooms")
                val defaultRooms = listOf(
                    Room(
                        id = "living_room",
                        name = "Salon",
                        currentTemperature = 22f,
                        targetTemperature = 23f,
                        isHeating = false
                    ),
                    Room(
                        id = "bedroom",
                        name = "Yatak Odası",
                        currentTemperature = 21f,
                        targetTemperature = 22f,
                        isHeating = false
                    )
                )

                defaultRooms.forEach { room ->
                    roomsRef.child(room.id).setValue(room)
                        .addOnSuccessListener {
                            Log.d("HomeViewModel", "${room.name} oluşturuldu")
                        }
                        .addOnFailureListener { e ->
                            Log.e("HomeViewModel", "${room.name} oluşturulamadı", e)
                        }
                }
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Varsayılan oda oluşturma hatası", e)
                _uiState.value = HomeUiState.Error("Varsayılan odalar oluşturulamadı: ${e.message}")
            }
        }
    }

    fun updateTargetTemperature(roomId: String, temperature: Float) {
        viewModelScope.launch {
            try {
                database.getReference("rooms")
                    .child(roomId)
                    .child("targetTemperature")
                    .setValue(temperature)
            } catch (e: Exception) {
                _uiState.value = HomeUiState.Error(e.message ?: "Sıcaklık güncellenirken hata oluştu")
            }
        }
    }

    fun addRoom(name: String, initialTemp: Float) {
        viewModelScope.launch {
            try {
                val roomsRef = database.getReference("rooms")
                val newRoomId = roomsRef.push().key ?: return@launch

                val newRoom = Room(
                    id = newRoomId,
                    name = name,
                    currentTemperature = initialTemp,
                    targetTemperature = initialTemp,
                    isHeating = false
                )

                roomsRef.child(newRoomId).setValue(newRoom)
                    .addOnFailureListener { exception ->
                        _uiState.value = HomeUiState.Error("Oda eklenirken hata oluştu: ${exception.message}")
                    }
            } catch (e: Exception) {
                _uiState.value = HomeUiState.Error("Oda eklenirken hata oluştu: ${e.message}")
            }
        }
    }

    fun updateRoomTemperature(roomId: String, temperature: Float, humidity: Int) {
        viewModelScope.launch {
            try {
                val roomRef = database.getReference("rooms").child(roomId)
                val updates = hashMapOf<String, Any>(
                    "currentTemperature" to temperature,
                    "humidity" to humidity,
                    "lastUpdate" to System.currentTimeMillis(),
                    "isHeating" to (temperature < getTargetTemperature(roomId))
                )
                
                roomRef.updateChildren(updates)
                    .addOnFailureListener { exception ->
                        _uiState.value = HomeUiState.Error("Sıcaklık güncellenirken hata: ${exception.message}")
                    }
            } catch (e: Exception) {
                _uiState.value = HomeUiState.Error("Sıcaklık güncellenirken hata: ${e.message}")
            }
        }
    }

    private suspend fun getTargetTemperature(roomId: String): Float {
        return try {
            val snapshot = database.getReference("rooms")
                .child(roomId)
                .child("targetTemperature")
                .get()
                .await()
            
            snapshot.getValue(Float::class.java) ?: 21f
        } catch (e: Exception) {
            21f // Varsayılan değer
        }
    }

    // ESP8266'dan gelen verileri işlemek için
    fun processEspData(data: Map<String, Any>) {
        viewModelScope.launch {
            try {
                data.forEach { (roomId, values) ->
                    when (values) {
                        is Map<*, *> -> {
                            val temp = (values["temperature"] as? Number)?.toFloat()
                            val humidity = (values["humidity"] as? Number)?.toInt()
                            
                            if (temp != null && humidity != null) {
                                updateRoomTemperature(roomId, temp, humidity)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("HomeViewModel", "ESP verisi işlenirken hata", e)
            }
        }
    }

    private fun monitorEspStatus() {
        try {
            espStatusListener?.let {
                database.getReference(".info/connected").removeEventListener(it)
                espStatusListener = null
            }

            espStatusListener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val connected = snapshot.getValue(Boolean::class.java) ?: false
                    updateEspStatus(connected)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("HomeViewModel", "ESP bağlantı durumu izlenirken hata", error.toException())
                }
            }

            database.getReference(".info/connected").addValueEventListener(espStatusListener!!)
        } catch (e: Exception) {
            Log.e("HomeViewModel", "ESP durumu izleme hatası", e)
        }
    }

    private fun updateEspStatus(isConnected: Boolean) {
        database.getReference("esp_status").updateChildren(
            mapOf(
                "connected" to isConnected,
                "lastHeartbeat" to ServerValue.TIMESTAMP
            )
        )
    }

    fun deleteRoom(roomId: String) {
        viewModelScope.launch {
            try {
                database.getReference("rooms")
                    .child(roomId)
                    .removeValue()
                    .addOnFailureListener { exception ->
                        _uiState.value = HomeUiState.Error("Oda silinirken hata oluştu: ${exception.message}")
                    }
            } catch (e: Exception) {
                _uiState.value = HomeUiState.Error("Oda silinirken hata oluştu: ${e.message}")
            }
        }
    }

    fun cleanup() {
        try {
            // Mevcut listener'ları temizle
            roomsListener?.let { 
                database.getReference("rooms").removeEventListener(it)
                roomsListener = null
            }
            espStatusListener?.let {
                database.getReference(".info/connected").removeEventListener(it)
                espStatusListener = null
            }
            
            // Repository cleanup'ı çağır
            roomRepository.cleanup()
            
            // UI state'i sıfırla
            _uiState.value = HomeUiState.Loading
            
        } catch (e: Exception) {
            Log.e("HomeViewModel", "Cleanup hatası", e)
        }
    }

    override fun onCleared() {
        super.onCleared()
        cleanup()
    }
} 