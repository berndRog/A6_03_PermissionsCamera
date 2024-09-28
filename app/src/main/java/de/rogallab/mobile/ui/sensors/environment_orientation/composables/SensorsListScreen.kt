package de.rogallab.mobile.ui.sensors.environment_orientation.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeGestures
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import de.rogallab.mobile.R
import de.rogallab.mobile.ui.navigation.AppBottomBar
import de.rogallab.mobile.ui.sensors.environment_orientation.EnvOriSensorsViewModel
import de.rogallab.mobile.ui.sensors.environment_orientation.EnvOriUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SensorsListScreen(
   viewModel: EnvOriSensorsViewModel,
   navController: NavController
) {

   // Environment & Orientation sensors state
   val envOriUiStateFlow: EnvOriUiState
      by viewModel.envOriUiStateFlow.collectAsStateWithLifecycle()

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
                  onClick = {
                     // Finish the app
                     //
                  }
               ) {
                  Icon(imageVector = Icons.Default.Menu,
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



      LazyColumn(
         modifier = Modifier
            .padding(paddingValues = paddingValues)
            .padding(horizontal = 16.dp)
      ) {
//         items(
//            items = mEnvOriUiStateFlow.ringBuffer.toList(),
//         ) { it: EnvOriUiState ->
//            Text(text = "Yaw: ${orientation.yaw}, Pitch: ${orientation.pitch}, Roll: ${orientation.roll}")
//         }
      }
   }
}

