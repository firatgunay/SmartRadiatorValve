package com.firatgunay.smartradiatorvalve.websocket

import android.content.Context
import android.util.Log
import okhttp3.*
import javax.inject.Inject

class WebSocketClient @Inject constructor(
    private val context: Context
) {
    private var webSocket: WebSocket? = null
    private val client = OkHttpClient()
    private var messageCallback: ((String) -> Unit)? = null

    fun connect(url: String) {
        val request = Request.Builder()
            .url(url)
            .build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                super.onOpen(webSocket, response)
                Log.d("WebSocketClient", "Bağlantı açıldı")
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                super.onMessage(webSocket, text)
                Log.d("WebSocketClient", "Mesaj alındı: $text")
                messageCallback?.invoke(text)
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                super.onClosing(webSocket, code, reason)
                Log.d("WebSocketClient", "Bağlantı kapanıyor: $reason")
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                super.onClosed(webSocket, code, reason)
                Log.d("WebSocketClient", "Bağlantı kapandı: $reason")
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                super.onFailure(webSocket, t, response)
                Log.e("WebSocketClient", "Bağlantı hatası", t)
            }
        })
    }

    fun setMessageCallback(callback: (String) -> Unit) {
        messageCallback = callback
    }

    fun sendMessage(message: String) {
        webSocket?.send(message)
    }

    fun disconnect() {
        try {
            webSocket?.close(1000, "Bağlantı kapatılıyor")
            client.dispatcher.executorService.shutdown()
        } catch (e: Exception) {
            Log.e("WebSocketClient", "Bağlantı kapatma hatası", e)
        }
    }
} 