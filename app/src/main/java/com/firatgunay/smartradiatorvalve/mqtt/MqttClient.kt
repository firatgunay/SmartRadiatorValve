package com.firatgunay.smartradiatorvalve.mqtt

import android.content.Context
import android.util.Log
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.*
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MqttClient @Inject constructor(
    private val context: Context
) {
    private val mqttClient = MqttAndroidClient(
        context,
        "tcp://broker.hivemq.com:1883",
        "android_${UUID.randomUUID()}"
    )

    private var messageCallback: ((String, String) -> Unit)? = null
    private var connectionCallback: ((Boolean) -> Unit)? = null
    
    private val _connectionStatus = MutableStateFlow(false)
    val connectionStatus: StateFlow<Boolean> = _connectionStatus.asStateFlow()

    // MQTT Topics
    private val topics = arrayOf(
        "valve/data",
        "valve/temperature",
        "valve/outside_temperature",
        "valve/humidity",
        "valve/status"
    )

    fun connect() {
        try {
            val options = MqttConnectOptions().apply {
                isAutomaticReconnect = true
                isCleanSession = true
            }

            mqttClient.connect(options, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    Log.d(TAG, "MQTT bağlantısı başarılı")
                    _connectionStatus.value = true
                    connectionCallback?.invoke(true)
                    subscribeToTopics()
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    Log.e(TAG, "MQTT bağlantı hatası", exception)
                    _connectionStatus.value = false
                    connectionCallback?.invoke(false)
                }
            })

            setupMqttCallback()
        } catch (e: Exception) {
            Log.e(TAG, "MQTT bağlantı hatası", e)
            _connectionStatus.value = false
            connectionCallback?.invoke(false)
        }
    }

    private fun setupMqttCallback() {
        mqttClient.setCallback(object : MqttCallback {
            override fun connectionLost(cause: Throwable?) {
                Log.e(TAG, "MQTT bağlantısı koptu", cause)
                _connectionStatus.value = false
                connectionCallback?.invoke(false)
            }

            override fun messageArrived(topic: String?, message: MqttMessage?) {
                message?.let {
                    val messageStr = String(it.payload)
                    Log.d(TAG, "MQTT mesajı alındı: $topic -> $messageStr")
                    messageCallback?.invoke(topic ?: "", messageStr)
                }
            }

            override fun deliveryComplete(token: IMqttDeliveryToken?) {
                Log.d(TAG, "MQTT mesajı iletildi")
            }
        })
    }

    private fun subscribeToTopics() {
        topics.forEach { topic ->
            try {
                mqttClient.subscribe(topic, 0, null, object : IMqttActionListener {
                    override fun onSuccess(asyncActionToken: IMqttToken?) {
                        Log.d(TAG, "Topic aboneliği başarılı: $topic")
                    }

                    override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                        Log.e(TAG, "Topic aboneliği başarısız: $topic", exception)
                    }
                })
            } catch (e: Exception) {
                Log.e(TAG, "Topic aboneliği hatası: $topic", e)
            }
        }
    }

    fun setCallback(callback: (String, String) -> Unit) {
        messageCallback = callback
    }

    fun setConnectionCallback(callback: (Boolean) -> Unit) {
        connectionCallback = callback
    }

    fun publishMessage(topic: String, message: String, qos: Int = 0) {
        try {
            val mqttMessage = MqttMessage(message.toByteArray()).apply {
                this.qos = qos
                isRetained = false
            }
            
            mqttClient.publish(topic, mqttMessage, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    Log.d(TAG, "Mesaj başarıyla gönderildi: $topic -> $message")
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    Log.e(TAG, "Mesaj gönderme hatası: $topic", exception)
                }
            })
        } catch (e: Exception) {
            Log.e(TAG, "Mesaj gönderme hatası", e)
        }
    }

    fun setTargetTemperature(temperature: Float) {
        publishMessage("valve/target_temperature", temperature.toString())
    }

    fun disconnect() {
        try {
            mqttClient.disconnect(null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    Log.d(TAG, "MQTT bağlantısı kapatıldı")
                    _connectionStatus.value = false
                    connectionCallback?.invoke(false)
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    Log.e(TAG, "MQTT bağlantısı kapatılırken hata", exception)
                }
            })
        } catch (e: Exception) {
            Log.e(TAG, "MQTT bağlantısı kapatılırken hata", e)
        }
    }

    companion object {
        private const val TAG = "MqttClient"
    }
} 