import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.android.gms.maps.MapView

@Composable
fun RememberMapViewWithLifecycle(): MapView {
   val context = LocalContext.current
   val mapView = remember { MapView(context) }

   val lifecycle = LocalLifecycleOwner.current.lifecycle
   DisposableEffect(lifecycle) {
      val observer = object : DefaultLifecycleObserver {
         override fun onCreate(owner: LifecycleOwner) {
            mapView.onCreate(null)
         }

         override fun onStart(owner: LifecycleOwner) {
            mapView.onStart()
         }

         override fun onResume(owner: LifecycleOwner) {
            mapView.onResume()
         }

         override fun onPause(owner: LifecycleOwner) {
            mapView.onPause()
         }

         override fun onStop(owner: LifecycleOwner) {
            mapView.onStop()
         }

         override fun onDestroy(owner: LifecycleOwner) {
            mapView.onDestroy()
         }
      }

      lifecycle.addObserver(observer)
      onDispose {
         lifecycle.removeObserver(observer)
      }
   }

   return mapView
}