package de.rogallab.mobile.ui.sensors.location

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.Location
import androidx.lifecycle.viewModelScope
import de.rogallab.mobile.domain.IAppLocationManager
import de.rogallab.mobile.domain.model.LocationValue
import de.rogallab.mobile.domain.utilities.logError
import de.rogallab.mobile.ui.base.BaseViewModel
import de.rogallab.mobile.ui.sensors.location.services.AppLocationService
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent

class LocationsViewModel(
   application: Application,
   private val _locationManager: IAppLocationManager,
   private val _ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : BaseViewModel(TAG), KoinComponent {

   private val _context: Context = application.applicationContext

   private val coroutineExceptionHandler = CoroutineExceptionHandler { _, exception ->
      // Handle the exception here
      logError(TAG, "Coroutine exception: ${exception.localizedMessage}")
   }

   // Expose location updates to the UI
   private val _locationUiStateFlow: MutableStateFlow<LocationUiState> =
      MutableStateFlow(LocationUiState())
   val locationUiStateFlow: StateFlow<LocationUiState> =
      _locationUiStateFlow.asStateFlow()

   // define a broadcast receiver to receive location updates (from tracking service)
   private val locationReceiver = object : BroadcastReceiver() {
      override fun onReceive(context: Context?, intent: Intent?) {
         val time = intent?.getLongExtra("time", 0L) ?: 0L
         val latitude = intent?.getDoubleExtra("latitude", 0.0) ?: 0.0
         val longitude = intent?.getDoubleExtra("longitude", 0.0) ?: 0.0
         val location = Location("dummyprovider").apply {
            this.time = time
            this.latitude = latitude
            this.longitude = longitude
         }
         // get location updates (tracking data)
         // via broadcast from the location manager
         processLocation(location)
      }
   }

   private fun processLocation(location: Location) {
      _locationUiStateFlow.update { it: LocationUiState ->
         // new location value object
         val locationValue = LocationValue(
            time = location.time,
            latitude = location.latitude,
            longitude = location.longitude,
         )
         // add the new locationValue to the ringBuffer
         it.ringBuffer.add(locationValue)
         // update the last locationValue
         it.copy(
            last = locationValue,
            ringBuffer = it.ringBuffer
         )
      }
   }

   init {
      val filter = IntentFilter("LOCATION_UPDATE")
      application.registerReceiver(locationReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
   }


   fun processIntent(intent: LocationIntent) {
      when (intent) {
         is LocationIntent.GetLocation -> getLocation()
         is LocationIntent.StartLocationService -> startLocationService()
         is LocationIntent.StopLocationService -> stopLocationService()
      }
   }

   // get the initial location at start
   private fun getLocation(){
      // get last known location and update the locationflow
      _locationManager.getLocation { location ->
         processLocation(location)
      }
   }

   private fun startLocationService() {
      viewModelScope.launch {
         withContext(_ioDispatcher + coroutineExceptionHandler) {
            Intent(_context, AppLocationService::class.java).apply {
               action = AppLocationService.Action.START.name
               _context.startService(this)
            }
         }
      }
   }

   private fun stopLocationService() {
      viewModelScope.launch {
         withContext(_ioDispatcher + coroutineExceptionHandler) {
            Intent(_context, AppLocationService::class.java).apply {
               action = AppLocationService.Action.STOP.name
               _context.stopService(this)
            }
         }
      }
   }

   override fun onCleared() {
      stopLocationService()
      super.onCleared()
   }

   companion object {
      const val TAG = "<-LocationsViewModel"
   }
}