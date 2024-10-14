package de.rogallab.mobile.ui.sensors.location

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.Location
import androidx.lifecycle.viewModelScope
import de.rogallab.mobile.domain.location.AppLocationManager
import de.rogallab.mobile.domain.location.AppLocationService
import de.rogallab.mobile.domain.location.LocationValue
import de.rogallab.mobile.ui.base.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class LocationsViewModel(
   application: Application,
) : BaseViewModel(TAG), KoinComponent {

   private val _context: Context = application.applicationContext
   private val _locationManager: AppLocationManager by inject()

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
         Intent(_context, AppLocationService::class.java).apply {
            action = AppLocationService.Action.START.name
            _context.startService(this)
         }
      }
   }

   private fun stopLocationService() {
      viewModelScope.launch {
         Intent(_context, AppLocationService::class.java).apply {
            action = AppLocationService.Action.STOP.name
            _context.stopService(this)
         }
      }
   }

   companion object {
      const val TAG = "<-LocationsViewModel"
   }
}