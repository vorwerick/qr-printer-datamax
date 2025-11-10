package cz.dzubera.qrprint.zebra

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.net.Socket
import kotlin.math.max

data class Dimensions(val size: Int, val offsetX: Int, val offsetY: Int)

object ZebraPrinterUtils {

    var connected = false
    var task: Job? = null

    fun printDataMatrix(
        ip: String, port: Int, data: String, dimensions: Dimensions, times: Int = 1, onError: (String) -> Unit
    ) {

        /*
        ^XA
^PW400               // šířka tisku
^LH0,0               // výchozí levý horní roh

// --- Text nad QR ---
^FO100,50            // pozice (x=100, y=50)
^A0N,40,40           // font: A0N, velikost 40x40
^FDID: 123456^FS     // text k vytištění

// --- QR kód ---
^FO100,100           // pozice (x=100, y=100)
^BQN,2,10            // QR code model 2, velikost 10
^FDLA,https://profilog.com/item/123456^FS

^XZ
         */
        val zpl =
            "^XA^FO${dimensions.offsetX},${dimensions.offsetY}^BXN,${dimensions.size},200^FD$data^FS^XZ" // ZPL pro DataMatrix

        GlobalScope.launch {
            println("CARU FARU")
            try {
                Socket(ip, port).use { socket ->
                    socket.soTimeout = 2000
                    println("HARU GARU")
                    val output = socket.getOutputStream()

                    repeat(times) {
                        output.write(zpl.toByteArray())
                        output.flush()
                    }

                }
            } catch (e: Exception) {
                e.printStackTrace()
                GlobalScope.launch(Dispatchers.Main) {
                    onError(e.localizedMessage ?: "unknown error")
                }
            }
        }
    }

    fun testConnection(
        ip: String, port: Int, onSuccess: () -> Unit, onError: (String) -> Unit
    ) {
        task?.cancel()
        task = GlobalScope.launch {
            while (this.isActive) {
                try {
                    Socket(ip, port).use { socket ->
                        socket.soTimeout = 2000
                        if (socket.isConnected) {
                            GlobalScope.launch(Dispatchers.Main) {
                                connected = true
                                onSuccess()

                            }
                            socket.close()
                        }

                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    GlobalScope.launch(Dispatchers.Main) {
                        connected = false
                        onError(e.localizedMessage ?: "unknown error")

                    }
                }
                delay(1000)
            }

        }
    }

    fun printDataMatrixWithText(
        ip: String,
        port: Int,
        data: String,
        textOverCode: String,
        dimensions: Dimensions,
        times: Int = 1,
        onError: (String) -> Unit
    ) {
        val labelWidth = 380 // šířka etikety
        val fontBase = 50
        val fontSize = max(20, fontBase - (textOverCode.length * 3).toInt())

        val zpl = """
^XA
^LH0,0

^FO0,${35+(textOverCode.length*2)}^FB${labelWidth},1,0,C,0
^A2N,${fontSize},${fontSize} 
^FD${textOverCode}^FS

^FO${dimensions.offsetX},${dimensions.offsetY}
^BXN,${dimensions.size},200
^FD${data}^FS

^XZ
""".trimIndent()
        GlobalScope.launch {
            println("CARU FARU")
            try {
                Socket(ip, port).use { socket ->
                    socket.soTimeout = 2000

                    println("HARU GARU")
                    val output = socket.getOutputStream()

                    repeat(times) {
                        output.write(zpl.toByteArray())
                        output.flush()
                    }

                }
            } catch (e: Exception) {
                e.printStackTrace()
                GlobalScope.launch(Dispatchers.Main) {
                    onError(e.localizedMessage ?: "unknown error")
                }
            }
        }
    }
}