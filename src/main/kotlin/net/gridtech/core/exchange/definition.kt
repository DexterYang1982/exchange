package net.gridtech.core.exchange

interface ISlave {
    fun beginToSync(content: String, serviceName: String?)
    fun serviceSyncFinishedFromMaster(content: String, serviceName: String?)
    fun dataUpdate(content: String, serviceName: String?)
    fun dataDelete(content: String, serviceName: String?)
    fun fieldValueAskFor(content: String, serviceName: String?)
}

interface IMaster<T> {
    fun dataShellFromSlave(session: T, content: String, serviceName: String?)
    fun serviceSyncFinishedFromSlave(session: T, content: String, serviceName: String?)
    fun fieldValueUpdate(session: T, content: String, serviceName: String?)
}


data class ExchangeParcel(
        var command: String,
        var content: String,
        var serviceName: String? = null
)

data class DataShell(
        var id: String,
        var updateTime: Long
)