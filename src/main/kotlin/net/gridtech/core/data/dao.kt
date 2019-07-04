package net.gridtech.core.data


interface IBaseDao<T : IBaseData> {
    fun getAll(): List<T>
    fun getById(id: String): T?
    fun save(d: T)
    fun delete(id: String)
}

interface INodeClassDao : IBaseDao<INodeClass>{
    fun getByTags(tags:List<String>): List<INodeClass>
}

interface IFieldDao : IBaseDao<IField> {
    fun getByNodeClassId(nodeClassId: String): List<IField>
}

interface INodeDao : IBaseDao<INode> {
    fun getByNodeClassId(nodeClassId: String): List<INode>
    fun getByBranchNodeId(branchNodeId: String): List<INode>
}

interface IFieldValueDao : IBaseDao<IFieldValue> {
    fun getByNodeId(nodeId: String): List<IFieldValue>
    fun getByFieldId(fieldId: String): List<IFieldValue>
    fun getSince(id: String, since: Long): List<IFieldValue>
}