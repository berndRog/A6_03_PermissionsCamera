package de.rogallab.mobile.ui.sensors.location

import android.app.Application
import android.content.Context
import android.location.Location
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import de.rogallab.mobile.domain.utilities.logInfo
import de.rogallab.mobile.ui.base.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LocationsViewModel(
   application: Application
) : BaseViewModel(TAG), LifecycleEventObserver {

   private val _context: Context = application.applicationContext
   val locationManager: LocationsManager = LocationsManager(_context)

   // Expose location updates to the UI
   private val _locationUiStateFlow: MutableStateFlow<LocationUiState> =
      MutableStateFlow(LocationUiState())
   val locationUiStateFlow: StateFlow<LocationUiState> =
      _locationUiStateFlow.asStateFlow()

   // Observe lifecycle events
   override fun onStateChanged(
      source: LifecycleOwner,
      event: Lifecycle.Event
   ) {
      logInfo(TAG, "onStateChanged: $event")
      when (event) {
         Lifecycle.Event.ON_START -> {
            // Start location updates when the lifecycle enters the started state
            locationManager.startLocationUpdates()
         }
         Lifecycle.Event.ON_STOP -> {
            // Stop location updates when the lifecycle stops
            locationManager.stopLocationUpdates()
         }
         Lifecycle.Event.ON_RESUME -> {
            // Fetch last known location, i.e update the locationflow
            locationManager.getLastLocation()
            // consume the locationFlow
            source.lifecycleScope.launch {
               locationManager.locationFlow.collect { location: Location? ->
                  location?.let { loc ->
                     logInfo(TAG, "location: ${loc.latitude}, ${loc.longitude}")
                     _locationUiStateFlow.update {  it: LocationUiState ->
                        // new location value object
                        val locationValue = LocationValue(
                           epochMillis = location.time,
                           latitude = location.latitude,
                           longitude = location.longitude,
                           altitude = location.altitude,
                           speed = location.speed,
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
               }
            }
         }
         else -> { }
      }
   }

   override fun onCleared() {
      super.onCleared()
      locationManager.stopLocationUpdates()
   }

   companion object {
      const val TAG = "<-LocationsViewModel"
   }
}