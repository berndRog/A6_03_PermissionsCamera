package de.rogallab.mobile.ui.sensors.environment_orientation

sealed class SensorIntent {
   data object Start : SensorIntent()
   data object Stop: SensorIntent()
}