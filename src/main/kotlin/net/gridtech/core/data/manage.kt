package net.gridtech.core.data

interface IManager {
    fun nodeClassAdd(id: String,
                     name: String,
                     alias: String,
                     connectable: Boolean,
                     tags: List<String>,
                     description: Any? = null): INodeClass

    fun nodeClassUpdate(id: String,
                        name: String,
                        alias: String,
                        description: Any? = null): INodeClass


    fun nodeClassDelete(id: String)

    fun fieldAdd(key: String,
                 name: String,
                 alias: String,
                 nodeClassId: String,
                 through: Boolean,
                 tags: List<String>,
                 description: Any? = null): IField

    fun fieldUpdate(id: String,
                    name: String,
                    alias: String,
                    description: Any? = null): IField

    fun fieldDelete(id: String)

    fun nodeAdd(id: String,
                name: String,
                alias: String,
                nodeClassId: String,
                parentId: String,
                externalNodeIdScope: List<String>,
                externalNodeClassTagScope: List<String>,
                tags: List<String>,
                description: Any? = null): INode

    fun nodeUpdate(id: String,
                   name: String,
                   alias: String,
                   description: Any? = null): INode

    fun nodeDelete(id: String)

    fun fieldValueUpdateByFieldKey(fieldKey: String,
                                   nodeId: String,
                                   session: String,
                                   value: Any)

    fun fieldValueUpdate(nodeId: String, fieldId: String,
                         session: String,value: Any)
}