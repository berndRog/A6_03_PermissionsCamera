package de.rogallab.mobile.ui.sensors.orientation

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.lifecycle.viewModelScope
import de.rogallab.mobile.domain.IAppSensorManager
import de.rogallab.mobile.ui.sensors.orientation.services.AppSensorService
import de.rogallab.mobile.domain.model.SensorValue
import de.rogallab.mobile.domain.utilities.logDebug
import de.rogallab.mobile.domain.utilities.logError
import de.rogallab.mobile.domain.utilities.logInfo
import de.rogallab.mobile.domain.utilities.toLocalDateTime
import de.rogallab.mobile.domain.utilities.toTimeString
import de.rogallab.mobile.ui.base.BaseViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class SensorsViewModel(
   private val _context: Context,
   private val _sensorManager: IAppSensorManager,
   private val _ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : BaseViewModel(TAG)  {

   private val _coroutineExceptionHandler = CoroutineExceptionHandler { _, exception ->
      // Handle the exception here
      logError(TAG, "Coroutine exception: ${exception.localizedMessage}")
   }

   // Expose sensor updates to the UI
   private val _sensorsUiStateFlow = MutableStateFlow(SensorsUiState())
   val sensorsUiStateFlow: StateFlow<SensorsUiState> = _sensorsUiStateFlow.asStateFlow()

   // define a broadcast receiver to receive location updates (from tracking service)
   private val orientationReceiver = object : BroadcastReceiver() {
      override fun onReceive(context: Context?, intent: Intent?) {
//         val time = intent?.getLongExtra("time", 0L) ?: 0L
//         val latitude = intent?.getDoubleExtra("latitude", 0.0) ?: 0.0
//         val longitude = intent?.getDoubleExtra("longitude", 0.0) ?: 0.0
//         val location = Location("dummyprovider").apply {
//            this.time = time
//            this.latitude = latitude
//            this.longitude = longitude
//         }
         // get location updates (tracking data)
         // via broadcast from the location manager
         //processOrientation(sensorValues)
      }
   }

   private fun processOrientation(sensorValues: SensorValue) {
      _sensorsUiStateFlow.update { it: SensorsUiState ->
         // new location value object
         // add the new locationValue to the ringBuffer
         it.ringBuffer.add(sensorValues)
         // update the last locationValue
         it.copy(
            last = sensorValues,
            ringBuffer = it.ringBuffer
         )
      }
   }

   fun processIntent(intent: SensorIntent) {
      when (intent) {
         SensorIntent.Start -> startSensorService()
         SensorIntent.Stop -> stopSensorService()
      }
   }

   private fun startSensorService() {
      val job = viewModelScope.launch(_ioDispatcher+_coroutineExceptionHandler) {
         try {
            Intent(_context, AppSensorService::class.java).apply {
               action = AppSensorService.Action.START.name
               _context.startService(this)
            }
         }  catch (e: Exception) {
            logError(TAG, "Exception in startSensorService: ${e.localizedMessage}")
         }
      }

      runBlocking {
         job.join()
         logDebug(TAG,"startSensorService: SensorsViewModel")

      }
   }

   private fun stopSensorService() {
      viewModelScope.launch {
         Intent(_context, AppSensorService::class.java).apply {
            action = AppSensorService.Action.STOP.name
            _context.stopService(this)
         }
      }
   }

   private fun onLifecycleResume() {
      logInfo(TAG, "onStateChanged: ON_RESUME")
      viewModelScope.launch {
         _sensorManager.sensorValuesFlow().collect{ sensorValues ->
            updateSensorUiState(sensorValues)
         }
      }
   }

   private fun updateSensorUiState(sensorValues: SensorValue) {
      _sensorsUiStateFlow.update { sensorUiState ->
         val timeString = toLocalDateTime(sensorValues.time).toTimeString()
         logInfo(TAG, "$timeString yaw/pitch/roll: ${sensorValues.yaw}, ${sensorValues.pitch}, ${sensorValues.roll}")
         sensorUiState.ringBuffer.add(sensorValues)
         sensorUiState.copy(last = sensorValues)
      }
   }

   override fun onCleared() {
      super.onCleared()
      stopSensorService()
   }

   companion object {
      const val TAG = "<-SensorsViewModel"
   }
}