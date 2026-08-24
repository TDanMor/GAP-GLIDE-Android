package com.nbsas.gapglide

import android.content.Context
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class MultiplayerManager(private val context: Context) {
    private val connectionsClient = Nearby.getConnectionsClient(context)
    private val serviceId = "com.nbsas.gapglide.MULTIPLATER"

    private val _connectedEndpoints = MutableStateFlow<Map<String, String>>(emptyMap()) // ID -> Name
    val connectedEndpoints = _connectedEndpoints.asStateFlow()

    private val _messages = MutableStateFlow<Pair<String, String>?>(null) // ID -> Message
    val messages = _messages.asStateFlow()

    private var isHosting = false
    private var isJoining = false

    private val connectionLifecycleCallback = object : ConnectionLifecycleCallback() {
        override fun onConnectionInitiated(endpointId: String, connectionInfo: ConnectionInfo) {
            connectionsClient.acceptConnection(endpointId, payloadCallback)
        }

        override fun onConnectionResult(endpointId: String, result: ConnectionResolution) {
            if (result.status.isSuccess) {
                _connectedEndpoints.value += (endpointId to "Player")
            }
        }

        override fun onDisconnected(endpointId: String) {
            _connectedEndpoints.value -= endpointId
        }
    }

    private val payloadCallback = object : PayloadCallback() {
        override fun onPayloadReceived(endpointId: String, payload: Payload) {
            payload.asBytes()?.let {
                _messages.value = endpointId to String(it)
            }
        }

        override fun onPayloadTransferUpdate(endpointId: String, update: PayloadTransferUpdate) {}
    }

    fun startHosting(playerName: String) {
        if (isHosting || isJoining) return
        isHosting = true
        val options = AdvertisingOptions.Builder().setStrategy(Strategy.P2P_STAR).build()
        connectionsClient.startAdvertising(playerName, serviceId, connectionLifecycleCallback, options)
    }

    fun startJoining(playerName: String) {
        if (isHosting || isJoining) return
        isJoining = true
        val options = DiscoveryOptions.Builder().setStrategy(Strategy.P2P_STAR).build()
        connectionsClient.startDiscovery(serviceId, object : EndpointDiscoveryCallback() {
            override fun onEndpointFound(endpointId: String, info: DiscoveredEndpointInfo) {
                connectionsClient.requestConnection(playerName, endpointId, connectionLifecycleCallback)
            }
            override fun onEndpointLost(endpointId: String) {}
        }, options)
    }

    fun stopAll() {
        connectionsClient.stopAdvertising()
        connectionsClient.stopDiscovery()
        connectionsClient.stopAllEndpoints()
        _connectedEndpoints.value = emptyMap()
        isHosting = false
        isJoining = false
    }

    fun broadcast(message: String) {
        val payload = Payload.fromBytes(message.toByteArray())
        connectionsClient.sendPayload(_connectedEndpoints.value.keys.toList(), payload)
    }

    fun sendTo(endpointId: String, message: String) {
        val payload = Payload.fromBytes(message.toByteArray())
        connectionsClient.sendPayload(endpointId, payload)
    }
}
