/*
 * Copyright (C) 2021 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.pocketmode

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log

import java.io.File
import java.util.concurrent.Executors

class PocketSensor(context: Context, sensorType: String) : SensorEventListener {
    private val sensorManager = context.getSystemService(SensorManager::class.java)
    private val sensor = sensorManager.getSensorList(Sensor.TYPE_ALL).find {
        it.stringType == sensorType
    }

    private val executorService = Executors.newSingleThreadExecutor()

    override fun onSensorChanged(event: SensorEvent) {
        File(FINGERPRINT_DISABLE_PATH).writeText(if (event.values[0] == 0.0f) "1" else "0")
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}

    fun enable() {
        if (sensor != null) {
            Log.d(TAG, "Enabling")
            executorService.submit {
                sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
            }
        }
    }

    fun disable() {
        if (sensor != null) {
            Log.d(TAG, "Disabling")
            executorService.submit {
                sensorManager.unregisterListener(this, sensor)
                File(FINGERPRINT_DISABLE_PATH).writeText("0")
            }
        }
    }

    companion object {
        private const val TAG = "PocketSensor"

        private const val FINGERPRINT_DISABLE_PATH =
            "/sys/devices/platform/soc/soc:goodix_fp/proximity_state"
    }
}
