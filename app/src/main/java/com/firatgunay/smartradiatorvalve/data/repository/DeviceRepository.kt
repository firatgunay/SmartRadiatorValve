package com.firatgunay.smartradiatorvalve.data.repository

import android.util.Log
import com.firatgunay.smartradiatorvalve.data.model.DeviceStatus
import com.firatgunay.smartradiatorvalve.data.model.LcdDisplay
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeviceRepository @Inject constructor(
    private val database: FirebaseDatabase
) {
    private var statusListener: ValueEventListener? = null

    fun updateDeviceStatus(temperature: Float, isValveOpen: Boolean) {
        try {
            val updates = mapOf(
                "temperature" to temperature,
                "isValveOpen" to isValveOpen,
                "lastUpdate" to System.currentTimeMillis()
            )
            
            database.reference.child("esp_status")
                .updateChildren(updates)
                .addOnFailureListener { e ->
                    Log.e("DeviceRepository", "Cihaz durumu güncellenirken hata", e)
                }
        } catch (e: Exception) {
            Log.e("DeviceRepository", "Cihaz durumu güncellenirken hata", e)
        }
    }

    fun observeDeviceStatus(): Flow<DeviceStatus> = callbackFlow {
        statusListener = database.reference.child("esp_status")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    try {
                        val temperature = snapshot.child("temperature").getValue(Float::class.java) ?: 0f
                        val humidity = snapshot.child("humidity").getValue(Float::class.java) ?: 0f
                        val isValveOpen = snapshot.child("isValveOpen").getValue(Boolean::class.java) ?: false
                        val targetTemperature = snapshot.child("targetTemperature").getValue(Float::class.java) ?: 21f
                        val lastUpdate = snapshot.child("lastUpdate").getValue(Long::class.java) ?: System.currentTimeMillis()
                        val isConnected = snapshot.child("isConnected").getValue(Boolean::class.java) ?: false
                        
                        // LCD ekran verilerini al
                        val line1 = snapshot.child("lcd").child("line1").getValue(String::class.java) ?: ""
                        val line2 = snapshot.child("lcd").child("line2").getValue(String::class.java) ?: ""
                        
                        val status = DeviceStatus(
                            temperature = temperature,
                            humidity = humidity,
                            isValveOpen = isValveOpen,
                            targetTemperature = targetTemperature,
                            lastUpdate = lastUpdate,
                            isConnected = isConnected,
                            lcdDisplay = LcdDisplay(line1, line2)
                        )
                        trySend(status)
                    } catch (e: Exception) {
                        Log.e("DeviceRepository", "Veri okuma hatası", e)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("DeviceRepository", "Firebase hatası: ${error.message}")
                    close(error.toException())
                }
            })

        awaitClose { 
            statusListener?.let {
                database.reference.child("esp_status").removeEventListener(it)
            }
        }
    }

    fun cleanup() {
        try {
            statusListener?.let {
                database.reference.child("esp_status").removeEventListener(it)
                statusListener = null
            }
        } catch (e: Exception) {
            Log.e("DeviceRepository", "Cleanup hatası", e)
        }
    }
} 