package de.rogallab.mobile.data.repositories
import android.content.Context
import android.content.SharedPreferences
import de.rogallab.mobile.data.ISettingsRepository
import de.rogallab.mobile.domain.entities.SensorSettings

class SettingsRepository(
   context: Context
): ISettingsRepository {

   private val sharedPreferences: SharedPreferences =
      context.getSharedPreferences("sensor_settings", Context.MODE_PRIVATE)

   override fun getSensorSettings(): SensorSettings {
      return SensorSettings(
         isLocationSensorEnabled = sharedPreferences.getBoolean("locations_sensor", false),
         isPressureSensorEnabled = sharedPreferences.getBoolean("pressure_sensor", true),
         isLightSensorEnabled = sharedPreferences.getBoolean("light_sensor", true),
         isTemperatureSensorEnabled = sharedPreferences.getBoolean("temperature_sensor", true),
         isHumiditySensorEnabled = sharedPreferences.getBoolean("humidity_sensor", true)
      )
   }

   override fun saveSensorSettings(settings: SensorSettings) {
      sharedPreferences.edit().apply {
         putBoolean("pressure_sensor", settings.isPressureSensorEnabled)
         putBoolean("light_sensor", settings.isLightSensorEnabled)
         putBoolean("temperature_sensor", settings.isTemperatureSensorEnabled)
         putBoolean("humidity_sensor", settings.isHumiditySensorEnabled)
         apply()
      }
   }
}