package net.gridtech.core.exchange

import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers.io
import net.gridtech.core.Bootstrap
import net.gridtech.core.data.FieldValueService
import net.gridtech.core.util.*
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.util.concurrent.TimeUnit
import kotlin.reflect.KFunction

class HostSlave(private val bootstrap: Bootstrap) : WebSocketListener() {
    var currentConnection: WebSocket? = null
    var parentHostPeer: String? = null

    init {
        hostInfoPublisher
                .subscribe { closeCurrentConnection() }
        Observable.interval(INTERVAL_TRY_CONNECT_TO_PARENT, TimeUnit.MILLISECONDS)
                .filter { currentConnection == null }
                .subscribe { connectToParent() }
        dataChangedPublisher
                .filter {
                    it.serviceName == FieldValueService::class.simpleName!!
                            && currentConnection != null
                            && parentHostPeer != null
                            && it.peer != parentHostPeer
                }
                .subscribeOn(io())
                .subscribe {

                }
    }

    private fun connectToParent() {
        try {
            Bootstrap.hostInfo?.let { hostInfo ->
                hostInfo.parentEntryPoint
                        ?.let { parentEntryPoint ->
                            Request.Builder().url("ws://$parentEntryPoint?nodeId=${hostInfo.nodeId}&nodeSecret=${hostInfo.nodeSecret}&peer=$PEER_ID").build()
                        }
            }?.let { request ->
                val client = okHttpClient.newBuilder().build()
                currentConnection = client.newWebSocket(request, this)
                client
            }?.apply {
                dispatcher().executorService().shutdown()
            }
        } catch (e: Throwable) {
            closeCurrentConnection()
            System.err.println("connect to parent error ${e.message}")
        }
    }

    override fun onOpen(webSocket: WebSocket, response: Response) {
        parentHostPeer = response.header("peer")
        println("Connected to parent peer $parentHostPeer")
    }


    override fun onMessage(webSocket: WebSocket, text: String) {
        try {
            val exchangeParcel: ExchangeParcel = parse(text)
        } catch (e: Throwable) {
            e.printStackTrace()
        }
        super.onMessage(webSocket, text)
    }


    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
        System.err.println("onClosed code $code reason $reason")
        closeCurrentConnection()
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        System.err.println("onFailure ${t.message} ${response?.code()}")
        closeCurrentConnection()
    }

    fun send(function: KFunction<*>, content: Any, serviceName: String? = null) {
        try {
            currentConnection?.send(
                    stringfy(ExchangeParcel(
                            command = function.name,
                            content = objectMapper.writeValueAsString(content),
                            serviceName = serviceName
                    )))
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    private fun closeCurrentConnection() {
        currentConnection?.cancel()
        currentConnection = null
        parentHostPeer = null
    }
}