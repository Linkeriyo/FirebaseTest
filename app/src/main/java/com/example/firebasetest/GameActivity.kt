package com.example.firebasetest

import android.graphics.Point
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class GameActivity : AppCompatActivity() {
    var sensorManager: SensorManager? = null
    var acelerometerSensor: Sensor? = null
    var ballView: BallView? = null

    @RequiresApi(api = Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val email = FirebaseAuth.getInstance().currentUser?.email
        val resolution = Point()
        display!!.getSize(resolution)
        ballView = BallView(this, resolution, email)
        setContentView(ballView)
        ballView!!.setOnClickListener { v: View? ->
            if (ballView!!.isBallGon) {
                ballView!!.reset()
            }
        }

        // Initialize sensors
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        acelerometerSensor = sensorManager!!.getSensorList(Sensor.TYPE_ACCELEROMETER)[0]
    }

    override fun onResume() {
        super.onResume()
        sensorManager!!.registerListener(
            ballView, acelerometerSensor,
            SensorManager.SENSOR_DELAY_NORMAL
        )
        // Hide the status bar.
        val decorView = window.decorView
        val uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN
        decorView.systemUiVisibility = uiOptions
        ballView!!.resumeMusic()
    }

    override fun onPause() {
        super.onPause()
        sensorManager!!.unregisterListener(ballView)
        ballView!!.pauseMusic()
    }

    override fun onDestroy() {
        super.onDestroy()
        sensorManager!!.unregisterListener(ballView)
        ballView!!.pauseMusic()
        FirebaseAuth.getInstance().signOut()
    }
}