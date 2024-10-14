package de.rogallab.mobile

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import de.rogallab.mobile.data.di.dataModules
import de.rogallab.mobile.domain.di.domainModules
import de.rogallab.mobile.domain.utilities.logInfo
import de.rogallab.mobile.ui.di.uiModules
import de.rogallab.mobile.domain.location.AppLocationService
import de.rogallab.mobile.domain.sensors.AppSensorService
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androix.startup.KoinStartup.onKoinStartup
import org.koin.core.logger.Level


@Suppress("OPT_IN_USAGE")
class AppStart : Application(), DefaultLifecycleObserver {

   init{
      logInfo(TAG, "init: onKoinStartUp{...}")
      onKoinStartup {
         // Log Koin into Android logger
         androidLogger(Level.DEBUG)
         // Reference Android context
         androidContext(this@AppStart)
         // Load modules
         modules(domainModules, dataModules, uiModules)
      }
   }


   override fun onCreate() {

      super<Application>.onCreate()

      val maxMemory = (Runtime.getRuntime().maxMemory() / 1024 ).toInt()
      logInfo(TAG, "onCreate() maxMemory $maxMemory kB")

      // Register the lifecycle observer
      ProcessLifecycleOwner.get().lifecycle.addObserver(this)

      if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

         val notificationChannel = NotificationChannel(
            AppLocationService.LOCATION_CHANNEL,
            "Location",
            NotificationManager.IMPORTANCE_LOW
         )

         val orientationChannel = NotificationChannel(
            AppSensorService.ORIENTATION_CHANNEL,
            "Orientation",
            NotificationManager.IMPORTANCE_LOW
         )
         val notificationManager:NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

         notificationManager.createNotificationChannel(notificationChannel)
         notificationManager.createNotificationChannel(orientationChannel)

         val x = 3

      }
   }


   // stop the location foreground service when the app is closed
   override fun onStop(owner: LifecycleOwner) {
      logInfo(TAG, "onStop()")
      val stopIntent = Intent(this, AppLocationService::class.java)
      stopService(stopIntent)
      //stopAppLocationService()
   }

   companion object {
      private const val TAG = "<-AppStart"
      const val ISDEBUG = true
      const val ISINFO = true
      const val ISVERBOSE = true
   }
}