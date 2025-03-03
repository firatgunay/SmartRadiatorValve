package com.firatgunay.smartradiatorvalve.data.repository

import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import javax.inject.Inject
import javax.inject.Singleton
import android.util.Log

@Singleton
class RoomRepository @Inject constructor(
    private val database: FirebaseDatabase
) {
    private var roomsListener: ValueEventListener? = null
    private var espStatusListener: ValueEventListener? = null

    fun cleanup() {
        try {
            roomsListener?.let { 
                database.getReference("rooms").removeEventListener(it)
                roomsListener = null
            }
            espStatusListener?.let {
                database.getReference(".info/connected").removeEventListener(it)
                espStatusListener = null
            }
            
            database.goOffline()
            
            Log.d("RoomRepository", "Cleanup başarılı")
        } catch (e: Exception) {
            Log.e("RoomRepository", "Cleanup hatası", e)
        }
    }
} 