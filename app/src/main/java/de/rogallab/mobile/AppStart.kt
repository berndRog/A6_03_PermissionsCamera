package de.rogallab.mobile

import android.app.Application
import de.rogallab.mobile.domain.utilities.logInfo

class AppStart : Application() {

   override fun onCreate() {
      super.onCreate()
      val maxMemory = (Runtime.getRuntime().maxMemory() / 1024 ).toInt()
      logInfo(TAG, "onCreate() maxMemory $maxMemory kB")
   }

   companion object {
      private const val TAG = "<-AppStart"
      const val ISDEBUG = true
      const val ISINFO = true
      const val ISVERBOSE = true
   }
}