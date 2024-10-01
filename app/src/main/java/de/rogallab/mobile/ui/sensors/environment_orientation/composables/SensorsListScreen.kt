package de.rogallab.mobile.ui.sensors.environment_orientation.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeGestures
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import de.rogallab.mobile.R
import de.rogallab.mobile.domain.utilities.toLocalDateTime
import de.rogallab.mobile.domain.utilities.toTimeString
import de.rogallab.mobile.ui.navigation.composables.AppBottomBar
import de.rogallab.mobile.ui.navigation.NavEvent
import de.rogallab.mobile.ui.sensors.environment_orientation.SensorsUiState
import de.rogallab.mobile.ui.sensors.environment_orientation.SensorsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SensorsListScreen(
   viewModel: SensorsViewModel,
   navController: NavController
) {

   // Environment & Orientation sensors state
   val sensorsUiState: SensorsUiState
      by viewModel.sensorsUiStateFlow.collectAsStateWithLifecycle()

   val snackbarHostState = remember { SnackbarHostState() }

   val windowInsets = WindowInsets.systemBars
      .add(WindowInsets.safeGestures)

   Scaffold(
      modifier = Modifier
         .fillMaxSize()
         .padding(windowInsets.asPaddingValues())
         .background(color = MaterialTheme.colorScheme.surface),
      topBar = {
         TopAppBar(
            title = { Text(text = stringResource(R.string.sensors_list)) },
            navigationIcon = {
               IconButton(
                  onClick = { viewModel.navigateTo(NavEvent.NavigateHome) }
               ) {
                  Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                     contentDescription = stringResource(R.string.back))
               }
            }
         )
      },
      bottomBar = {
         AppBottomBar(navController, viewModel)
      },
      snackbarHost = {
         SnackbarHost(hostState = snackbarHostState) { data ->
            Snackbar(snackbarData = data, actionOnNewLine = true)
         }
      }
   ) { paddingValues: PaddingValues ->

      LazyColumn(
         modifier = Modifier
            .padding(paddingValues = paddingValues)
            .padding(end = 16.dp),
      ) {
         val sensorValues = sensorsUiState.ringBuffer
            .toList()
            .sortedByDescending { it.epochMillis }

         item {
            Row(modifier = Modifier.fillMaxWidth()) {
               Text(modifier = Modifier.weight(0.25f),
                  text = "Time",
                  textAlign = TextAlign.End)
               Text(modifier = Modifier.weight(0.2f),
                  text = "Yaw",
                  textAlign = TextAlign.End)
               Text(modifier = Modifier.weight(0.2f),
                  text = "Pitch",
                  textAlign = TextAlign.End)
               Text(modifier = Modifier.weight(0.2f),
                  text = "Roll",
                  textAlign = TextAlign.End)
            }
         }

         items(
            count = sensorValues.size,
         ) { it: Int ->  // index

            Row(modifier = Modifier.fillMaxWidth()) {
               Text(modifier = Modifier.weight(0.25f),
                  text = toLocalDateTime(sensorValues[it].epochMillis).toTimeString(),
                  textAlign = TextAlign.End)
               Text(modifier = Modifier.weight(0.2f),
                  text = String.format("%.1f", sensorValues[it].yaw),
                  textAlign = TextAlign.End)
               Text(modifier = Modifier.weight(0.2f),
                  text = String.format("%.1f", sensorValues[it].pitch),
                  textAlign = TextAlign.End)
               Text(modifier = Modifier.weight(0.2f),
                  text = String.format("%.1f", sensorValues[it].roll),
                  textAlign = TextAlign.End)
            }
         }
      }
   }
}

