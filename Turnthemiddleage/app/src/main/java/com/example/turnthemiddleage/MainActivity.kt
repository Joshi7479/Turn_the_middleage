package com.example.turnthemiddleage

import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.ComponentActivity

class MainActivity : ComponentActivity(), SensorEventListener {

    private var sensorManager: SensorManager? = null
    private var accSensor: Sensor? = null
    private var gravityInitialized = false
    private lateinit var numberview: TextView
    private val gravity = FloatArray(3)
    private var prof: ImageView? = null
    private var waitingForLift = false

    override fun onCreate(savedInstanceState: Bundle?) {
        val levelhandler = levelhandler(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.firstrun)

        var currentlevel = levelhandler.loadLevel()

        val startbutton = findViewById<Button>(R.id.start)

        if (currentlevel != 0) {

        }

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        accSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        if (accSensor == null) {
            setContentView(R.layout.device_is_not_supported)
        }

        startbutton.setOnClickListener {
            setContentView(R.layout.introduction)
            numberview = findViewById(R.id.numberview)
            val progressbar = findViewById<ProgressBar>(R.id.connectbar)
            val textfield = findViewById<TextView>(R.id.textfield)
            prof = findViewById(R.id.prof)
            currentlevel = 1
            levelhandler.saveLevel(currentlevel)

            progressbar.visibility = View.GONE
            prof?.visibility = View.VISIBLE
            textfield.text = getString(R.string.tryit)

            gravityInitialized = false
            waitingForLift = true
        }
    }

    private fun upanddown(view: View, height: Float = 300f, onEnd: (() -> Unit)? = null) {
        view.animate()
            .translationY(-height)
            .setDuration(400)
            .setInterpolator(DecelerateInterpolator())
            .withEndAction {
                view.animate()
                    .translationY(0f)
                    .setDuration(500)
                    .setInterpolator(AccelerateInterpolator())
                    .withEndAction { onEnd?.invoke() }
                    .start()
            }
            .start()
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

        if (::numberview.isInitialized) {
            numberview.text = "%.1f".format(up)
        }

        if (waitingForLift && up > 6f) {
            waitingForLift = false
            val px = 100 * resources.displayMetrics.density
            prof?.let {
                upanddown(it, px) {
                    startActivity(Intent(this@MainActivity, levellogic::class.java))
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }
}