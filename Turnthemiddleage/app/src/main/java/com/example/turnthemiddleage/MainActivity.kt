package com.example.turnthemiddleage

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.widget.TextView
import androidx.activity.ComponentActivity
import kotlin.math.absoluteValue

class MainActivity : ComponentActivity(), SensorEventListener {

    private var sensorManager: SensorManager? = null
    private var accSensor: Sensor? = null
    private val accMax = floatArrayOf(0f, 0f, 0f)

    override fun onCreate(savedInstanceState: Bundle?) {
        val levelhandler = levelhandler(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.firstrun)

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        accSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        if (accSensor == null) {
            setContentView(R.layout.device_is_not_supported)
        }
    }

    override fun onResume() {
        super.onResume()
        accSensor?.let { sensor ->
            sensorManager?.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager?.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null || event.sensor.type != Sensor.TYPE_ACCELEROMETER) return

        for (i < 0) {

        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }
}
