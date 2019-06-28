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
    val nodeClassService = NodeClassService(enableCache, nodeClassDao)
    val fieldService = FieldService(enableCache, fieldDao)
    val nodeService = NodeService(enableCache, nodeDao)
    val fieldValueService = FieldValueService(enableCache, fieldValueDao)
    private val hostSlave = HostSlave(this)

    init {
        hostInfoPublisher.subscribe { hostInfo = it }
        Observable.interval(INTERVAL_RUNNING_STATUS_REPORT, TimeUnit.MILLISECONDS).subscribe { reportRunningStatus() }
    }

    companion object {
        var hostInfo: IHostInfo? = null
    }

    private fun reportRunningStatus() =
            hostInfo?.nodeId?.apply {
                fieldValueService.setFieldValueByFieldKey(this, KEY_FIELD_RUNNING_STATUS,
                        stringfy(RunningStatus(
                                PEER_ID,
                                hostSlave.parentHostPeer
                        )))
            }

}