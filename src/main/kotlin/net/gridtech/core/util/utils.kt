package net.gridtech.core.util

import okhttp3.OkHttpClient
import java.net.Inet4Address
import java.net.NetworkInterface
import java.util.ArrayList


private var lastTime: Long = 0
@Synchronized
private fun nextSequence(): Long {
    var now = System.currentTimeMillis()
    if (now <= lastTime) {
        now = lastTime + 1
    }
    lastTime = now
    return now
}

fun currentTime(): Long = nextSequence()
fun generateId(): String = "${nextSequence()}"


fun compose(vararg id: String): String = id.joinToString(COMPOSE_ID_SEPARATOR)


fun getNetworkAddress(): List<String> {
    val result = ArrayList<String>()
    try {
        val netInterfaces = NetworkInterface.getNetworkInterfaces()
        while (netInterfaces.hasMoreElements()) {
            val addresses = netInterfaces.nextElement().inetAddresses
            while (addresses.hasMoreElements()) {
                val ip = addresses.nextElement()
                if (!ip.isLoopbackAddress && ip is Inet4Address) {
                    result.add(ip.hostAddress)
                }
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return result
}


val okHttpClient = OkHttpClient()