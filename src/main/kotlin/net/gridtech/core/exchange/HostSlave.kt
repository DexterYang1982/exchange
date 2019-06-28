package net.gridtech.core.exchange

import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers.io
import net.gridtech.core.Bootstrap
import net.gridtech.core.data.*
import net.gridtech.core.util.*
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.util.concurrent.TimeUnit
import kotlin.reflect.KFunction
import kotlin.reflect.full.declaredMemberFunctions

class HostSlave(private val bootstrap: Bootstrap) : WebSocketListener() {
    private val commandMap: Map<String, KFunction<*>> = ISlave::class.declaredMemberFunctions.associateBy { it.name }
    var currentConnection: WebSocket? = null
    var parentHostPeer: String? = null

    init {
        hostInfoPublisher
                .subscribe { closeCurrentConnection() }
        Observable.interval(INTERVAL_TRY_CONNECT_TO_PARENT, TimeUnit.MILLISECONDS)
                .filter { currentConnection == null }
                .subscribe { connectToParent() }
        dataChangedPublisher
                .filter {message->
                    message.serviceName == FieldValueService::class.simpleName!!
                            && currentConnection != null
                            && parentHostPeer != null
                            && message.peer != parentHostPeer
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
            commandMap[exchangeParcel.command]?.call(handler, exchangeParcel.content, exchangeParcel.serviceName)
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
                            content = stringfy(content),
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


    private val handler: ISlave = object : ISlave {
        val structureDataServiceToSync = ArrayList<IBaseService<*>>()
        override fun beginToSync(content: String, serviceName: String?) {
            structureDataServiceToSync.clear()
            structureDataServiceToSync.add(IBaseService.service(NodeClassService::class.simpleName!!))
            structureDataServiceToSync.add(IBaseService.service(FieldService::class.simpleName!!))
            structureDataServiceToSync.add(IBaseService.service(NodeService::class.simpleName!!))
            structureDataServiceToSync.add(IBaseService.service(FieldValueService::class.simpleName!!))
            serviceSyncFinishedFromMaster("", null)
        }

        override fun serviceSyncFinishedFromMaster(content: String, serviceName: String?) {
            if (structureDataServiceToSync.isEmpty()) {
                null
            } else {
                structureDataServiceToSync.removeAt(0)
            }?.let { service ->
                service.getAll().forEach {
                    send(IMaster<*>::dataShellFromSlave, DataShell(it.id, it.updateTime), service.serviceName)
                }
                send(IMaster<*>::serviceSyncFinishedFromSlave, "", service.serviceName)
            }
        }

        override fun dataUpdate(content: String, serviceName: String?) {
            serviceName?.apply {
                IBaseService.service(this).saveFromRemote(content, parentHostPeer!!)
            }
        }

        override fun dataDelete(content: String, serviceName: String?) {
            val id: String = parse(content)
            serviceName?.apply {
                IBaseService.service(this).delete(id, parentHostPeer!!)
            }
        }

        override fun fieldValueAskFor(content: String, serviceName: String?) {
            val dataShell: DataShell = parse(content)
            bootstrap.fieldValueService.getSince(dataShell.id, dataShell.updateTime).sortedBy { it.updateTime }.forEach {
                send(IMaster<*>::fieldValueUpdate, it)
            }
        }
    }
}