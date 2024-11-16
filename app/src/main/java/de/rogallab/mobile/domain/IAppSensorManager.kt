package de.rogallab.mobile.domain

import android.hardware.SensorEventListener
import de.rogallab.mobile.domain.model.SensorValue
import kotlinx.coroutines.flow.Flow

interface IAppSensorManager {
   fun setUpdateInterval(interval: Long)
   fun sensorValuesFlow(): Flow<SensorValue>
}
