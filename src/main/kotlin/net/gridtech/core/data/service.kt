package net.gridtech.core.data

import net.gridtech.core.Bootstrap
import net.gridtech.core.util.INSTANCE_ID
import net.gridtech.core.util.compose
import net.gridtech.core.util.currentTime
import net.gridtech.core.util.dataChangedPublisher
import java.util.concurrent.ConcurrentHashMap


abstract class IBaseService<T : IBaseData>(enableCache: Boolean, private val dao: IBaseDao<T>) {
    val serviceName = javaClass.simpleName!!
    private val cache: ConcurrentHashMap<String, T>? = if (enableCache) ConcurrentHashMap() else null
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

    open fun save(data: T, instance: String? = null) {
        cache?.put(data.id, data)
        dao.save(data)
        dataChangedPublisher.onNext(DataChangedMessage(
                data.id,
                serviceName,
                ChangedType.UPDATE,
                instance ?: INSTANCE_ID
        ))
    }

    fun saveFromRemote(content: String, instance: String) = save(parseData(content), instance)
    abstract fun parseData(content: String): T

    open fun delete(id: String, instance: String? = null) {
        cache?.remove(id)
        dao.delete(id)
        dataChangedPublisher.onNext(DataChangedMessage(
                id,
                serviceName,
                ChangedType.DELETE,
                instance ?: INSTANCE_ID
        ))
    }
}


class NodeClassService(enableCache: Boolean, private val nodeClassDao: INodeClassDao, private val bootstrap: Bootstrap) : IBaseService<INodeClass>(enableCache, nodeClassDao) {
    override fun parseData(content: String): INodeClass = NodeClassStub.parseFromString(content)
    fun getByTags(tags: List<String>): List<INodeClass> = nodeClassDao.getByTags(tags)
}

class FieldService(enableCache: Boolean, private val fieldDao: IFieldDao, private val bootstrap: Bootstrap) : IBaseService<IField>(enableCache, fieldDao) {

    override fun parseData(content: String): IField = FieldStub.parseFromString(content)
    fun getByNodeClass(nodeClass: INodeClass): List<IField> = fieldDao.getByNodeClassId(nodeClass.id)
    override fun delete(id: String, instance: String?) {
        getById(id)?.apply {
            bootstrap.fieldValueService.getByField(this).forEach {
                bootstrap.fieldValueService.delete(it.id)
            }
        }
        super.delete(id, instance)
    }
}

class NodeService(enableCache: Boolean, private val nodeDao: INodeDao, private val bootstrap: Bootstrap) : IBaseService<INode>(enableCache, nodeDao) {

    override fun parseData(content: String): INode = NodeStub.parseFromString(content)
    fun getByNodeClass(nodeClass: INodeClass): List<INode> = nodeDao.getByNodeClassId(nodeClass.id)
    fun getByBranch(branchNode: INode): List<INode> = nodeDao.getByBranchNodeId(branchNode.id)
    fun getNodeScope(node: INode): Set<INode> {
        val scope = getNodeBranchScope(node)
        node.externalNodeIdScope.forEach { branchNodeId ->
            scope.addAll(getById(branchNodeId)?.let { branchNode ->
                getNodeBranchScope(branchNode)
            } ?: emptySet())
        }
        return scope
    }

    private fun getNodeBranchScope(branchNode: INode): MutableSet<INode> =
            getByBranch(branchNode).toMutableSet().apply {
                branchNode.path.forEach { parentId ->
                    getById(parentId)?.let { parentNode ->
                        add(parentNode)
                    }
                }
                add(branchNode)
            }

    override fun delete(id: String, instance: String?) {
        getById(id)?.apply {
            bootstrap.fieldValueService.getByNode(this).forEach {
                bootstrap.fieldValueService.delete(it.id)
            }
        }
        super.delete(id, instance)
    }
}

class FieldValueService(enableCache: Boolean, private val fieldValueDao: IFieldValueDao, private val bootstrap: Bootstrap) : IBaseService<IFieldValue>(enableCache, fieldValueDao) {

    override fun parseData(content: String): IFieldValue = FieldValueStub.parseFromString(content)
    fun getByNode(node: INode): List<IFieldValue> = fieldValueDao.getByNodeId(node.id)
    fun getByField(field: IField): List<IFieldValue> = fieldValueDao.getByFieldId(field.id)
    fun getSince(id: String, since: Long) = fieldValueDao.getSince(id, since)
    fun valueSyncToChild(childNodeId: String, valueNode: INode, valueField: IField): Boolean =
            valueNode.id == childNodeId || valueNode.path.contains(childNodeId) || valueField.through

    fun getFieldValueByFieldKey(nodeId: String, fieldKey: String): IFieldValue? {
        return bootstrap.nodeService.getById(nodeId)?.let { node ->
            bootstrap.fieldService.getById(compose(node.nodeClassId, fieldKey))?.let { field ->
                getById(compose(node.id, field.id))
            }
        }
    }

    fun setFieldValue(nodeId: String, fieldId: String, value: String, session: String? = null) {
        bootstrap.nodeService.getById(nodeId)?.let { node ->
            bootstrap.fieldService.getById(fieldId)?.let { field ->
                save(FieldValueStub(
                        id = compose(node.id, field.id),
                        nodeId = node.id,
                        fieldId = field.id,
                        value = value,
                        session = session ?: "",
                        updateTime = currentTime()
                ), INSTANCE_ID)
            }
        }
    }

    fun setFieldValueByFieldKey(nodeId: String, fieldKey: String, value: String, session: String? = null) {
        bootstrap.nodeService.getById(nodeId)?.let { node ->
            bootstrap.fieldService.getById(compose(node.nodeClassId, fieldKey))?.let { field ->
                save(FieldValueStub(
                        id = compose(node.id, field.id),
                        nodeId = node.id,
                        fieldId = field.id,
                        value = value,
                        session = session ?: "",
                        updateTime = currentTime()
                ), INSTANCE_ID)
            }
        }
    }
}