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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import de.rogallab.mobile.R
import de.rogallab.mobile.domain.utilities.logDebug
import de.rogallab.mobile.domain.utilities.logInfo
import de.rogallab.mobile.ui.openAppSettings
import kotlinx.coroutines.CompletableDeferred


@Composable
fun RequestPermissions(
   permissionsDeferred: CompletableDeferred<Boolean>
) {
   val tag = "<-RequestPermissions"
   val context = LocalContext.current

   // Local state for permission queue
   val permissionQueue = remember { mutableStateListOf<String>() }

   // Get permissions from the manifest
   val permissionsFromManifest: Array<String> = getPermissionsFromManifest(context)
//   permissionsFromManifest.forEach { permission ->
//      logInfo(tag, "Permissions from manifest: $permission")
//   }

   // Filter permissions that are not granted yet
   val permissionsToRequest = permissionsFromManifest.filter { permission ->
      ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED
   }.toTypedArray()
   permissionsToRequest.forEach { permission ->
      logInfo(tag, "Permissions to request: $permission")
   }

   // Setup multiple permission request launcher
   val multiplePermissionRequestLauncher = rememberLauncherForActivityResult(
      // RequestMultiplePermissions() is a built-in ActivityResultContract
      contract = ActivityResultContracts.RequestMultiplePermissions(),
      // Callback for the result of the permission request
      // the result is a Map<String, Boolean> where the key is the permission and
      // the value is the result, i.e. is the permission granted or not
      onResult = { permissionMap: Map<String, @JvmSuppressWildcards Boolean> ->
         permissionMap.forEach { (permission, isGranted) ->
            logDebug(tag, "$permission = $isGranted")
            if (!isGranted && !permissionQueue.contains(permission)) {
               permissionQueue.add(permission)
            }
         }
         // Complete the deferred with the result
         permissionsDeferred.complete(permissionMap.all { it.value })
      }
   )

   // Request permissions if not already granted
   if (permissionsToRequest.isNotEmpty()) {
      LaunchedEffect(true) {
         multiplePermissionRequestLauncher.launch(permissionsToRequest)
      }
   } else {
      // All permissions are already granted
      LaunchedEffect(true) {
         permissionsDeferred.complete(true)
      }
   }

   // Handle permission rationale and app settings
   permissionQueue.reversed().forEach { permission ->
      logDebug(tag, "permissionQueue $permission")

      var dialogOpen by remember { mutableStateOf(true) }
      val isPermanentlyDeclined = (context as Activity).shouldShowRequestPermissionRationale(permission)
      val permissionText = getPermissionText(permission)

      if (dialogOpen) {
         AlertDialog(
            modifier = Modifier,
            onDismissRequest = {
               logDebug(tag, "AlertDialog-onDismissRequest")
               dialogOpen = false
            },
            confirmButton = {
               TextButton(
                  onClick = {
                     logDebug(tag, "AlertDialog-confirmButton() $permission")
                     permissionQueue.remove(permission)
                     multiplePermissionRequestLauncher.launch(arrayOf(permission))
                     dialogOpen = false
                  }
               ) {
                  Text(text = stringResource(R.string.agree))
               }
            },
            dismissButton = {
               TextButton(
                  onClick = {
                     logDebug(tag, "AlertDialog dismissButton() $permission")
                     if (!isPermanentlyDeclined) {
                        permissionQueue.remove(permission)
                        multiplePermissionRequestLauncher.launch(arrayOf(permission))
                     } else {
                        logDebug(tag, "openAppSettings() $permission and exit the app")
                        context.openAppSettings()
                        context.finish()
                     }
                     dialogOpen = false
                  }
               ) {
                  Text(text = stringResource(R.string.refuse))
               }
            },
            icon = {},
            title = { Text(text = stringResource(R.string.permissionRequired)) },
            text = {
               Text(text = permissionText?.getDescription(context, isPermanentlyDeclined = isPermanentlyDeclined) ?: "")
            }
         )
      }
   }
}

@Composable
fun ArePermissionsGranted(permissions: Array<String>): Boolean {
   val context = LocalContext.current
   return permissions.all { permission ->
      ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
   }
}

private fun getPermissionsFromManifest(context: Context): Array<String> {
   val packageInfo = context.packageManager
      .getPackageInfo(context.packageName, PackageManager.GET_PERMISSIONS)
   return packageInfo.requestedPermissions ?: emptyArray()
}


private fun getPermissionText(permission: String): IPermissionText? {
   return when (permission) {
      Manifest.permission.CAMERA -> PermissionCamera()
      Manifest.permission.RECORD_AUDIO -> PermissionRecordAudio()
      Manifest.permission.ACCESS_COARSE_LOCATION -> PermissionCoarseLocation()
      Manifest.permission.ACCESS_FINE_LOCATION -> PermissionFineLocation()
      else -> null
   }
}