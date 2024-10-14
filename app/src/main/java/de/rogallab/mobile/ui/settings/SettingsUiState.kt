package de.rogallab.mobile.ui.settings


data class SettingsUiState(
   val isLocationSensorEnabled: Boolean = false,
   val isPressureSensorEnabled: Boolean = true,
   val isLightSensorEnabled: Boolean = true,
   val isOrientationSensorEnabled: Boolean = false,
)