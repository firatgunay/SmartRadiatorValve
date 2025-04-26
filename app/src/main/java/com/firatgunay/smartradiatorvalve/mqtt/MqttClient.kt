package com.firatgunay.smartradiatorvalve.mqtt

import android.content.Context
import android.util.Log
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.*
import java.util.UUID
import javax.inject.Inject

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

    fun connect() {
        val options = MqttConnectOptions().apply {
            isAutomaticReconnect = true
            isCleanSession = true
        }

        try {
            mqttClient.connect(options, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    Log.d("MqttClient", "Bağlantı başarılı")
                    connectionCallback?.invoke(true)
                    subscribeToTopics()
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    Log.e("MqttClient", "Bağlantı hatası", exception)
                    connectionCallback?.invoke(false)
                }
            })

            mqttClient.setCallback(object : MqttCallback {
                override fun connectionLost(cause: Throwable?) {
                    Log.e("MqttClient", "Bağlantı koptu", cause)
                    connectionCallback?.invoke(false)
                }

                override fun messageArrived(topic: String?, message: MqttMessage?) {
                    message?.let {
                        val messageStr = String(it.payload)
                        Log.d("MqttClient", "Mesaj alındı: $topic -> $messageStr")
                        messageCallback?.invoke(topic ?: "", messageStr)
                    }
                }

                override fun deliveryComplete(token: IMqttDeliveryToken?) {
                    Log.d("MqttClient", "Mesaj iletildi")
                }
            })
        } catch (e: Exception) {
            Log.e("MqttClient", "Bağlantı hatası", e)
            connectionCallback?.invoke(false)
        }
    }

    fun setCallback(callback: (String, String) -> Unit) {
        messageCallback = callback
    }

    fun setConnectionCallback(callback: (Boolean) -> Unit) {
        connectionCallback = callback
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
                        Log.d("MqttClient", "Topic subscription başarılı: $topic")
                    }

                    override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                        Log.e("MqttClient", "Topic subscription hatası: $topic", exception)
                    }
                })
            }
        } catch (e: Exception) {
            Log.e("MqttClient", "Topic subscription hatası", e)
        }
    }

    fun publishMessage(topic: String, message: String) {
        try {
            val mqttMessage = MqttMessage(message.toByteArray())
            mqttClient.publish(topic, mqttMessage, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    Log.d("MqttClient", "Mesaj başarıyla gönderildi: $topic -> $message")
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    Log.e("MqttClient", "Mesaj gönderme hatası: $topic", exception)
                }
            })
        } catch (e: Exception) {
            Log.e("MqttClient", "Mesaj gönderme hatası", e)
        }
    }

    fun disconnect() {
        try {
            mqttClient.disconnect()
            connectionCallback?.invoke(false)
        } catch (e: Exception) {
            Log.e("MqttClient", "Bağlantı kesme hatası", e)
        }
    }
} 