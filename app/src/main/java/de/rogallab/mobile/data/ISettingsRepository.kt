package de.rogallab.mobile.data
import de.rogallab.mobile.domain.entities.SensorSettings

interface ISettingsRepository {
   fun getSensorSettings(): SensorSettings
   fun saveSensorSettings(settings: SensorSettings)
}