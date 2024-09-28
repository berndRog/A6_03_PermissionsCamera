package de.rogallab.mobile.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun SettingsScreen(
   settingsViewModel: SettingsViewModel = viewModel()
) {
   val sensorSettings by settingsViewModel.sensorSettings.collectAsState()

   Column(modifier = Modifier.padding(16.dp)) {
      Text("Sensor Settings", style = MaterialTheme.typography.displayMedium)

      SwitchSetting(
         label = "Pressure Sensor",
         isChecked = sensorSettings.isPressureSensorEnabled,
         onCheckedChange = { settingsViewModel.updateSettings(sensorSettings.copy(isPressureSensorEnabled = it)) }
      )

      SwitchSetting(
         label = "Light Sensor",
         isChecked = sensorSettings.isLightSensorEnabled,
         onCheckedChange = { settingsViewModel.updateSettings(sensorSettings.copy(isLightSensorEnabled = it)) }
      )

      SwitchSetting(
         label = "Temperature Sensor",
         isChecked = sensorSettings.isTemperatureSensorEnabled,
         onCheckedChange = { settingsViewModel.updateSettings(sensorSettings.copy(isTemperatureSensorEnabled = it)) }
      )

      SwitchSetting(
         label = "Humidity Sensor",
         isChecked = sensorSettings.isHumiditySensorEnabled,
         onCheckedChange = { settingsViewModel.updateSettings(sensorSettings.copy(isHumiditySensorEnabled = it)) }
      )
   }
}

@Composable
fun SwitchSetting(label: String, isChecked: Boolean, onCheckedChange: (Boolean) -> Unit) {
   Row(verticalAlignment = Alignment.CenterVertically) {
      Text(label, modifier = Modifier.weight(1f))
      Switch(checked = isChecked, onCheckedChange = onCheckedChange)
   }
}