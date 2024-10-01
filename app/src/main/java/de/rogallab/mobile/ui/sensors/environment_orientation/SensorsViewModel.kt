package de.rogallab.mobile.ui.sensors.environment_orientation

import android.app.Application
import android.content.Context
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import de.rogallab.mobile.domain.utilities.logInfo
import de.rogallab.mobile.domain.utilities.toLocalDateTime
import de.rogallab.mobile.domain.utilities.toTimeString
import de.rogallab.mobile.ui.base.BaseViewModel
import de.rogallab.mobile.ui.sensors.environment_orientation.AppSensorsManager.Companion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class SensorsViewModel(
   application: Application
) : BaseViewModel(TAG), LifecycleEventObserver {

   private val _context: Context = application.applicationContext

   // Environment & Orientation sensors manager
   private val _sensorsManager = AppSensorsManager(_context)

   // Expose sensor updates to the UI
   private var _sensorsUiStateFlow: MutableStateFlow<SensorsUiState> =
      MutableStateFlow(SensorsUiState())
   val sensorsUiStateFlow: StateFlow<SensorsUiState> =
      _sensorsUiStateFlow.asStateFlow()

   // Observe lifecycle events
   override fun onStateChanged(
      source: LifecycleOwner,
      event: Lifecycle.Event
   ) {

      when (event) {
         Lifecycle.Event.ON_START -> {
            // Start orientation updates when the lifecycle enters the started state
            logInfo(TAG, "onStateChanged: ON_START")
            _sensorsManager.startListening()
         }
         Lifecycle.Event.ON_STOP -> {
            // Stop orientation updates when the lifecycle stops
            logInfo(TAG, "onStateChanged: ON_STOP")
            _sensorsManager.stopListening()
         }
         Lifecycle.Event.ON_RESUME -> {
            logInfo(TAG, "onStateChanged")
            _sensorsManager.onSensorValuesChanged = { sensorValues: SensorValues ->
               _sensorsUiStateFlow.update { sensorUiState: SensorsUiState ->
                  val dt = toLocalDateTime(sensorValues.epochMillis).toTimeString()
                  logInfo(TAG, "$dt yaw(azimuth)/pitch/roll " +
                     "${sensorValues.yaw} ${sensorValues.pitch} ${sensorValues.roll}")
                  // Add the new orientationValue to the ringBuffer of orientationValues
                  sensorUiState.ringBuffer.add(sensorValues)
                  // Update the last orientationValue
                  sensorUiState.copy(last = sensorValues)
               }
            }
         }
         else -> {}
      }
   }

   override fun onCleared() {
      super.onCleared()
      _sensorsManager.stopListening()
   }

   companion object {
      const val TAG = "<-SensorsViewModel"
   }
}