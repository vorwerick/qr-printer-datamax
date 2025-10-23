package cz.dzubera.qrprint.zebra

import android.app.Application

class App: Application() {

    companion object{
        val timeManager: TimerManager by lazy { TimerManager() }
    }

    override fun onCreate() {
        super.onCreate()

        timeManager.start(1000)
    }
}