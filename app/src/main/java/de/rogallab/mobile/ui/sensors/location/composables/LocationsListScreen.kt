package de.rogallab.mobile.ui.sensors.location.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeGestures
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import de.rogallab.mobile.R
import de.rogallab.mobile.domain.utilities.formatf52
import de.rogallab.mobile.domain.utilities.toDateString
import de.rogallab.mobile.domain.utilities.toDateTimeString
import de.rogallab.mobile.domain.utilities.toLocalDateTime
import de.rogallab.mobile.domain.utilities.toTimeString
import de.rogallab.mobile.ui.navigation.AppBottomBar
import de.rogallab.mobile.ui.navigation.NavEvent
import de.rogallab.mobile.ui.sensors.location.LocationUiState
import de.rogallab.mobile.ui.sensors.location.LocationsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationsListScreen(
   viewModel: LocationsViewModel,
   navController: NavController
) {

   val context = LocalContext.current

   // Yaw, Pitch, Roll +
   val locationUiState: LocationUiState
      by viewModel.locationUiStateFlow.collectAsStateWithLifecycle()

   val snackbarHostState = remember { SnackbarHostState() }

   val windowInsets = WindowInsets.systemBars
      .add(WindowInsets.navigationBars)
      .add(WindowInsets.ime)
      .add(WindowInsets.safeGestures)

   Scaffold(
      modifier = Modifier
         .fillMaxSize()
         .padding(windowInsets.asPaddingValues())
         .background(color = MaterialTheme.colorScheme.surface),
      topBar = {
         TopAppBar(
            title = { Text(text = stringResource(R.string.locations_list)) },
            navigationIcon = {
               IconButton(
                  onClick = { viewModel.navigateTo(NavEvent.Home) }
               ) {
                  Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                     contentDescription = stringResource(R.string.back))
               }
            }
         )
      },
      bottomBar = {
         AppBottomBar(navController = navController)
      },
      snackbarHost = {
         SnackbarHost(hostState = snackbarHostState) { data ->
            Snackbar(snackbarData = data, actionOnNewLine = true)
         }
      }
   ) { paddingValues: PaddingValues ->

      Column(modifier = Modifier
         .padding(paddingValues = paddingValues)
         .padding(horizontal = 16.dp)
      ) {

//        locationUiState.last?.let { it: LocationValue ->
//           val dt = toLocalDateTime(it.timestamp).toTimeString()
//           val latitude = formatf52(Math.toDegrees(it.latitude))
//           val longitude = formatf52(Math.toDegrees(it.longitude))
//           val altitude = formatf52(it.altitude)
//           Text(
//               text = "Last Location t:$dt, L:$latitude, B:$longitude, H:$altitude",
//               style = MaterialTheme.typography.bodyLarge
//            )
//         }

         val locationValue = locationUiState.last

         val ld = toLocalDateTime(locationValue.epochMillis).toDateString()
         val lt = toLocalDateTime(locationValue.epochMillis).toTimeString()
         val ldt = toLocalDateTime(locationValue.epochMillis).toDateTimeString()
         val latitude = formatf52(locationValue.latitude)
         val longitude = formatf52(locationValue.longitude)
         val altitude = formatf52(locationValue.altitude)
         Text(
            text = "$ld, $lt, $ldt",
            style = MaterialTheme.typography.bodyMedium
         )
         Text(
            text = "L/B:$latitude, $longitude, H:$altitude",
            style = MaterialTheme.typography.bodyLarge
         )

         GoogleMapScreen(context, viewModel.locationManager)


//         LazyColumn {
//            items(
//               items = locationUiState.ringBuffer.toList()
//            ) { it: LocationValue ->
//               val ld = toLocalDateTime(it.epochMillis).toDateString()
//               val lt = toLocalDateTime(it.epochMillis).toTimeString()
//               val ldt = toLocalDateTime(it.epochMillis).toDateTimeString()
//               val latitude = formatf52(it.latitude)
//               val longitude = formatf52(it.longitude)
//               val altitude = formatf52(it.altitude)
//               Text(
//                  text = "$ld, $lt, $ldt",
//                  style = MaterialTheme.typography.bodyMedium
//               )
//               Text(
//                  text = "L/B:$latitude, $longitude, H:$altitude",
//                  style = MaterialTheme.typography.bodyLarge
//               )
//
//
//
//            }
//         }
      }
   }
}