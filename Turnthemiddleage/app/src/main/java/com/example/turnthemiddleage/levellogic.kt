package com.example.turnthemiddleage

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import androidx.activity.ComponentActivity

class levellogic : ComponentActivity(), SensorEventListener {

    private lateinit var gameView: GameView
    private var sensorManager: SensorManager? = null
    private var accSensor: Sensor? = null
    private val gravity = FloatArray(3)
    private var gravityInitialized = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.levelone)

        gameView = findViewById(R.id.gameView)

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        accSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        if (accSensor == null) {
            setContentView(R.layout.device_is_not_supported)
        }
    }

    override fun onResume() {
        super.onResume()
        gameView.resume()
        gravityInitialized = false
        accSensor?.let { sensor ->
            sensorManager?.registerListener(this, sensor, SensorManager.SENSOR_DELAY_GAME)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager?.unregisterListener(this)
        gameView.pause()
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null || event.sensor.type != Sensor.TYPE_ACCELEROMETER) return

        if (!gravityInitialized) {
            event.values.copyInto(gravity, endIndex = 3)
            gravityInitialized = true
            return
        }

        val alpha = 0.8f
        for (i in 0..2) {
            gravity[i] = alpha * gravity[i] + (1 - alpha) * event.values[i]
        }

        val lx = event.values[0] - gravity[0]
        val ly = event.values[1] - gravity[1]
        val lz = event.values[2] - gravity[2]

        val gNorm = kotlin.math.sqrt(
            gravity[0] * gravity[0] + gravity[1] * gravity[1] + gravity[2] * gravity[2]
        )
        if (gNorm < 1f) return
        val up = (lx * gravity[0] + ly * gravity[1] + lz * gravity[2]) / gNorm

        if (up > 6f) {
            gameView.jump()
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }
}