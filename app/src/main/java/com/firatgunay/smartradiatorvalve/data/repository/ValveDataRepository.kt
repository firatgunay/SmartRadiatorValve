package com.firatgunay.smartradiatorvalve.data.repository

import android.util.Log
import com.firatgunay.smartradiatorvalve.data.local.dao.ValveDataDao
import com.firatgunay.smartradiatorvalve.data.model.ValveData
import com.firatgunay.smartradiatorvalve.mqtt.MqttClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton
import java.util.Date

@Singleton
class ValveDataRepository @Inject constructor(
    private val valveDataDao: ValveDataDao,
    private val mqttClient: MqttClient
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    private val _currentData = MutableStateFlow<ValveData?>(null)
    val currentData: StateFlow<ValveData?> = _currentData.asStateFlow()
    
    private val _connectionStatus = MutableStateFlow(false)
    val connectionStatus: StateFlow<Boolean> = _connectionStatus.asStateFlow()

    init {
        setupMqttConnection()
        startDataCleanup()
    }

    private fun setupMqttConnection() {
        mqttClient.setCallback { topic, message ->
            when (topic) {
                "valve/data" -> processValveData(message)
                "valve/temperature" -> processTemperature(message)
                "valve/humidity" -> processHumidity(message)
                "valve/status" -> processValveStatus(message)
            }
        }

        // MQTT bağlantısını başlat
        mqttClient.connect()
    }

    private fun processValveData(jsonString: String) {
        try {
            val json = JSONObject(jsonString)
            val data = ValveData(
                temperature = json.optDouble("temperature", 0.0).toFloat(),
                humidity = json.optDouble("humidity", 0.0).toFloat(),
                isValveOpen = json.optBoolean("isValveOpen", false),
                timestamp = Date().time
            )
            
            scope.launch {
                valveDataDao.insertData(data)
                _currentData.value = data
            }
        } catch (e: Exception) {
            Log.e(TAG, "JSON işleme hatası", e)
        }
    }

    private fun processTemperature(message: String) {
        message.toFloatOrNull()?.let { temp ->
            _currentData.value = _currentData.value?.copy(
                temperature = temp,
                timestamp = Date().time
            ) ?: ValveData(temperature = temp, timestamp = Date().time)
        }
    }

    private fun processHumidity(message: String) {
        message.toFloatOrNull()?.let { humidity ->
            _currentData.value = _currentData.value?.copy(
                humidity = humidity,
                timestamp = Date().time
            ) ?: ValveData(humidity = humidity, timestamp = Date().time)
        }
    }

    private fun processValveStatus(message: String) {
        val isValveOpen = message.equals("1", ignoreCase = true)
        _currentData.value = _currentData.value?.copy(
            isValveOpen = isValveOpen,
            timestamp = Date().time
        ) ?: ValveData(isValveOpen = isValveOpen, timestamp = Date().time)
    }

    fun setTargetTemperature(temperature: Float) {
        mqttClient.setTargetTemperature(temperature)
    }

    fun getLatestData(): Flow<ValveData?> = valveDataDao.getLatestData()

    fun getRecentData(limit: Int): Flow<List<ValveData>> = valveDataDao.getRecentData(limit)

    private fun startDataCleanup() {
        scope.launch {
            try {
                // 7 günden eski verileri temizle
                val cutoffTime = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000)
                valveDataDao.deleteOldData(cutoffTime)
            } catch (e: Exception) {
                Log.e(TAG, "Veri temizleme hatası", e)
            }
        }
    }

    fun cleanup() {
        mqttClient.disconnect()
    }

    companion object {
        private const val TAG = "ValveDataRepository"
    }
} 