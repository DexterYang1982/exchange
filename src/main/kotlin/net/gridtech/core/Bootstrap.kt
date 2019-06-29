package net.gridtech.core

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

}