package net.gridtech.core.data


data class NodeClassStub(
        override var id: String,
        override var name: String,
        override var alias: String,
        override var description: String,
        override var connectable: Boolean,
        override var tags: List<String>,
        override var updateTime: Long
) : INodeClass

data class FieldStub(
        override var id: String,
        override var name: String,
        override var alias: String,
        override var description: String,
        override var tags: List<String>,
        override var nodeClassId: String,
        override var through: Boolean,
        override var updateTime: Long
) : IField

data class NodeStub(
        override var id: String,
        override var name: String,
        override var alias: String,
        override var description: String,
        override var tags: List<String>,
        override var nodeClassId: String,
        override var path: List<String>,
        override var externalScope: List<String>,
        override var updateTime: Long
) : INode

data class FieldValueStub(
        override var id: String,
        override var nodeId: String,
        override var fieldId: String,
        override var value: String,
        override var updateTime: Long,
        override var session: String
) : IFieldValue