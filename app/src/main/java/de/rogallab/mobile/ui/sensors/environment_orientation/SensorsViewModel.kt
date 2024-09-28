package de.rogallab.mobile.ui.sensors.environment_orientation

import android.app.Application
import android.content.Context
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import de.rogallab.mobile.domain.utilities.logInfo
import de.rogallab.mobile.ui.base.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class EnvOriSensorsViewModel(
   application: Application
) : BaseViewModel(application, TAG), LifecycleEventObserver {

   private val _context: Context = application.applicationContext

   // Environment & Orientation sensors manager
   private val _envOriSensorsManager = EnvOriSensorsManager(_context)

   // Expose sensor updates to the UI
   private var _envOriUiStateFlow: MutableStateFlow<EnvOriUiState> =
      MutableStateFlow(EnvOriUiState())
   val envOriUiStateFlow: StateFlow<EnvOriUiState> =
      _envOriUiStateFlow.asStateFlow()

   // Observe lifecycle events
   override fun onStateChanged(
      source: LifecycleOwner,
      event: Lifecycle.Event
   ) {

      when (event) {
         Lifecycle.Event.ON_START -> {
            // Start orientation updates when the lifecycle enters the started state
            logInfo(TAG, "onStateChanged: ON_START")
            _envOriSensorsManager.startListening()
         }
         Lifecycle.Event.ON_STOP -> {
            // Stop orientation updates when the lifecycle stops
            logInfo(TAG, "onStateChanged: ON_STOP")
            _envOriSensorsManager.stopListening()
         }
         Lifecycle.Event.ON_RESUME -> {
            logInfo(TAG, "onStateChanged")
            _envOriSensorsManager.onEnvOriValuesChanged = { envOriValues: SensorValues ->
               _envOriUiStateFlow.update { it: EnvOriUiState ->
                  // Add the new orientationValue to the ringBuffer of orientationValues
                  it.ringBuffer.add(envOriValues)
                  // Update the last orientationValue
                  it.copy(last = envOriValues)
               }
            }
         }
         else -> {}
      }
   }

   override fun onCleared() {
      super.onCleared()
      _envOriSensorsManager.stopListening()
   }

   companion object {
      const val TAG = "<-EnvOriSensorsViewModel"
   }
}