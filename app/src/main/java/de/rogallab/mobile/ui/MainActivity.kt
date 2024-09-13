package de.rogallab.mobile.ui

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import de.rogallab.mobile.ui.base.BaseActivity
import de.rogallab.mobile.ui.navigation.AppNavHost
import de.rogallab.mobile.ui.people.PeopleViewModel
import de.rogallab.mobile.ui.permissions.RequestPermissions
import de.rogallab.mobile.ui.theme.AppTheme

class MainActivity : BaseActivity(TAG) {


   private val _mainViewModel by viewModels<MainViewModel>()
   private val _peopleViewModel by viewModels<PeopleViewModel>()

   // required permissions
   private val permissionsToRequest = arrayOf(
      Manifest.permission.CAMERA,
//    Manifest.permission.RECORD_AUDIO,
      Manifest.permission.ACCESS_NETWORK_STATE,
      Manifest.permission.ACCESS_WIFI_STATE,
//    Manifest.permission.ACCESS_COARSE_LOCATION,
//    Manifest.permission.ACCESS_FINE_LOCATION,
//    Manifest.permission.ACCESS_BACKGROUND_LOCATION
   )

   override fun onCreate(savedInstanceState: Bundle?) {
      super.onCreate(savedInstanceState)

      setContent {
         AppTheme {
            Surface(
               modifier = Modifier.fillMaxSize()
            ) {
               RequestPermissions(permissionsToRequest, _mainViewModel)
               AppNavHost()
            }
         }
      }
   }

   companion object {
      const val isInfo = true
      const val isDebug = true
      const val isVerbose = true
      private const val TAG = "[MainActivity]"
   }
}

// static extension function for Activity
fun Activity.openAppSettings() {
   Intent(
      Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
      Uri.fromParts("package", packageName, null)
   ).also(::startActivity)
}


@Preview(showBackground = true)
@Composable
fun Preview() {
   AppTheme {
      AppNavHost()
   }
}