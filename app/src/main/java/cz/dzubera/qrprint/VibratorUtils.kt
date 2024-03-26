package cz.dzubera.qrprint

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.core.content.ContextCompat.getSystemService


object VibratorUtils {

    fun vibrate(context: Context){
        val v = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator?

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v!!.vibrate(VibrationEffect.createOneShot(300, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            //deprecated in API 26
            v!!.vibrate(200)
        }
    }



}