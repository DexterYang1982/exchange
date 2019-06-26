package net.gridtech.core.data


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
}

interface INode : IStructureData {
    var nodeClassId: String
    var path: List<String>
    var scopes: List<Scope>
}

interface IFieldValue : IBaseData {
    var nodeId: String
    var fieldId: String
    var value: String
    var session: String
}

data class Scope(
        var branchNodeId: String,
        var branchNodeTags: List<String>
)


enum class ChangedType {
    UPDATE,
    DELETE
}

enum class ChangedDirection {
    UP,
    DOWN,
    BOTH
}
data class DataChangedMessage(
        val dataId: String,
        val serviceName: String,
        val type: ChangedType,
        val direction: ChangedDirection
)
data class HostAddress(
        val ip:String,
        val port:Int
)
data class HostInfo(
        val isRoot:Boolean?,
        val nodeId:String?,
        val secret:String?,
        val parentAddress:HostAddress?
)