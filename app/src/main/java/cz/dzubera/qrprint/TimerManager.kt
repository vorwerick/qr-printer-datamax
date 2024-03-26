package cz.dzubera.qrprint

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.concurrent.Executors

class TimerManager {

    val executor = Executors.newSingleThreadExecutor()

    val callbacks = mutableListOf<() -> Unit>()

    var countdown = 0

    fun resetCountDown() {
        countdown = 0
    }

    fun start(repeat: Long) {
        executor.execute {
            while (true) {
                Thread.sleep(repeat)
                callbacks.forEach { it.invoke() }
                countdown += 1
                GlobalScope.launch(Dispatchers.Main) {
                    StaticStorage.pressLimit -= 1
                    if (StaticStorage.pressLimit <= 0) {
                        StaticStorage.pressLimit = 0
                    }
                }
            }
        }
    }

    fun registerCallback(callback: () -> Unit) {
        callbacks.add(callback)
    }

    fun unregister(callback: () -> Unit) {
        callbacks.remove(callback)
    }
}