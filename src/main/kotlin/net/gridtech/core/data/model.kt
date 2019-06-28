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
    var externalScope: List<String>
}

interface IFieldValue : IBaseData {
    var nodeId: String
    var fieldId: String
    var value: String
    var session: String
}

enum class ChangedType {
    UPDATE,
    DELETE
}

data class DataChangedMessage(
        val dataId: String,
        val serviceName: String,
        val type: ChangedType,
        val peer: String
)

interface IHostInfo {
    var nodeId: String
    var nodeSecret: String
    var parentAddress: String?
}

data class RunningStatus(
        var nodeId: String,
        var peer: String,
        var connectedToParentNodeId: String?,
        var connectedToParentPeer: String?
)