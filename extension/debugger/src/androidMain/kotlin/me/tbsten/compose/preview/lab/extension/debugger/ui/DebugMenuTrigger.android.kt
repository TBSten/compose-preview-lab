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
 * Android implementation of DebugMenuTrigger.
 */
actual sealed interface DebugMenuTrigger {
    @Composable
    actual fun Effect(onTrigger: () -> Unit)

    actual companion object {
        actual fun default(): DebugMenuTrigger = Shake()
        actual val None: DebugMenuTrigger = NoneImpl
    }

    private data object NoneImpl : DebugMenuTrigger {
        @Composable
        override fun Effect(onTrigger: () -> Unit) {
            // No-op
        }
    }

    /**
     * Trigger that detects device shake using the accelerometer.
     *
     * @param threshold Acceleration threshold to detect shake (default: 15.0f)
     * @param slopTime Minimum time between shakes in milliseconds (default: 500ms)
     */
    data class Shake(val threshold: Float = 15.0f, val slopTime: Long = 500L,) : DebugMenuTrigger {
        @Composable
        override fun Effect(onTrigger: () -> Unit) {
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

                val listener = object : SensorEventListener {
                    override fun onSensorChanged(event: SensorEvent?) {
                        if (event == null) return

                        val x = event.values[0]
                        val y = event.values[1]
                        val z = event.values[2]

                        val acceleration = sqrt(x * x + y * y + z * z) - SensorManager.GRAVITY_EARTH
                        val currentTime = System.currentTimeMillis()

                        if (acceleration > threshold && currentTime - lastShakeTime > slopTime) {
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
    }
}
