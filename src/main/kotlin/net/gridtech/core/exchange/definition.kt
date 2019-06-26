package net.gridtech.core.exchange

interface ISlave{

}

interface IMaster{

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