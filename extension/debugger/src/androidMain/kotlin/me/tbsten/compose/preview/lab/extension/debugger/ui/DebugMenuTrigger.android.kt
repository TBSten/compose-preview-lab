package me.tbsten.compose.preview.lab.extension.debugger.ui

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import kotlin.math.sqrt

/**
 * Android implementation of DebugMenuTrigger using shake detection.
 */
@Composable
actual fun DebugMenuTrigger(onTrigger: () -> Unit,) {
    val context = LocalContext.current
    val sensorManager = remember {
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }
    val accelerometer = remember {
        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    }

    DisposableEffect(accelerometer, onTrigger) {
        if (accelerometer == null) {
            return@DisposableEffect onDispose { }
        }

        var lastShakeTime: Long = 0
        val shakeThreshold = 15.0f // acceleration threshold
        val shakeSlopTime = 500L // minimum time between shakes

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if (event == null) return

                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]

                // Calculate acceleration without gravity
                val acceleration = sqrt(x * x + y * y + z * z) - SensorManager.GRAVITY_EARTH
                val currentTime = System.currentTimeMillis()

                if (acceleration > shakeThreshold && currentTime - lastShakeTime > shakeSlopTime) {
                    lastShakeTime = currentTime
                    onTrigger()
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                // Not used
            }
        }

        sensorManager.registerListener(
            listener,
            accelerometer,
            SensorManager.SENSOR_DELAY_UI,
        )

        onDispose {
            sensorManager.unregisterListener(listener)
        }
    }
}
