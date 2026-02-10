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
 * iOS implementation of DebugMenuTrigger.
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
     * Trigger that detects device shake using CoreMotion.
     *
     * @param threshold G force threshold to detect shake (default: 2.5)
     * @param slopTime Minimum time between shakes in milliseconds (default: 500ms)
     */
    data class Shake(val threshold: Double = 2.5, val slopTime: Long = 500L,) : DebugMenuTrigger {
        @OptIn(ExperimentalForeignApi::class)
        @Composable
        override fun Effect(onTrigger: () -> Unit) {
            val motionManager = remember { CMMotionManager() }

            DisposableEffect(motionManager, onTrigger) {
                var lastShakeTime: Long = 0

                if (motionManager.accelerometerAvailable) {
                    motionManager.accelerometerUpdateInterval = 0.1
                    motionManager.startAccelerometerUpdatesToQueue(
                        NSOperationQueue.mainQueue,
                    ) { data, _ ->
                        data?.acceleration?.useContents {
                            val acceleration = sqrt(x * x + y * y + z * z)
                            val currentTime = (NSDate().timeIntervalSince1970 * 1000).toLong()

                            if (acceleration > threshold && currentTime - lastShakeTime > slopTime) {
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
    }
}
