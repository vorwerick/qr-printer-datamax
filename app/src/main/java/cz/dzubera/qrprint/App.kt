package cz.dzubera.qrprint

import android.app.Application

class App: Application() {

    companion object{
        val printerConnectionManager: PrinterConnectionManager by lazy { PrinterConnectionManager() }
        val timeManager: TimerManager by lazy { TimerManager() }
    }

    override fun onCreate() {
        super.onCreate()

        timeManager.start(1000)
    }
}