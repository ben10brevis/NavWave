package com.lastwave.app.ui.glass

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch

/**
 * Observes device tilt using the gravity or accelerometer sensor,
 * producing a smoothed 2D offset for motion-reactive specular sheens.
 */
@Composable
fun rememberDeviceTilt(): State<Offset> {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val tiltX = remember { Animatable(0f) }
    val tiltY = remember { Animatable(0f) }

    DisposableEffect(context) {
        val sensorManager = runCatching {
            context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        }.getOrNull()

        val sensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
            ?: sensorManager?.getDefaultSensor(Sensor.TYPE_GRAVITY)
            ?: sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        if (sensorManager == null || sensor == null) {
            return@DisposableEffect onDispose {}
        }

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                // Pitch and roll normalized to approximately [-1f, 1f]
                val rawX: Float
                val rawY: Float
                if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
                    rawX = (event.values[1] * 2f).coerceIn(-1f, 1f)
                    rawY = (event.values[0] * 2f).coerceIn(-1f, 1f)
                } else {
                    rawX = (event.values[0] / 9.81f).coerceIn(-1f, 1f)
                    rawY = (event.values[1] / 9.81f).coerceIn(-1f, 1f)
                }

                scope.launch {
                    val springSpec = spring<Float>(
                        dampingRatio = Spring.DampingRatioLowBouncy,
                        stiffness = Spring.StiffnessLow,
                    )
                    tiltX.animateTo(rawX, springSpec)
                    tiltY.animateTo(rawY, springSpec)
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        sensorManager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_UI)

        onDispose {
            sensorManager.unregisterListener(listener)
        }
    }

    return remember {
        derivedStateOf { Offset(tiltX.value, tiltY.value) }
    }
}
