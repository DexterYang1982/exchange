package net.gridtech.core.model


interface IBaseData {
    var id: String
    var updateTime: Long
}

interface IStructureData : IBaseData {
    var name: String
    var alias: String
    var tags: List<String>
    var description: String
}

interface INodeClass : IStructureData {
    var connectable: Boolean
}


interface IField : IStructureData {
    var nodeClassId: String
    var internal: Boolean
}

interface INode : IStructureData {
    var nodeClassId: String
    var path: List<String>
    var scopes: List<Scope>
}

data class Scope(
        var id: String,
        var nodeClassTags: List<String>,
        var fieldTags: List<String>,
        var nodeTags: List<String>,
        var writable: Boolean
)

interface IFieldValue : IBaseData {
    var nodeId: String
    var fieldId: String
    var value: String
    var session: String
    var recordTime: Long
}

interface IFieldValueHistory {
    var id: String
    var nodeId: String
    var fieldId: String
    var records: List<FieldValueRecord>
}

data class FieldValueRecord(
        var value: String,
        var session: String,
        var updateTime: Long,
        var recordTime: Long
)