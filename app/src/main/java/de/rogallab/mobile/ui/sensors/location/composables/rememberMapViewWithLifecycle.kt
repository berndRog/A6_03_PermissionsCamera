package de.rogallab.mobile.ui.sensors.location.composables

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.android.gms.maps.MapView

@Composable
fun rememberMapViewWithLifecycle(): MapView {
   val context = LocalContext.current
   val mapView = remember { MapView(context) }

   val lifecycleObserver = rememberUpdatedState(object : DefaultLifecycleObserver {
      override fun onResume(owner: LifecycleOwner) {
         mapView.onResume()
      }

      override fun onPause(owner: LifecycleOwner) {
         mapView.onPause()
      }

      override fun onDestroy(owner: LifecycleOwner) {
         mapView.onDestroy()
      }
   })

   val lifecycle = LocalLifecycleOwner.current.lifecycle

   DisposableEffect(lifecycle) {
      lifecycle.addObserver(lifecycleObserver.value)
      onDispose {
         lifecycle.removeObserver(lifecycleObserver.value)
      }
   }

   return mapView
}
