package de.rogallab.mobile.ui

import android.app.Application
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.AndroidViewModel
import de.rogallab.mobile.domain.utilities.logDebug
import de.rogallab.mobile.domain.utilities.logInfo

data class LngLat(
   val longitude: Double = 0.0,
   val latitude: Double = 0.0
)

class MainViewModel(
   application: Application
) : AndroidViewModel(application) {

   val permissionQueue: SnapshotStateList<String> = mutableStateListOf()
   private val applicationContext = application.applicationContext

   fun addPermission(
      permission: String,
      isGranted: Boolean
   ) {
      logDebug(tag, "addPermission $permission $isGranted")
      if (isGranted || permissionQueue.contains(permission)) return
      permissionQueue.add(permission)
   }

   fun removePermission() {
      logDebug(tag, "removePermission ${permissionQueue.size}")
      permissionQueue.removeFirst()
   }

   companion object {
      private const val tag: String = "[MainViewModel]"
   }
}