package net.gridtech.core.data

interface IManager {
    fun nodeClassAdd(id: String,
                     name: String,
                     alias: String,
                     connectable: Boolean,
                     tags: List<String>,
                     description: Any? = null):INodeClass

    fun nodeClassUpdate(id: String,
                        name: String,
                        alias: String,
                        description: Any? = null):INodeClass
}