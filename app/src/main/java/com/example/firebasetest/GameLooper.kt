package com.example.firebasetest

class GameLooper(var v: BallView) : Runnable {
    @Volatile
    private var running = true
    private val frameRate = 60
    private val timeout = (1.0 / frameRate * 100).toLong()
    fun terminate() {
        running = false
    }

    override fun run() {
        while (running) {
            try {
                Thread.sleep(timeout)
            } catch (e: InterruptedException) {
                e.printStackTrace()
                running = false
            }
            v.reDraw()
        }
    }
}