package de.rogallab.mobile.ui.permissions

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import de.rogallab.mobile.R
import de.rogallab.mobile.ui.MainViewModel
import de.rogallab.mobile.domain.utilities.logDebug
import de.rogallab.mobile.ui.openAppSettings

@Composable
fun RequestPermissions(
   permissionsToRequest: Array<String>,
   mainViewModel: MainViewModel
) {
   val tag = "[RequestPermissions]"

   val activity = (LocalContext.current as? Activity)
   val context: Context =
      activity?.applicationContext ?:
      throw Exception("context is null in RequestPermisssions")

   // Setup multiple permission request launcher
   val multiplePermissionRequestLauncher = rememberLauncherForActivityResult(
      contract = ActivityResultContracts.RequestMultiplePermissions(),
      // callback: handle permissions results, i.e store them into the MainViewModel
      onResult = { permissionMap: Map<String, @JvmSuppressWildcards Boolean> ->
         logDebug(tag, "PermissionRequestLauncher onResult: ")
         permissionsToRequest.forEach { permission ->
            logDebug(tag,"$permission isGranted ${permissionMap[permission]}")
            mainViewModel.addPermission(
               permission = permission,
               isGranted = permissionMap[permission] == true  // key = true
            )
         }
      }
   )

   if (!ArePermissionsAlreadyGranted(permissionsToRequest, tag)) {
      // Request permissions, i.e. launch dialog
      LaunchedEffect(true) {
         val cameraPermission = Manifest.permission.CAMERA
         logDebug(tag, "PermissionRequestLauncher launched")
         multiplePermissionRequestLauncher.launch(
            permissionsToRequest
         )
      }
   }

   // if a requested permission is not granted -> ask again or goto appsettings
   mainViewModel.permissionQueue
      .reversed()
      .forEach { permission ->
         logDebug(tag, "permissionQueue $permission")

         var dialogOpen by remember {  mutableStateOf(false) }

         val isPermanentlyDeclined =
            activity!!.shouldShowRequestPermissionRationale(permission)
         logDebug(tag, "permissionsQueue isPermanentlyDeclined $isPermanentlyDeclined")

         // get the text for requested permission
         val permissionText: IPermissionText? = when (permission) {
            Manifest.permission.CAMERA -> PermissionCamera()
            Manifest.permission.RECORD_AUDIO -> PermissionRecordAudio()
            //Manifest.permission.ACCESS_COARSE_LOCATION -> PermissionCoarseLocation()
            //Manifest.permission.ACCESS_FINE_LOCATION -> PermissionFineLocation()
            else -> null
         }
         logDebug(tag, "permissionsQueue " +
            "${permissionText?.getDescription(context, isPermanentlyDeclined)}")

         AlertDialog(
            modifier = Modifier,
            onDismissRequest = {
               logDebug(tag, "AlertDialog OnDismiss()")
               dialogOpen = false
            },
            // permission is granted, perform the confirm actions
            confirmButton = {
               TextButton(
                  onClick = {
                     logDebug(tag, " AlertDialog confirmButton() $permission")
                     // remove granted permission from the permissionQueue
                     mainViewModel.removePermission()
                     // launch the dialog again if further permissions are required
                     multiplePermissionRequestLauncher.launch(arrayOf(permission))
                     // close the dialog
                     dialogOpen = false
                  }
               ) {
                  Text(text = stringResource(R.string.agree))
               }
            },
            // permission is declined, perform the decline actions
            dismissButton = {
               TextButton(
                  onClick = {
                     logDebug(tag, "AlertDialog dismissButton() $permission")
                     if (! isPermanentlyDeclined) {
                        // remove permanently declined permissions from the permissionQueue
                        mainViewModel.removePermission()
                        // launch the dialog again if further permissions are required
                        multiplePermissionRequestLauncher.launch(arrayOf(permission))
                     } else {
                        logDebug(tag, "openAppSettings() $permission and exit the app")
                        // as a last resort, go to the app settings and close the app
                        activity?.openAppSettings()
                        activity?.finish()
                     }
                     // close the dialog
                     dialogOpen = false
                  }
               ) {
                  Text(text = stringResource(R.string.refuse))
               }
            },
            icon = {},
            title = {
               Text(text = stringResource(R.string.permissionRequired))
            },
            text = {
               Text(
                  text = permissionText?.getDescription(context,
                     isPermanentlyDeclined = isPermanentlyDeclined
                  ) ?: ""
               )
            }
         )
      }
}


@Composable
fun ArePermissionsAlreadyGranted(
   permissionsToRequest: Array<String>,
   tag: String
): Boolean {
   permissionsToRequest.forEach { permissionToRequest ->
      if (ContextCompat.checkSelfPermission(LocalContext.current,permissionToRequest)
         == PackageManager.PERMISSION_GRANTED) {
         logDebug(tag, "requestPermission() $permissionToRequest already granted")
      } else {
         // permission must be requested
         return false
      }
   }
   // all permission are already granted
   return true
}