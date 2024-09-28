package de.rogallab.mobile.ui.sensors.location.composables
import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Slider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import de.rogallab.mobile.domain.utilities.logInfo
import de.rogallab.mobile.ui.sensors.location.LocationsManager
import kotlinx.coroutines.flow.collectLatest

@Composable
fun GoogleMapScreen(
   context: Context,
   locationManager: LocationsManager
) {
   val mapView = rememberMapViewWithLifecycle()

   var currentLocation by remember { mutableStateOf(LatLng(0.0, 0.0)) }
   var zoom by remember { mutableStateOf(15f) }
   var sliderValue by remember { mutableStateOf(1f) }

   // Initialize the camera position state with the desired initial position and zoom level
   val cameraPositionState = rememberCameraPositionState {
      position = CameraPosition.fromLatLngZoom(currentLocation, zoom)
   }

   // Initialize the marker state with the initial position
   val markerState = rememberMarkerState(position = currentLocation)

   LaunchedEffect(Unit) {
      logInfo("<-GoogleMapsScreen", "collect locationFlow")

      locationManager.locationFlow.collectLatest { location ->
         location?.let {
            logInfo("<-GoogleMapsScreen", "location: ${it.latitude}, ${it.longitude}")
            currentLocation =
               LatLng(it.latitude, it.longitude)
            cameraPositionState.position =
               CameraPosition.fromLatLngZoom(currentLocation, cameraPositionState.position.zoom)
            markerState.position = currentLocation
         }
      }
   }

   LaunchedEffect(cameraPositionState.isMoving) {
      if (!cameraPositionState.isMoving) {
         zoom = cameraPositionState.position.zoom
      }
   }

   Column(modifier = Modifier.fillMaxSize()) {
      Slider(
         value = sliderValue,
         onValueChange = { sliderValue = it },
         valueRange = 1f..20f
      )
      GoogleMap(
         cameraPositionState = cameraPositionState,
      ) {
         if(sliderValue > 0.5f) {

            Marker(
               state = markerState,
               title = "Your Location"
            )
         }
      }

   }
}

