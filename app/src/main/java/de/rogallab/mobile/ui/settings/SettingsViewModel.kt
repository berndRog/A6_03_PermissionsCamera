package de.rogallab.mobile.ui.settings

import android.app.Application
import android.content.Context
import androidx.lifecycle.viewModelScope
import de.rogallab.mobile.data.ISettingsRepository
import de.rogallab.mobile.data.repositories.SettingsRepository
import de.rogallab.mobile.domain.entities.SensorSettings
import de.rogallab.mobile.ui.base.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(
   application: Application
) : BaseViewModel(TAG) {

   // we must fix this by using a dependency injection framework
   private val _context: Context = application.applicationContext
   private val settingsRepository: ISettingsRepository =
      SettingsRepository(_context)

   // Expose sensor state to the UI via a StateFlow
   private val _sensorSettings = MutableStateFlow(SensorSettings())
   val sensorSettings: StateFlow<SensorSettings> = _sensorSettings

   init {
      loadSettings()
   }

   private fun loadSettings() {
      _sensorSettings.value = settingsRepository.getSensorSettings()
   }

   fun updateSettings(settings: SensorSettings) {
      _sensorSettings.value = settings
      saveSettings(settings)
   }

   private fun saveSettings(settings: SensorSettings) {
      viewModelScope.launch {
         settingsRepository.saveSensorSettings(settings)
      }
   }

   companion object {
      private const val TAG = "<-SettingsViewModel"
   }
}