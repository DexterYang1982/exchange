package net.gridtech.core.data

import net.gridtech.core.util.PEER_ID
import net.gridtech.core.util.compose
import net.gridtech.core.util.currentTime
import net.gridtech.core.util.dataChangedPublisher
import java.util.concurrent.ConcurrentHashMap


abstract class IBaseService<T : IBaseData>(enableCache: Boolean, private val dao: IBaseDao<T>) {
    val serviceName: String = javaClass.simpleName
    private val cache: ConcurrentHashMap<String, T>? = if (enableCache) ConcurrentHashMap() else null

    init {
        services[serviceName] = this
    }

    companion object {
        val services = HashMap<String, IBaseService<*>>()
        fun get(name: String): IBaseService<*> {
            return services[name]!!
        }
    }

    open fun getAll(): List<T> = dao.getAll()
    open fun getById(id: String): T? {
        var data = cache?.get(id)
        if (data == null) {
            data = dao.getById(id)
        } else {
            return data
        }
        if (data != null) {
            cache?.put(id, data)
        }
        return data
    }

    open fun save(data: T, peer: String? = null) {
        cache?.put(data.id, data)
        dao.save(data)
        dataChangedPublisher.onNext(DataChangedMessage(
                data.id,
                serviceName,
                ChangedType.UPDATE,
                peer ?: PEER_ID
        ))
    }

    open fun delete(id: String, peer: String? = null) {
        cache?.remove(id)
        dao.delete(id)
        dataChangedPublisher.onNext(DataChangedMessage(
                id,
                serviceName,
                ChangedType.DELETE,
                peer ?: PEER_ID
        ))
    }
}

class NodeClassService(enableCache: Boolean, dao: INodeClassDao) : IBaseService<INodeClass>(enableCache, dao) {
}

class FieldService(enableCache: Boolean, private val fieldDao: IFieldDao) : IBaseService<IField>(enableCache, fieldDao) {
    fun getByNodeClass(nodeClass: INodeClass): List<IField> = fieldDao.getByNodeClassId(nodeClass.id)
    override fun delete(id: String, peer: String?) {
        val fieldValueService = get(FieldValueService::class.simpleName!!) as FieldValueService
        getById(id)?.apply {
            fieldValueService.getByField(this).forEach {
                fieldValueService.delete(it.id)
            }
        }
        super.delete(id,peer)
    }
}

class NodeService(enableCache: Boolean, private val nodeDao: INodeDao) : IBaseService<INode>(enableCache, nodeDao) {
    fun getByNodeClass(nodeClass: INodeClass): List<INode> = nodeDao.getByNodeClassId(nodeClass.id)
    fun getByBranch(branchNode: INode): List<INode> = nodeDao.getByBranchNodeId(branchNode.id)
    override fun delete(id: String, peer: String?) {
        val fieldValueService = get(FieldValueService::class.simpleName!!) as FieldValueService
        getById(id)?.apply {
            fieldValueService.getByNode(this).forEach {
                fieldValueService.delete(it.id)
            }
        }
        super.delete(id,peer)
    }
}

class FieldValueService(enableCache: Boolean, private val fieldValueDao: IFieldValueDao) : IBaseService<IFieldValue>(enableCache, fieldValueDao) {
    fun getByNode(node: INode): List<IFieldValue> = fieldValueDao.getByNodeId(node.id)
    fun getByField(field: IField): List<IFieldValue> = fieldValueDao.getByFieldId(field.id)

    fun getFieldValueByFieldKey(nodeId: String, fieldKey: String): IFieldValue? {
        val nodeService = get(NodeService::class.simpleName!!) as NodeService
        val fieldService = get(FieldService::class.simpleName!!) as FieldService
        return nodeService.getById(nodeId)?.let { node ->
            fieldService.getById(compose(node.nodeClassId, fieldKey))?.let { field ->
                getById(compose(node.id, field.id))
            }
        }
    }

    fun setFieldValueByFieldKey(nodeId: String, fieldKey: String, value: String, session: String? = null) {
        val nodeService = get(NodeService::class.simpleName!!) as NodeService
        val fieldService = get(FieldService::class.simpleName!!) as FieldService
        nodeService.getById(nodeId)?.let { node ->
            fieldService.getById(compose(node.nodeClassId, fieldKey))?.let { field ->
                save(FieldValueStub(
                        id = compose(node.id, field.id),
                        nodeId = node.id,
                        fieldId = field.id,
                        value = value,
                        session = session ?: "",
                        updateTime = currentTime()
                ), PEER_ID)
            }
        }
    }
}