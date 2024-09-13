package de.rogallab.mobile

import android.app.Application
import de.rogallab.mobile.domain.utilities.logInfo

class AppStart : Application() {

   override fun onCreate() {
      super.onCreate()
      val maxMemory = (Runtime.getRuntime().maxMemory() / 1024 ).toInt()
      logInfo(tag, "onCreate() maxMemory $maxMemory kB")
   }

   companion object {
      private const val tag = "[AppStart]"
      const val isDebug = true
      const val isInfo = true
   }
}