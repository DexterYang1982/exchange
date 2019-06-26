package net.gridtech.core

import net.gridtech.core.data.*
import net.gridtech.core.exchange.HostSlave
import net.gridtech.core.util.ID_NODE_ROOT
import net.gridtech.core.util.hostInfoPublisher

var hostInfo: HostInfo? = null

class Bootstrap(
        initHostInfo: HostInfo?,
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
        hostInfoPublisher.subscribe { initHost(it) }
        initHostInfo?.apply { hostInfoPublisher.onNext(this) }
    }

    private fun initHost(hi: HostInfo) {
        hostInfo = hi
        if (hostInfo?.isRoot == true && nodeService.getById(ID_NODE_ROOT) == null) {
            TODO("init blank root host")
        } else if (hostInfo?.isRoot != true) {
            TODO("connect to parent")
        }
    }
}