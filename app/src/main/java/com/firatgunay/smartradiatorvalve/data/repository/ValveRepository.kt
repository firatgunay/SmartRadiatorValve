package com.firatgunay.smartradiatorvalve.data.repository

import com.firatgunay.smartradiatorvalve.data.model.DeviceStatus
import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton
import com.firatgunay.smartradiatorvalve.data.model.Schedule
import com.firatgunay.smartradiatorvalve.mqtt.MqttClient
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import com.firatgunay.smartradiatorvalve.data.local.dao.ScheduleDao
import com.firatgunay.smartradiatorvalve.ml.TemperaturePredictor
import kotlinx.coroutines.flow.Flow


@Singleton
class ValveRepository @Inject constructor(
    private val mqttClient: MqttClient,
    private val scheduleDao: ScheduleDao,
    private val temperaturePredictor: TemperaturePredictor
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val _deviceStatus = MutableStateFlow(DeviceStatus())
    val deviceStatus: StateFlow<DeviceStatus> = _deviceStatus.asStateFlow()

    private var updateJob: Job? = null

    init {
        setupMqttCallbacks()
        connectMqtt()
        startOptimalTemperatureUpdates()
    }

    private fun setupMqttCallbacks() {
        try {
            mqttClient.setCallback { topic, message ->
                when (topic) {
                    "valve/temperature" -> {
                        message.toFloatOrNull()?.let { temp ->
                            _deviceStatus.update { currentStatus ->
                                currentStatus.copy(currentTemperature = temp)
                            }
                        }
                    }
                    "valve/outside_temperature" -> {
                        message.toFloatOrNull()?.let { temp ->
                            _deviceStatus.update { currentStatus ->
                                currentStatus.copy(outsideTemperature = temp)
                            }
                        }
                    }
                    "valve/humidity" -> {
                        message.toFloatOrNull()?.let { humidity ->
                            _deviceStatus.update { currentStatus ->
                                currentStatus.copy(humidity = humidity)
                            }
                        }
                    }
                    "valve/status" -> {
                        message.toBooleanStrictOrNull()?.let { isHeating ->
                            _deviceStatus.update { currentStatus ->
                                currentStatus.copy(isHeating = isHeating)
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("ValveRepository", "MQTT callback kurulumu hatası", e)
        }
    }

    private fun connectMqtt() {
        try {
            mqttClient.connect()
        } catch (e: Exception) {
            Log.e("ValveRepository", "MQTT bağlantı hatası", e)
        }
    }

    fun getSchedulesForDay(dayOfWeek: Int): Flow<List<Schedule>> {
        return scheduleDao.getSchedulesForDay(dayOfWeek)
    }

    suspend fun addSchedule(schedule: Schedule) {
        scheduleDao.insertSchedule(schedule)
        publishScheduleUpdate()
    }

    suspend fun insertSchedule(schedule: Schedule) {
        scheduleDao.insertSchedule(schedule)
        publishScheduleUpdate()
    }

    suspend fun deleteSchedule(id: Long) {
        scheduleDao.deleteScheduleById(id)
        publishScheduleUpdate()
    }

    suspend fun updateSchedule(schedule: Schedule) {
        scheduleDao.updateSchedule(schedule)
        publishScheduleUpdate()
    }

    private fun startOptimalTemperatureUpdates() {
        updateJob?.cancel()
        updateJob = scope.launch {
            while (isActive) {
                try {
                    val currentStatus = deviceStatus.value
                    val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
                    
                    val optimalTemp = temperaturePredictor.predictOptimalTemperature(
                        currentTemp = currentStatus.currentTemperature,
                        outsideTemp = currentStatus.outsideTemperature,
                        humidity = currentStatus.humidity,
                        hour = currentHour
                    )
                    
                    // Optimal sıcaklığı MQTT üzerinden ESP8266'ya gönder
                    mqttClient.publishMessage("valve/target_temperature", optimalTemp.toString())
                    
                    delay(15 * 60 * 1000) // 15 dakikada bir güncelle
                } catch (e: Exception) {
                    Log.e("ValveRepository", "Optimal sıcaklık hesaplama hatası", e)
                    delay(60 * 1000) // Hata durumunda 1 dakika bekle
                }
            }
        }
    }

    private suspend fun publishScheduleUpdate() {
        try {
            val schedules = scheduleDao.getAllSchedules()
            val scheduleJson = Json.encodeToString(schedules)
            mqttClient.publishMessage("valve/schedules", scheduleJson)
        } catch (e: Exception) {
            Log.e("ValveRepository", "Program güncellemesi yayınlanırken hata", e)
        }
    }

    suspend fun cleanup() {
        try {
            updateJob?.cancel()
            updateJob = null
            mqttClient.disconnect()
            scope.launch { 
                temperaturePredictor.cleanup()
            }
        } catch (e: Exception) {
            Log.e("ValveRepository", "Cleanup hatası", e)
        }
    }
} 