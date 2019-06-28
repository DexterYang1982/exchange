package net.gridtech.core.data

import net.gridtech.core.util.parse


data class NodeClassStub(
        override var id: String,
        override var name: String,
        override var alias: String,
        override var description: String,
        override var connectable: Boolean,
        override var tags: List<String>,
        override var updateTime: Long
) : INodeClass {
    companion object {
        fun parseFromString(content: String): NodeClassStub = parse(content)
    }
}

data class FieldStub(
        override var id: String,
        override var name: String,
        override var alias: String,
        override var description: String,
        override var tags: List<String>,
        override var nodeClassId: String,
        override var through: Boolean,
        override var updateTime: Long
) : IField{
    companion object {
        fun parseFromString(content: String): FieldStub = parse(content)
    }
}

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
) : INode{
    companion object {
        fun parseFromString(content: String): NodeStub = parse(content)
    }
}

data class FieldValueStub(
        override var id: String,
        override var nodeId: String,
        override var fieldId: String,
        override var value: String,
        override var updateTime: Long,
        override var session: String
) : IFieldValue{
    companion object {
        fun parseFromString(content: String): FieldValueStub = parse(content)
    }
}