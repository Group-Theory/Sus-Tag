package com.grouptheory.sus_tag

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import kotlin.math.pow
import kotlin.math.sqrt

class MotionDetector(context: MainActivity): SensorEventListener {
    private val context = context
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val sensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)
    private val accelThreshold = 10
    init {
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event != null) {
            val magnitudeX = event.values[0].pow(2)
            val magnitudeY = event.values[1].pow(2)
            val magnitudeZ = event.values[2].pow(2)
            val magnitude = sqrt(magnitudeX + magnitudeY + magnitudeZ)

            if (magnitude > accelThreshold) {
                Log.i("MotionDetector", magnitude.toString())
                context.accelDetected()
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        Log.i("MotionDetector", "Accuracy changed...")
    }
}