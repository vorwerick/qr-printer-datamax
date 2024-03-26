package cz.dzubera.qrprint

import android.util.Log
import honeywell.connection.Connection_TCP
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.IOException
import java.net.InetAddress
import java.net.SocketException


class PrinterConnectionManager {

    var connection: Connection_TCP? = null
    var connectionStatus: Status = Status.DISCONNECTED

    val callbacks = mutableListOf<((Status) -> Unit)>()

    @Throws(Exception::class)
    fun connect(address: String, port: Int): Boolean {
        connection?.close()
        connection =
            Connection_TCP.createClient(address, port, false)
        if (!connection!!.isOpen) {
            connection!!.open()
        }
        setStatus(Status.CONNECTED)
        GlobalScope.launch {
            val ip = InetAddress.getByName(address)

            while (true) {
                try {
                    delay(1000)
                    val x = connection?.read("", 1000)
                    Log.d("XX", "JUJU")
                    if (!ip.isReachable(2000)) {
                        break
                    }


                } catch (e: IOException) {
                    e.printStackTrace()
                    break;
                } catch (e: SocketException) {
                    e.printStackTrace()
                    break;
                } catch (e: UnsupportedOperationException) {
                    e.printStackTrace()
                    break;
                }
            }
            close()

        }
        return true
    }

    private fun setStatus(status: Status) {
        connectionStatus = status
        GlobalScope.launch(Dispatchers.Main) {
            callbacks.forEach { it.invoke(connectionStatus) }
        }
    }

    fun write(data: ByteArray): Boolean {
        try {
            connection?.write(data)
            return true
        } catch (e: java.lang.UnsupportedOperationException) {
            setStatus(Status.DISCONNECTED)
            e.printStackTrace()
            connection?.close()
            connection = null
            return false
        } catch (e: SocketException) {
            e.printStackTrace()
            setStatus(Status.DISCONNECTED)
            connection?.close()
            connection = null
            return false
        }
    }

    fun close(): Boolean {
        connection?.close()
        connection = null
        setStatus(Status.DISCONNECTED)
        return true
    }

    enum class Status {
        CONNECTED, DISCONNECTED
    }
}