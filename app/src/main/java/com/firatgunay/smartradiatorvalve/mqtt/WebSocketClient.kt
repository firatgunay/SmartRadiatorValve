package com.firatgunay.smartradiatorvalve.mqtt

import android.content.Context
import android.util.Log
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.*
import java.util.UUID
import javax.inject.Inject

class WebSocketClient @Inject constructor(
    private val context: Context
) {
    private val mqttClient = MqttAndroidClient(
        context,
        "ws://broker.hivemq.com:8000/mqtt", // WebSocket URL'i
        "android_${UUID.randomUUID()}"
    )

    private var messageCallback: ((String, String) -> Unit)? = null

    fun connect() {
        val options = MqttConnectOptions().apply {
            isAutomaticReconnect = true
            isCleanSession = true
        }

        try {
            mqttClient.connect(options, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    Log.d("WebSocketClient", "WebSocket bağlantısı başarılı")
                    subscribeToTopics()
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    Log.e("WebSocketClient", "WebSocket bağlantı hatası", exception)
                }
            })

            mqttClient.setCallback(object : MqttCallback {
                override fun connectionLost(cause: Throwable?) {
                    Log.e("WebSocketClient", "WebSocket bağlantısı koptu", cause)
                }

                override fun messageArrived(topic: String?, message: MqttMessage?) {
                    message?.let {
                        val messageStr = String(it.payload)
                        Log.d("WebSocketClient", "Mesaj alındı: $topic -> $messageStr")
                        messageCallback?.invoke(topic ?: "", messageStr)
                    }
                }

                override fun deliveryComplete(token: IMqttDeliveryToken?) {
                    Log.d("WebSocketClient", "Mesaj iletildi")
                }
            })
        } catch (e: Exception) {
            Log.e("WebSocketClient", "WebSocket bağlantı hatası", e)
        }
    }

    fun setCallback(callback: (String, String) -> Unit) {
        messageCallback = callback
    }

    private fun subscribeToTopics() {
        try {
            val topics = arrayOf(
                "valve/temperature",
                "valve/outside_temperature",
                "valve/humidity",
                "valve/status",
                "valve/target_temperature"
            )
            
            topics.forEach { topic ->
                mqttClient.subscribe(topic, 1, null, object : IMqttActionListener {
                    override fun onSuccess(asyncActionToken: IMqttToken?) {
                        Log.d("WebSocketClient", "Topic subscription başarılı: $topic")
                    }

                    override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                        Log.e("WebSocketClient", "Topic subscription hatası: $topic", exception)
                    }
                })
            }
        } catch (e: Exception) {
            Log.e("WebSocketClient", "Topic subscription hatası", e)
        }
    }

    fun publishMessage(topic: String, message: String) {
        try {
            val mqttMessage = MqttMessage(message.toByteArray())
            mqttClient.publish(topic, mqttMessage, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    Log.d("WebSocketClient", "Mesaj başarıyla gönderildi: $topic -> $message")
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    Log.e("WebSocketClient", "Mesaj gönderme hatası: $topic", exception)
                }
            })
        } catch (e: Exception) {
            Log.e("WebSocketClient", "Mesaj gönderme hatası", e)
        }
    }

    fun disconnect() {
        try {
            mqttClient.disconnect(null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    Log.d("WebSocketClient", "WebSocket bağlantısı kapatıldı")
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    Log.e("WebSocketClient", "WebSocket bağlantısı kapatılırken hata", exception)
                }
            })
        } catch (e: Exception) {
            Log.e("WebSocketClient", "WebSocket bağlantısı kapatma hatası", e)
        }
    }
} 