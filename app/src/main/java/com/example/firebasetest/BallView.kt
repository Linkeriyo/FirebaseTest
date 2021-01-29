package com.example.firebasetest

import android.content.Context
import android.graphics.*
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.media.MediaPlayer
import android.view.View
import java.io.IOException

class BallView(context: Context?, var res: Point, var email: String?) : View(context), SensorEventListener {
    val deadzone = 0.1
    var ballDiameter: Int = (res.x / 3.6).toInt()
    var ballRadius: Int
    val slowing = 0.5
    var ballBitmap: Bitmap
    var ballPos: Point
    var boundsStart: Point
    var boundsEnd: Point
    var ballSpeedX = 0.0
    var ballSpeedY = 0.0
    var senValX = 0.0
    var senValY = 0.0
    var runnable: GameLooper
    var gapStart: Int
    var gapEnd: Int
    var paintBlack: Paint
    var paintWhite: Paint
    var isBallGon = true
    var mediaPlayer: MediaPlayer
    var mediaPlayerPaused = false

    private fun getResizedBitmap(bm: Bitmap, newWidth: Int, newHeight: Int): Bitmap {
        val width = bm.width
        val height = bm.height
        val scaleWidth = newWidth.toFloat() / width
        val scaleHeight = newHeight.toFloat() / height
        // Create a matrix for the manipulation
        val matrix = Matrix()
        // Resize the bitmap
        matrix.postScale(scaleWidth, scaleHeight)

        // Recreate the new bitmap
        val resizedBitmap = Bitmap.createBitmap(
            bm, 0, 0, width, height, matrix, false
        )
        bm.recycle()
        return resizedBitmap
    }

    override fun onDraw(canvas: Canvas) {
        ballSpeedX -= senValX
        ballSpeedY += senValY

        // Sets ball in its position based on its speed.
        ballPos.x = (ballPos.x + ballSpeedX).toInt()
        ballPos.y = (ballPos.y + ballSpeedY).toInt()

        // Check if the ball is out of the screen.
        checkIfBallsGone()

        // Checks if the ball is colliding with a border.
        checkBorderColisions()

        // Slows the ball a bit each frame.
        slowBall()
        canvas.drawColor(Color.WHITE)
        canvas.drawLine(0f, 0f, res.x.toFloat(), 0f, paintBlack)
        canvas.drawLine(0f, 0f, 0f, res.y.toFloat(), paintBlack)
        canvas.drawLine(res.x.toFloat(), 0f, res.x.toFloat(), res.y.toFloat(), paintBlack)
        canvas.drawLine(0f, res.y.toFloat(), res.x.toFloat(), res.y.toFloat(), paintBlack)
        canvas.drawLine(
            (gapStart - ballRadius).toFloat(),
            res.y.toFloat(),
            (gapEnd + ballRadius).toFloat(),
            res.y.toFloat(),
            paintWhite
        )
        canvas.drawBitmap(
            ballBitmap,
            ballPos.x.toFloat(),
            ballPos.y.toFloat(),
            null
        )
        if (isBallGon) {
            if (email.isNullOrBlank() || email.equals("null")) {
                canvas.drawText(
                    "enhorabuena",
                    (res.x / 3).toFloat(),
                    (res.y / 2).toFloat(),
                    paintBlack
                )
                canvas.drawText(
                        "anónimo",
                    (res.x / 3).toFloat(),
                    (res.y / 2).toFloat() + 100,
                    paintBlack
                )
            } else {
                canvas.drawText(
                    "enhorabuena",
                    (res.x / 3).toFloat(),
                    (res.y / 2).toFloat(),
                    paintBlack
                )
                canvas.drawText(
                    email.toString(),
                    (res.x / 3).toFloat(),
                    (res.y / 2).toFloat() + 100,
                    paintBlack
                )
            }

            runnable.terminate()
        }
    }

    fun reDraw() {
        invalidate()
    }

    private fun slowBall() {
        // X axis
        ballSpeedX = when {
            ballSpeedX < -slowing -> {
                ballSpeedX + slowing
            }
            ballSpeedX > slowing -> {
                ballSpeedX - slowing
            }
            else -> {
                0.0
            }
        }

        // Y axis
        ballSpeedY = when {
            ballSpeedY < -slowing -> {
                ballSpeedY + slowing
            }
            ballSpeedY > slowing -> {
                ballSpeedY - slowing
            }
            else -> {
                0.0
            }
        }
    }

    private fun checkBorderColisions() {

        // Let it go in to the gap
        if (ballPos.y > res.y / 2 && ballPos.x > gapStart - ballRadius && ballPos.x < gapEnd - ballRadius) {

            // If it's in, don't let it go to the sides
            if (ballPos.y > boundsEnd.y) {
                if (ballPos.x < gapStart) {
                    ballPos.x = gapStart
                } else if (ballPos.x > gapEnd + ballDiameter) {
                    ballPos.x = gapEnd - ballDiameter
                }
            }
            return
        }


        // X axis
        if (ballPos.x + ballBitmap.width > boundsEnd.x) {
            ballPos.x = boundsEnd.x - ballBitmap.width - 1
            ballSpeedX = -(ballSpeedX / 2)
        } else if (ballPos.x < boundsStart.x) {
            ballPos.x = boundsStart.x + 1
            ballSpeedX = -(ballSpeedX / 2)
        }

        // Y axis
        if (ballPos.y + ballBitmap.height > boundsEnd.y) {
            ballPos.y = boundsEnd.y - ballBitmap.height + 1
            ballSpeedY = -(ballSpeedY / 2)
        } else if (ballPos.y < boundsStart.y) {
            ballPos.y = boundsStart.y - 1
            ballSpeedY = -(ballSpeedY / 2)
        }
    }

    private fun checkIfBallsGone() {
        if (ballPos.x > res.x + ballRadius || ballPos.x < -ballRadius || ballPos.y > res.y + ballRadius || ballPos.y < -ballRadius) {
            isBallGon = true
            if (!mediaPlayer.isPlaying) {
                mediaPlayer.start()
            }
        } else {
            isBallGon = false
            if (mediaPlayer.isPlaying) {
                resetMediaPlayer()
            }
        }
    }

    private fun resetMediaPlayer() {
        mediaPlayer.stop()
        try {
            mediaPlayer.prepare()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        runnable.run()
    }

    fun reset() {
        resetMediaPlayer()
        ballPos.x = res.x / 2 - ballDiameter / 2
        ballPos.y = res.y / 2 - ballDiameter / 2
        ballSpeedX = 0.0
        ballSpeedY = 0.0
        runnable = GameLooper(this)
        Thread(runnable).start()
    }

    fun pauseMusic() {
        if (mediaPlayer.isPlaying) {
            mediaPlayer.pause()
            mediaPlayerPaused = true
        }
    }

    fun resumeMusic() {
        if (mediaPlayerPaused) {
            mediaPlayer.start()
            mediaPlayerPaused = false
        }
    }

    override fun onSensorChanged(event: SensorEvent) {
        synchronized(this) {
            if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                senValX = event.values[0].toDouble()
                senValY = event.values[1].toDouble()
            }
            if (senValX < deadzone && senValX > -deadzone) {
                senValX = 0.0
            }
            if (senValY < deadzone && senValY > -deadzone) {
                senValY = 0.0
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        // TODO Auto-generated method stub
    }

    private fun getNameChars() : Int {
        var numChars = 0
        email?.forEach {
            if (it == '@') {
                return numChars
            } else {
                numChars++
            }
        }
        return numChars
    }

    init {

        email = email?.substring(0, getNameChars())

        ballRadius = ballDiameter / 2

        // Load the ball
        val resources = resources
        ballBitmap = BitmapFactory.decodeResource(
            resources,
            R.drawable.ball
        )
        ballBitmap = getResizedBitmap(ballBitmap, ballDiameter, ballDiameter)

        // Set the ball's coordinates
        ballPos = Point()
        ballPos.x = res.x / 2 - ballBitmap.width / 2
        ballPos.y = res.y / 2 - ballBitmap.height / 2

        // Stablish the gap
        gapStart = (res.x / 2 - ballDiameter / 1.9).toInt()
        gapEnd = (res.x / 2 + ballDiameter / 1.9).toInt()

        // Initialize paint black
        paintBlack = Paint()
        paintBlack.color = Color.BLACK
        paintBlack.strokeWidth = (res.x / 36.0).toFloat()
        paintBlack.textSize = (res.x / 10.8).toFloat()

        // Initialize paint white
        paintWhite = Paint()
        paintWhite.color = Color.WHITE
        paintWhite.strokeWidth = (res.x / 36.0).toFloat()

        // Set the bounds to work with
        boundsStart = Point()
        boundsStart.x = paintBlack.strokeWidth.toInt() / 2
        boundsStart.y = paintBlack.strokeWidth.toInt() / 2
        boundsEnd = Point()
        boundsEnd.x = (res.x - paintBlack.strokeWidth / 2).toInt()
        boundsEnd.y = (res.y - paintBlack.strokeWidth / 2).toInt()

        // Initialize media player
        mediaPlayer = MediaPlayer.create(context, R.raw.crabrave)
        runnable = GameLooper(this)
        Thread(runnable).start()
    }
}