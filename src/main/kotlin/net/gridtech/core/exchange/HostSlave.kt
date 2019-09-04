package net.gridtech.core.exchange

import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers.io
import net.gridtech.core.Bootstrap
import net.gridtech.core.data.ChangedType
import net.gridtech.core.data.IBaseService
import net.gridtech.core.util.*
import okhttp3.*
import java.util.concurrent.TimeUnit
import kotlin.reflect.KFunction
import kotlin.reflect.full.declaredMemberFunctions

class HostSlave(private val bootstrap: Bootstrap) : WebSocketListener() {
    private val commandMap: Map<String, KFunction<*>> = ISlave::class.declaredMemberFunctions.associateBy { it.name }
    var currentConnection: WebSocket? = null
    var parentHostInstance: String? = null

    init {
        hostInfoPublisher
                .subscribe { 
                    closeCurrentConnection() 
                }
        Observable.interval(INTERVAL_TRY_CONNECT_TO_PARENT, TimeUnit.MILLISECONDS)
                .filter { currentConnection == null }
                .subscribe { connectToParent() }
        dataChangedPublisher
                .subscribeOn(io())
                .filter { message ->
                    message.serviceName == bootstrap.fieldValueService.serviceName
                            && message.type == ChangedType.UPDATE
                            && message.instance != parentHostInstance
                            && currentConnection != null
                            && parentHostInstance != null
                }
                .subscribe { message ->
                    bootstrap.fieldValueService.getById(message.dataId)?.apply {
                        send(IMaster<*>::fieldValueUpdate, this)
                    }
                }
    }

    private fun connectToParent() {
        try {
            Bootstrap.hostInfo
                    ?.let { hostInfo ->
                        hostInfo.parentEntryPoint
                                ?.let { parentEntryPoint ->
                                    val url = "$parentEntryPoint?nodeId=${hostInfo.nodeId}&nodeSecret=${hostInfo.nodeSecret}&instance=$INSTANCE_ID"
                                    Request.Builder().url(url).build()
                                }
                    }?.let { request ->
                        val client = OkHttpClient().newBuilder().build()
                        currentConnection = client.newWebSocket(request, this)
                        client
                    }?.apply {
                        dispatcher.executorService.shutdown()
                    }
        } catch (e: Throwable) {
            closeCurrentConnection()
            System.err.println("connect to parent error ${e.message}")
        }
    }

    override fun onOpen(webSocket: WebSocket, response: Response) {
        parentHostInstance = response.header("instance")
        println("Connected to parent instance $parentHostInstance")
    }


    override fun onMessage(webSocket: WebSocket, text: String) {
        try {
            val exchangeParcel: ExchangeParcel = parse(text)
            println("slave->$exchangeParcel")
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
        System.err.println("onFailure ${t.message} ${response?.code}")
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
        parentHostInstance = null
    }


    private val handler: ISlave = object : ISlave {
        val structureDataServiceToSync = ArrayList<IBaseService<*>>()
        override fun beginToSync(content: String, serviceName: String?) {
            structureDataServiceToSync.clear()
            structureDataServiceToSync.add(bootstrap.nodeClassService)
            structureDataServiceToSync.add(bootstrap.fieldService)
            structureDataServiceToSync.add(bootstrap.nodeService)
            structureDataServiceToSync.add(bootstrap.fieldValueService)
            serviceSyncFinishedFromMaster("", null)
        }

        override fun serviceSyncFinishedFromMaster(content: String, serviceName: String?) {
            if (structureDataServiceToSync.isEmpty()) {
                null
            } else {
                structureDataServiceToSync.removeAt(0)
            }?.apply {
                this.getAll().forEach {
                    send(IMaster<*>::dataShellFromSlave, DataShell(it.id, it.updateTime), this.serviceName)
                }
                send(IMaster<*>::serviceSyncFinishedFromSlave, "", this.serviceName)
            }
        }

        override fun dataUpdate(content: String, serviceName: String?) {
            bootstrap.service(serviceName!!).saveFromRemote(content, parentHostInstance!!)
        }

        override fun dataDelete(content: String, serviceName: String?) {
            val id: String = parse(content)
            bootstrap.service(serviceName!!).delete(id, parentHostInstance!!)
        }

        override fun fieldValueAskFor(content: String, serviceName: String?) {
            val dataShell: DataShell = parse(content)
            bootstrap.fieldValueService.getSince(dataShell.id, dataShell.updateTime).sortedBy { it.updateTime }.forEach {
                send(IMaster<*>::fieldValueUpdate, it)
            }
        }
    }
}