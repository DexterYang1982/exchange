package net.gridtech.core

import fi.iki.elonen.NanoHTTPD
import io.reactivex.Observable
import net.gridtech.core.data.*
import net.gridtech.core.exchange.HostSlave
import net.gridtech.core.util.*
import java.util.concurrent.TimeUnit


class Bootstrap(
        enableCache: Boolean,
        nodeClassDao: INodeClassDao,
        fieldDao: IFieldDao,
        nodeDao: INodeDao,
        fieldValueDao: IFieldValueDao
) {
    val nodeClassService = NodeClassService(enableCache, nodeClassDao, this)
    val fieldService = FieldService(enableCache, fieldDao, this)
    val nodeService = NodeService(enableCache, nodeDao, this)
    val fieldValueService = FieldValueService(enableCache, fieldValueDao, this)
    private val services = HashMap<String, IBaseService<*>>()
    private val hostSlave = HostSlave(this)

    init {
        services[nodeClassService.serviceName] = nodeClassService
        services[fieldService.serviceName] = fieldService
        services[nodeService.serviceName] = nodeService
        services[fieldValueService.serviceName] = fieldValueService

        hostInfoPublisher.subscribe { hostInfo = it }
        Observable.interval(INTERVAL_RUNNING_STATUS_REPORT, TimeUnit.MILLISECONDS).subscribe { reportRunningStatus() }
    }

    companion object {
        var hostInfo: IHostInfo? = null
    }

    fun service(name: String): IBaseService<*> = services[name]!!

    private fun reportRunningStatus() =
            hostInfo?.nodeId?.apply {
                fieldValueService.setFieldValueByFieldKey(this, KEY_FIELD_RUNNING_STATUS,
                        stringfy(RunningStatus(
                                INSTANCE_ID,
                                hostSlave.parentHostInstance
                        )))
            }

    fun <T : IBaseData> dataPublisher(serviceName: String): Observable<Triple<ChangedType, String, T?>> =
            Observable.concat(
                    Observable.fromIterable(service(serviceName).getAll())
                            .map { Triple(ChangedType.UPDATE, it.id, cast<T>(it)) },
                    Observable.just(Triple(ChangedType.FINISHED, "", null)),
                    dataChangedPublisher.filter { it.serviceName == serviceName }
                            .map {
                                Triple(it.type,
                                        it.dataId,
                                        if (it.type == ChangedType.UPDATE)
                                            cast<T>(service(serviceName).getById(it.dataId))
                                        else
                                            null)
                            }
            )

    fun startHostInfoChangServer(port: Int) {
        val server = object : NanoHTTPD(port) {
            override fun serve(session: IHTTPSession?): Response {
                return if (session?.uri?.endsWith("/hostInfo") == true) {
                    newFixedLengthResponse(hostInfo?.let { stringfy(it) } ?: "{}").apply {
                        mimeType="application/json"
                    }
                } else if (session?.uri?.endsWith("/updateHostInfo") == true) {
                    val map = HashMap<String, String>()
                    session.parseBody(map)
                    val body = map["postData"]!!
                    try {
                        val hostInfo: HostInfoStub = parse(body)
                        hostInfoPublisher.onNext(hostInfo)
                        newFixedLengthResponse("done")
                    } catch (e: Throwable) {
                        newFixedLengthResponse(e.message)
                    }
                } else {
                    newFixedLengthResponse("request not found")
                }
            }
        }
        server.start()
        System.err.println("Start HostInfo Chang Server at $port use /hostInfo or /updateHostInfo")
    }

    data class HostInfoStub(
            override var nodeId: String,
            override var nodeSecret: String,
            override var parentEntryPoint: String?
    ) : IHostInfo
}