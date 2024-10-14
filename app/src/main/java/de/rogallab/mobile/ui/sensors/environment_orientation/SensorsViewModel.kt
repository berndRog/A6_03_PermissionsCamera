package de.rogallab.mobile.ui.sensors.environment_orientation

import android.app.Application
import android.content.Context
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewModelScope
import de.rogallab.mobile.domain.sensors.AppSensorManager
import de.rogallab.mobile.domain.sensors.SensorValues
import de.rogallab.mobile.domain.utilities.logInfo
import de.rogallab.mobile.domain.utilities.toLocalDateTime
import de.rogallab.mobile.domain.utilities.toTimeString
import de.rogallab.mobile.ui.base.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SensorsViewModel(
   application: Application
) : BaseViewModel(TAG), LifecycleEventObserver {

   private val _context: Context = application.applicationContext
   private val _sensorsManager = AppSensorManager(_context)

   // Expose sensor updates to the UI
   private val _sensorsUiStateFlow = MutableStateFlow(SensorsUiState())
   val sensorsUiStateFlow: StateFlow<SensorsUiState> = _sensorsUiStateFlow.asStateFlow()

   private var shouldListen = false
   private var canListen = false

   fun processIntent(intent: SensorIntent) {
      when (intent) {
         SensorIntent.Start -> startListeningIfPossible()
         SensorIntent.Stop -> stopListening()
      }
   }

   private fun startListeningIfPossible() {
      shouldListen = true
      if (canListen) _sensorsManager.startListening()
   }

   private fun stopListening() {
      shouldListen = false
      _sensorsManager.stopListening()
   }

   override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
      when (event) {
         Lifecycle.Event.ON_START -> onLifecycleStart()
         Lifecycle.Event.ON_STOP -> onLifecycleStop()
         Lifecycle.Event.ON_RESUME -> onLifecycleResume()
         else -> {}
      }
   }

   private fun onLifecycleStart() {
      logInfo(TAG, "onStateChanged: ON_START")
      if (shouldListen) _sensorsManager.startListening()
      canListen = true
   }

   private fun onLifecycleStop() {
      logInfo(TAG, "onStateChanged: ON_STOP")
      stopListening()
      canListen = false
   }

   private fun onLifecycleResume() {
      logInfo(TAG, "onStateChanged: ON_RESUME")
      viewModelScope.launch {
         _sensorsManager.sensorValuesFlow().collect{ sensorValues ->
            updateSensorUiState(sensorValues)
         }
      }
   }

   private fun updateSensorUiState(sensorValues: SensorValues) {
      _sensorsUiStateFlow.update { sensorUiState ->
         val timeString = toLocalDateTime(sensorValues.time).toTimeString()
         logInfo(TAG, "$timeString yaw/pitch/roll: ${sensorValues.yaw}, ${sensorValues.pitch}, ${sensorValues.roll}")
         sensorUiState.ringBuffer.add(sensorValues)
         sensorUiState.copy(last = sensorValues)
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