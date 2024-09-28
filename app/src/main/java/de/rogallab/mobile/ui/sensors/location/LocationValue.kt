package de.rogallab.mobile.ui.sensors.location

data class LocationValue(
   val epochMillis: Long = System.currentTimeMillis(),
   val latitude: Double = 0.0,
   val longitude: Double = 0.0,
   val altitude: Double = 0.0,
   val speed: Float = 0f
)