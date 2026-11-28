package com.example.turnthemiddleage

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.view.Surface
import androidx.activity.ComponentActivity
import android.graphics.RectF
import android.widget.Button

class levellogic : ComponentActivity(), SensorEventListener {

    private lateinit var gameView: GameView
    private var sensorManager: SensorManager? = null
    private var accSensor: Sensor? = null
    private val gravity = FloatArray(3)
    private var gravityInitialized = false
    private var gyrSensor: Sensor? = null
    private var gyrListener: SensorEventListener? = null
    private var tiltDirection = 0
    private val tiltOn = 20f
    private val tiltOff = 10f
    private var level: Int? = 1
    private lateinit var handler: levelhandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.gameview)
        handler = levelhandler(this) {loadview(1)}
        var currentlevel = handler.loadLevel()

        gameView = findViewById(R.id.gameView)
        gameView.onGoalReached = { handler.leveldone(true) }

        gameView.pause()

        if (currentlevel == 1) {
            var currentlevelplatforms = listOf(
                RectF(600f, 1650f, 950f, 1700f),
                RectF(100f, 1200f, 450f, 1250f),
                RectF(1200f, 1500f, 1600f, 1550f),
                RectF(1800f, 1200f, 2200f, 1250f),
                RectF(2500f, 1650f, 2900f, 1700f),
                RectF(3000f, 1700f, 3500f, 1750f)
            )
            gameView.loadLevel(currentlevelplatforms)
        } else if (currentlevel == 2) {
            var currentlevelplatforms = listOf(
                RectF(600f, 1650f, 950f, 1700f),
                RectF(100f, 1200f, 450f, 1250f),
                RectF(1200f, 1500f, 1600f, 1550f),
                RectF(2000f, 1500f, 2200f, 1550f),
                RectF(2500f, 1650f, 2900f, 1700f),
                RectF(3000f, 1700f, 3500f, 1750f)
            )
            gameView.loadLevel(currentlevelplatforms)
        } else if (currentlevel == 3) {
            var currentlevelplatforms = listOf(
                RectF(600f, 1650f, 950f, 1700f),
                RectF(100f, 1200f, 450f, 1250f),
                RectF(1200f, 1500f, 1600f, 1550f),
                RectF(1800f, 1200f, 2200f, 1250f),
                RectF(2500f, 1650f, 2900f, 1700f),
                RectF(3000f, 1700f, 3500f, 1750f)
            )
            gameView.loadLevel(currentlevelplatforms)
        } else {
            setContentView(R.layout.everyleveldone)
            val restartbutton = findViewById<Button>(R.id.restart)
            restartbutton.setOnClickListener {
                currentlevel = 1
                handler.saveLevel(currentlevel)
                recreate()
            }
        }

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        gyrSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        accSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        if (accSensor == null) {
            setContentView(R.layout.device_is_not_supported)
        }
    }

    override fun onResume() {
        super.onResume()
        gameView.resume()
        gravityInitialized = false
        tiltDirection = 0
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

        val screenX = when (gameView.display?.rotation) {
            Surface.ROTATION_90 -> -gravity[1]
            Surface.ROTATION_180 -> -gravity[0]
            Surface.ROTATION_270 -> gravity[1]
            else -> gravity[0]
        }

        val sinTilt = (-screenX / gNorm).coerceIn(-1f, 1f)
        val tilt = Math.toDegrees(kotlin.math.asin(sinTilt).toDouble()).toFloat()

        if (tilt > tiltOn) {
            gameView.moveRight()
        } else if (tilt < -tiltOn) {
            gameView.moveLeft()
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }

    fun loadview(viewnumber: Int) {
        var currentlevel = handler.loadLevel()
        if (viewnumber == 1) {
            runOnUiThread {
                gameView.pause()
                setContentView(R.layout.levelwon)
                val nextlevelbutton = findViewById<Button>(R.id.newlevel)
                nextlevelbutton.setOnClickListener {
                    recreate()
                }
            }
        } else if (viewnumber == 2) {
            runOnUiThread {
                gameView.pause()
                setContentView(R.layout.youloose)
                val retrybutton = findViewById<Button>(R.id.retry)
                retrybutton.setOnClickListener {
                    recreate()
                }
            }
        } else {
            gameView.pause()
            handler.saveLevel(currentlevel)
        }
    }
}