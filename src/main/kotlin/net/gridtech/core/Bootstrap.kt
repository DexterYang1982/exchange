package net.gridtech.core

import net.gridtech.core.data.*
import net.gridtech.core.exchange.HostSlave
import net.gridtech.core.util.hostInfoPublisher


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

    val hostSlave = HostSlave()

    init {
        hostInfoPublisher.subscribe { hostInfo = it }
    }
    companion object{
        var hostInfo: IHostInfo? = null
    }

}