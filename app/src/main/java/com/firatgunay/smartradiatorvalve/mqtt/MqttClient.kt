package com.firatgunay.smartradiatorvalve.mqtt

import android.content.Context
import android.util.Log
import org.eclipse.paho.client.mqttv3.*
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MqttClient @Inject constructor(
    private val context: Context
) {
    private var mqttClient: MqttAsyncClient? = null
    private var callback: ((String, String) -> Unit)? = null
    
    private val serverUri = "tcp://broker.hivemq.com:1883"
    private val clientId = "AndroidApp_" + System.currentTimeMillis()
    
    private val topics = arrayOf(
        "valve/temperature",    // DHT11'den gelen sıcaklık verisi
        "valve/humidity",       // DHT11'den gelen nem verisi
        "valve/status",         // Valf durumu
        "valve/lcd_display"     // LCD ekran durumu
    )

    fun connect() {
        try {
            mqttClient = MqttAsyncClient(serverUri, clientId, MemoryPersistence())
            
            val connectOptions = MqttConnectOptions().apply {
                isCleanSession = true
                connectionTimeout = 30
                keepAliveInterval = 60
                isAutomaticReconnect = true
            }

            mqttClient?.setCallback(object : MqttCallback {
                override fun connectionLost(cause: Throwable?) {
                    Log.e("MQTT", "Bağlantı koptu", cause)
                }

                override fun messageArrived(topic: String?, message: MqttMessage?) {
                    message?.let {
                        val messageStr = String(it.payload)
                        Log.d("MQTT", "Topic: $topic, Message: $messageStr")
                        callback?.invoke(topic ?: "", messageStr)
                    }
                }

                override fun deliveryComplete(token: IMqttDeliveryToken?) {
                    Log.d("MQTT", "Mesaj iletildi")
                }
            })

            mqttClient?.connect(connectOptions, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    Log.d("MQTT", "Bağlantı başarılı")
                    subscribeToTopics()
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    Log.e("MQTT", "Bağlantı hatası", exception)
                }
            })
        } catch (e: Exception) {
            Log.e("MQTT", "MQTT client oluşturma hatası", e)
        }
    }

    private fun subscribeToTopics() {
        topics.forEach { topic ->
            mqttClient?.subscribe(topic, 0, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    Log.d("MQTT", "Topic'e abone olundu: $topic")
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    Log.e("MQTT", "Topic abonelik hatası: $topic", exception)
                }
            })
        }
    }

    fun publishMessage(topic: String, message: String) {
        try {
            val mqttMessage = MqttMessage(message.toByteArray())
            mqttClient?.publish(topic, mqttMessage, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    Log.d("MQTT", "Mesaj gönderildi - Topic: $topic, Message: $message")
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    Log.e("MQTT", "Mesaj gönderme hatası - Topic: $topic", exception)
                }
            })
        } catch (e: Exception) {
            Log.e("MQTT", "Mesaj yayınlama hatası", e)
        }
    }

    fun setCallback(newCallback: (topic: String, message: String) -> Unit) {
        callback = newCallback
    }

    fun disconnect() {
        try {
            mqttClient?.disconnect()
            mqttClient = null
        } catch (e: Exception) {
            Log.e("MQTT", "Bağlantı kesme hatası", e)
        }
    }

    // LCD ekrana mesaj gönderme
    fun updateLcdDisplay(line1: String, line2: String = "") {
        val displayData = "$line1|$line2"
        publishMessage("valve/lcd_display", displayData)
    }

    // Hedef sıcaklık ayarlama
    fun setTargetTemperature(temperature: Float) {
        publishMessage("valve/target_temperature", temperature.toString())
    }

    // Valf durumunu kontrol etme
    fun setValveStatus(isOpen: Boolean) {
        publishMessage("valve/control", if (isOpen) "1" else "0")
    }
} 
