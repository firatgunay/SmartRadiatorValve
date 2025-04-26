package com.firatgunay.smartradiatorvalve.data.repository

import android.util.Log
import com.firatgunay.smartradiatorvalve.data.local.dao.ValveDataDao
import com.firatgunay.smartradiatorvalve.data.model.ValveData
import com.firatgunay.smartradiatorvalve.websocket.WebSocketClient
import com.firatgunay.smartradiatorvalve.mqtt.MqttClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@Singleton
class ValveDataRepository @Inject constructor(
    private val valveDataDao: ValveDataDao,
    private val mqttClient: MqttClient,
    private val webSocketClient: WebSocketClient
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    private val _connectionStatus = MutableStateFlow(false)
    val connectionStatus: StateFlow<Boolean> = _connectionStatus.asStateFlow()
    
    init {
        setupMqttListener()
        setupWebSocketListener()
        startDataCleanup()
        monitorConnection()
    }
    
    private fun setupMqttListener() {
        mqttClient.setCallback { topic, message ->
            if (topic == "valve/data") {
                processData(message)
            }
        }
    }
    
    private fun setupWebSocketListener() {
        webSocketClient.setMessageCallback { message ->
            processData(message)
        }
    }
    
    private fun processData(jsonString: String) {
        try {
            val json = JSONObject(jsonString)
            val data = ValveData(
                temperature = json.optDouble("temperature", 0.0).toFloat(),
                outsideTemperature = json.optDouble("outsideTemperature", 0.0).toFloat(),
                humidity = json.optDouble("humidity", 0.0).toFloat(),
                isHeating = json.optBoolean("isHeating", false)
            )
            
            scope.launch {
                valveDataDao.insertData(data)
            }
        } catch (e: Exception) {
            Log.e("ValveDataRepository", "Veri işleme hatası", e)
        }
    }
    
    fun getLatestData(): Flow<ValveData?> = valveDataDao.getLatestData()
    
    fun getRecentData(limit: Int): Flow<List<ValveData>> = valveDataDao.getRecentData(limit)
    
    private fun startDataCleanup() {
        scope.launch {
            try {
                // 7 günden eski verileri temizle
                val cutoffTime = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(7)
                valveDataDao.deleteOldData(cutoffTime)
            } catch (e: Exception) {
                Log.e("ValveDataRepository", "Veri temizleme hatası", e)
            }
        }
    }
    
    fun cleanup() {
        mqttClient.disconnect()
        webSocketClient.disconnect()
    }
    
    private fun monitorConnection() {
        mqttClient.setConnectionCallback { isConnected ->
            _connectionStatus.value = isConnected
        }
    }
} 