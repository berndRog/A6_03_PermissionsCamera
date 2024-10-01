package de.rogallab.mobile.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import de.rogallab.mobile.domain.utilities.logError
import de.rogallab.mobile.domain.utilities.logInfo
import de.rogallab.mobile.ui.base.BaseActivity
import de.rogallab.mobile.ui.base.LocationsViewModelFactory
import de.rogallab.mobile.ui.base.PeopleViewModelFactory
import de.rogallab.mobile.ui.base.SensorsViewModelFactory
import de.rogallab.mobile.ui.base.SettingsViewModelFactory
import de.rogallab.mobile.ui.home.HomeScreen
import de.rogallab.mobile.ui.navigation.NavigationViewModel
import de.rogallab.mobile.ui.navigation.composables.AppDrawer
import de.rogallab.mobile.ui.navigation.composables.AppNavHost
import de.rogallab.mobile.ui.people.PeopleViewModel
import de.rogallab.mobile.ui.permissions.RequestPermissions
import de.rogallab.mobile.ui.sensors.environment_orientation.SensorsViewModel
import de.rogallab.mobile.ui.sensors.location.LocationsViewModel
import de.rogallab.mobile.ui.settings.SettingsViewModel
import de.rogallab.mobile.ui.theme.AppTheme
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.launch

class MainActivity : BaseActivity(TAG) {

   private val _peopleViewModel: PeopleViewModel by viewModels {
      PeopleViewModelFactory(application)
   }
   private val _locationsViewModel: LocationsViewModel by viewModels {
      LocationsViewModelFactory(application)
   }
   private val _sensorsViewModel: SensorsViewModel by viewModels {
      SensorsViewModelFactory(application)
   }
   private val _settingsViewModel: SettingsViewModel by viewModels {
      SettingsViewModelFactory(application)
   }

   private val _navigationsViewModel: NavigationViewModel by viewModels()

   override fun onCreate(savedInstanceState: Bundle?) {
      super.onCreate(savedInstanceState)

      // environment & orientation sensors don't need permissions to request
      lifecycle.addObserver(_sensorsViewModel)

      setContent {

         AppTheme {
            Surface( modifier = Modifier.fillMaxSize() ) {
               val drawerState = rememberDrawerState(DrawerValue.Closed)
               val scope = rememberCoroutineScope()

               ModalNavigationDrawer(
                  drawerState = drawerState,
                  drawerContent = {
                     AppDrawer(drawerState, _navigationsViewModel, scope)
                  }
               ) {

                  // Request permissions, wait for the permissions result
                  val permissionsDeferred: CompletableDeferred<Boolean> =
                     remember { CompletableDeferred<Boolean>() }
                  RequestPermissions(permissionsDeferred)

                  // Wait for the permissions result, then continue
                  var permissionsGranted: Boolean
                     by remember { mutableStateOf<Boolean>(false) }
                  // Show the home screen if permissions are not granted
                  if (!permissionsGranted) HomeScreen()

                  LaunchedEffect(Unit) {
                     // Wait for the permissions result
                     permissionsGranted = permissionsDeferred.await()
                     if (permissionsGranted) {
                        logInfo(TAG, "Permissions are granted")
                        startLifecycleForLocationSensor()
                     } else {
                        logError(TAG, "Permissions not granted")
                     }
                  }

                  // Show the app content if permissions are granted
                  if (permissionsGranted) {
                     AppNavHost(
                        peopleViewModel = _peopleViewModel,
                        locationsViewModel = _locationsViewModel,
                        sensorsViewModel = _sensorsViewModel,
                        settingsViewModel = _settingsViewModel,
                        navigationViewModel = _navigationsViewModel,
                        drawerState = drawerState
                     )
                  } else if (permissionsGranted == false) {
                     logError(TAG, "Permissions not granted")
                  }
               } // ModalNavigationDrawer
            } // Surface
         } // AppTheme
      } // setContent
   } // onCreate

   private fun startLifecycleForLocationSensor() {
      // Add lifecycle observers to the viewmodels
      lifecycle.addObserver(_locationsViewModel)
      // Manually trigger lifecycle events, to start
      lifecycleScope.launch {
         lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            _locationsViewModel.onStateChanged(this@MainActivity, Lifecycle.Event.ON_START)
         }
         lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            _locationsViewModel.onStateChanged(this@MainActivity, Lifecycle.Event.ON_RESUME)
         }
      }
   }

   companion object {
      private const val TAG = "<-MainActivity"
   }
}

// static extension function for Activity
fun Activity.openAppSettings() {
   Intent(
      Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
      Uri.fromParts("package", packageName, null)
   ).also(::startActivity)
}