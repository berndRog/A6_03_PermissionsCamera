package de.rogallab.mobile.domain.entities


data class SensorSettings(
   val isLocationSensorEnabled: Boolean = false,
   val isOrientationSensorEnabled: Boolean = false,
   val isPressureSensorEnabled: Boolean = true,
   val isLightSensorEnabled: Boolean = true,
   val isTemperatureSensorEnabled: Boolean = true,
   val isHumiditySensorEnabled: Boolean = true
)