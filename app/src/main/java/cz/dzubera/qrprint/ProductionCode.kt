package cz.dzubera.qrprint

import android.annotation.SuppressLint
import java.text.SimpleDateFormat
import java.util.*

class ProductionCode(
    private val productNumber: String,
    private val productionCommandNumber: String,
    private val timestamp: Long
) {

    fun getProductPartNumber(): Int? {
        var result = productNumber.split("-")
        var lastnChars = result[0]
        if (lastnChars.length > 2) {
            lastnChars = lastnChars.substring(lastnChars.length - 2, lastnChars.length)
        }
        return lastnChars.toIntOrNull()
    }

    @SuppressLint("SimpleDateFormat")
    override fun toString(): String {
        val date = SimpleDateFormat("yyyyMMdd").format(Date(timestamp)).toString()
        val time = SimpleDateFormat("HHmm").format(Date(timestamp)).toString()
        return "$productNumber.$date.$time.VP-$productionCommandNumber"
    }
}