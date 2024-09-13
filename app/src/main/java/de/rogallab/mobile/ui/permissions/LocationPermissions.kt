package de.rogallab.mobile.ui.permissions

import androidx.compose.runtime.Composable

//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.Row
//import androidx.compose.foundation.layout.Spacer
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.outlined.CheckCircle
//import androidx.compose.material3.AlertDialog
//import androidx.compose.material3.Button
//import androidx.compose.material3.Icon
//import androidx.compose.material3.Text
//import androidx.compose.material3.TextButton
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
//import androidx.compose.runtime.setValue
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.unit.dp
//import java.lang.reflect.Modifier
//
///**
// * Simple screen that manages the location permission state
// */
//@OptIn(ExperimentalPermissionsApi::class)

@Composable
fun Lala() {

}

//fun LocationPermissions(text: String, rationale: String, locationState: PermissionState) {
//   LocationPermissions(
//      text = text,
//      rationale = rationale,
//      locationState = rememberMultiplePermissionsState(
//         permissions = listOf(
//            locationState.permission
//         )
//      )
//   )
//}
//
///**
// * Simple screen that manages the location permission state
// */
//@OptIn(ExperimentalPermissionsApi::class)
//@Composable
//fun LocationPermissions(text: String, rationale: String, locationState: MultiplePermissionsState) {
//   var showRationale by remember(locationState) {
//      mutableStateOf(false)
//   }
//   if (showRationale) {
//      PermissionRationaleDialog(rationaleState = RationaleState(
//         title = "Location Permission Access",
//         rationale = rationale,
//         onRationaleReply = { proceed ->
//            if (proceed) {
//               locationState.launchMultiplePermissionRequest()
//            }
//            showRationale = false
//         }
//      ))
//   }
//   Box(
//      modifier = Modifier.fillMaxSize(),
//      contentAlignment = Alignment.Center
//   ) {
//      PermissionRequestButton(isGranted = false, title = text) {
//         if (locationState.shouldShowRationale) {
//            showRationale = true
//         } else {
//            locationState.launchMultiplePermissionRequest()
//         }
//      }
//   }
//}
//
///**
// * A button that shows the title or the request permission action.
// */
//@Composable
//fun PermissionRequestButton(isGranted: Boolean, title: String, onClick: () -> Unit) {
//   if (isGranted) {
//      Row(
//         modifier = Modifier
//            .fillMaxWidth()
//            .padding(16.dp),
//         verticalAlignment = Alignment.CenterVertically
//      ) {
//         Icon(Icons.Outlined.CheckCircle, title, modifier = Modifier.size(48.dp))
//         Spacer(Modifier.size(10.dp))
//         Text(text = title, modifier = Modifier.background(Color.Transparent))
//      }
//   } else {
//      Button(onClick = onClick) {
//         Text("Request $title")
//      }
//   }
//}
//
///**
// * Simple AlertDialog that displays the given rational state
// */
//@Composable
//fun PermissionRationaleDialog(rationaleState: RationaleState) {
//   AlertDialog(
//      onDismissRequest = { rationaleState.onRationaleReply(false) },
//      title = {
//         Text(text = rationaleState.title)
//      }, text = {
//         Text(text = rationaleState.rationale)
//      },
//      confirmButton = {
//         TextButton(onClick = {
//            rationaleState.onRationaleReply(true)
//         }) {
//            Text("Continue")
//         }
//      },
//      dismissButton = {
//         TextButton(onClick = {
//            rationaleState.onRationaleReply(false)
//         }) {
//            Text("Dismiss")
//         }
//      })
//}
//
//data class RationaleState(
//   val title: String,
//   val rationale: String,
//   val onRationaleReply: (proceed: Boolean) -> Unit,
//)