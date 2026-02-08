package me.tbsten.compose.preview.lab.extension.debugger.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.CoreMotion.CMMotionManager
import platform.Foundation.NSDate
import platform.Foundation.NSOperationQueue
import platform.Foundation.timeIntervalSince1970
import kotlin.math.sqrt

/**
 * iOS implementation of DebugMenuTrigger using shake detection via CoreMotion.
 */
@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun DebugMenuTrigger(onTrigger: () -> Unit,) {
    val motionManager = remember { CMMotionManager() }

    DisposableEffect(motionManager, onTrigger) {
        var lastShakeTime: Long = 0
        val shakeThreshold = 2.5 // G force threshold
        val shakeSlopTime = 500L // minimum time between shakes

        if (motionManager.accelerometerAvailable) {
            motionManager.accelerometerUpdateInterval = 0.1
            motionManager.startAccelerometerUpdatesToQueue(
                NSOperationQueue.mainQueue,
            ) { data, _ ->
                data?.acceleration?.useContents {
                    val acceleration = sqrt(x * x + y * y + z * z)
                    val currentTime = (NSDate().timeIntervalSince1970 * 1000).toLong()

                    if (acceleration > shakeThreshold && currentTime - lastShakeTime > shakeSlopTime) {
                        lastShakeTime = currentTime
                        onTrigger()
                    }
                }
            }
        }

        onDispose {
            motionManager.stopAccelerometerUpdates()
        }
    }
}
