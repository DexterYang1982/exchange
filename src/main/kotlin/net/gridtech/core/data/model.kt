package net.gridtech.core.data

import net.gridtech.core.util.compose


interface IBaseData {
    var id: String
    var updateTime: Long
}

interface IStructureData : IBaseData {
    var name: String
    var alias: String
    var description: String
    var tags: List<String>
}

interface INodeClass : IStructureData {
    var connectable: Boolean
}


interface IField : IStructureData {
    var nodeClassId: String
    var through: Boolean

    fun matchKey(key: String) = this.id == compose(nodeClassId, key)
}

interface INode : IStructureData {
    var nodeClassId: String
    var path: List<String>
    var externalNodeIdScope: List<String>
    var externalNodeClassTagScope: List<String>
}

interface IFieldValue : IBaseData {
    var nodeId: String
    var fieldId: String
    var value: String
    var session: String
}

enum class ChangedType {
    UPDATE,
    DELETE,
    FINISHED
}

data class DataChangedMessage(
        val dataId: String,
        val serviceName: String,
        val type: ChangedType,
        val instance: String
)

interface IHostInfo {
    var nodeId: String
    var nodeSecret: String
    var parentEntryPoint: String?
}

data class RunningStatus(
        var instance: String,
        var connectedToParentInstance: String?
)

data class ChildScope(
        val scope: Map<String, MutableSet<String>>,
        val sync: Map<String, MutableList<String>>
)