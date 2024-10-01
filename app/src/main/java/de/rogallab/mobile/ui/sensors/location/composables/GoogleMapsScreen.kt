package de.rogallab.mobile.ui.sensors.location.composables
import RememberMapViewWithLifecycle
import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
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
   val tag = "<-GoogleMapScreen"

   val mapView = RememberMapViewWithLifecycle()

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

      var checked by remember { mutableStateOf(true) }
      
      SwitchWithLabel(
         label ="Show Marker",
         checked = checked,
         onCheckedChange = {
            checked = it
         }
      )

      logInfo(tag, "markerState: ${markerState.position.latitude}, ${markerState.position.longitude}")
      GoogleMap(
         modifier = Modifier.fillMaxSize(),
         cameraPositionState = cameraPositionState,
      ) {
         if(checked) {
            Marker(
               state = markerState,
               title = "Your Location"
            )
         }
      }

   }
}

@Composable
fun SwitchWithLabel(
   label: String,
   checked: Boolean,
   onCheckedChange: (Boolean) -> Unit
) {

   val interactionSource = remember { MutableInteractionSource() }

   Row(
      modifier = Modifier
         .clickable(
            interactionSource = interactionSource,
            // This is for removing ripple when Row is clicked
            indication = null,
            role = Role.Switch,
            onClick = {
               onCheckedChange(!checked)
            }
         )
         .padding(8.dp),
      verticalAlignment = Alignment.CenterVertically

   ) {

      Text(
         modifier = Modifier.weight(0.9f),
         text = label)
      Switch(
         modifier = Modifier.weight(0.1f),
         checked = checked,
         onCheckedChange = {
            onCheckedChange(it)
         }
      )
   }
}

