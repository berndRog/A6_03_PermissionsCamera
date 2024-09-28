package de.rogallab.mobile.ui.sensors.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import de.rogallab.mobile.domain.utilities.logError
import de.rogallab.mobile.domain.utilities.logInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class LocationsManager(private val context: Context) {

   // FusedLocationProviderClient to access location APIs
   private val fusedLocationClient: FusedLocationProviderClient =
      LocationServices.getFusedLocationProviderClient(context)

   // required permissions for location updates
   private val requiredPermissions = arrayOf(
      android.Manifest.permission.ACCESS_FINE_LOCATION,
      android.Manifest.permission.ACCESS_COARSE_LOCATION
   )

   // Exposing location updates through StateFlow
   private val _locationFlow = MutableStateFlow<Location?>(null)
   val locationFlow: StateFlow<Location?> = _locationFlow

   // LocationRequest parameters
   private val locationRequest: LocationRequest = LocationRequest.Builder(
      Priority.PRIORITY_HIGH_ACCURACY,
      5000L // Update interval in milliseconds
   ).build()


   // Start requesting location updates for tracking
   fun startLocationUpdates() {
      logInfo(TAG, "startLocationUpdates()")
      if (requiredPermissions.all { permission ->
            ContextCompat.checkSelfPermission(context, permission
            ) == PackageManager.PERMISSION_GRANTED
         } &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
      ) {
         logInfo(TAG, "Permission granted: requestLocationUpdates")
         fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
         )
      } else {
         // Handle permissions not granted
         logError(TAG, "Permissions not granted to startLocationUpdates")
      }
   }
   // Callback for location tracking
   private val locationCallback = object : LocationCallback() {
      override fun onLocationResult(locationResult: LocationResult) {
         logInfo(TAG, "onLocationResult: ${locationResult.lastLocation}")
         _locationFlow.value = locationResult.lastLocation
      }
   }

   // Stop requesting location updates
   fun stopLocationUpdates() {
      logInfo(TAG, "stopLocationUpdates()")
      fusedLocationClient.removeLocationUpdates(locationCallback)
   }

   // Fetch the last known location
   // @SuppressLint("MissingPermission")
   fun getLastLocation() {
      logInfo(TAG, "getLastLocation()")
      if (requiredPermissions.all { permission ->
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
         }
      ) {
         logInfo(TAG, "Permission granted: getLastLocation()")
         fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            _locationFlow.value = location
            logInfo(TAG, "lastLocation.addOnSuccessListener: $location")
         }
      } else {
         // Handle permissions not granted
      }
   }

   companion object {
      const val TAG = "<-LocationsManager"
   }
}