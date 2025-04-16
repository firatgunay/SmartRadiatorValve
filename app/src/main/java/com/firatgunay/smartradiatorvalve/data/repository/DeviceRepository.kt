import android.util.Log
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

    fun updateDeviceStatus(temperature: Float, isHeating: Boolean) {
        try {
            val updates = mapOf(
                "temperature" to temperature,
                "isHeating" to isHeating,
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
                        val status = snapshot.getValue(DeviceStatus::class.java)
                        status?.let { trySend(it) }
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