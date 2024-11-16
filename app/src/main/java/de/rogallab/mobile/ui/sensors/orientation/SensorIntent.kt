package de.rogallab.mobile.ui.sensors.orientation

sealed class SensorIntent {
   data object Start : SensorIntent()
   data object Stop: SensorIntent()
}