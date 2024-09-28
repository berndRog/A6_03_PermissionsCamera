package de.rogallab.mobile.ui.sensors.environment_orientation

data class SensorValues(
   val epochMillis: Long = System.currentTimeMillis(), // epoch

   // Environment ----------------------------------------------------
   // pressure in milli Pascal
   val pressure: Float = 0.0f,
   // light in lumen
   val light: Float = 0.0f,
   // temperature in
   val temperature: Float = 0.0f,
   // relative humidity in percent
   val humidity: Float = 0.0f,

   // Orientation ----------------------------------------------------
   // yaw is the rotation around the vertical axis
   val yaw: Float = 0.0f,
   // pitch is the rotation around the wings (transverse axis)
   val pitch: Float = 0.0f,
   // roll is the rotation around the fuselage (longitudinal axis)
   val roll: Float = 0.0f,
   // azimuth is the angle between the magnetic north direction and the direction of the object
   val azimuth: Float = 0.0f
)
