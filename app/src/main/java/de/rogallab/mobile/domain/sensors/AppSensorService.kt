package de.rogallab.mobile.domain.sensors

import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import de.rogallab.mobile.R
import de.rogallab.mobile.domain.location.AppLocationService.Action
import de.rogallab.mobile.domain.utilities.logDebug
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class AppSensorService() : Service() {

   private val _sensorManager: AppSensorManager by inject()

   private val scope = CoroutineScope(
      SupervisorJob() + Dispatchers.IO
   )

   override fun onBind(intent: Intent?): IBinder? {
      return null // We're not binding the service
   }

   override fun onStartCommand(
      intent: Intent?, flags: Int, startId: Int
   ): Int {
      when (intent?.action) {
         Action.START.name -> startForegroundService()
         Action.STOP.name -> stopForegroundService()
      }
      return super.onStartCommand(intent, flags, startId)
   }

   private fun startForegroundService() {
      logDebug(TAG,"startForegroundService")

      val notificationManager =
         getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

      val notification = NotificationCompat
         .Builder(this, ORIENTATION_CHANNEL)
         .setSmallIcon(R.drawable.ic_launcher_foreground)
         .setContentTitle("Orientation Tracker")
         .setStyle(NotificationCompat.BigTextStyle())

      startForeground(2, notification.build())

      scope.launch {
         _sensorManager.sensorValuesFlow().collect { sensorValues ->

            // send orientation as broadcast
            broadcastOrientation(sensorValues)

            // send location as notification
            notificationManager.notify(
               2,
               notification.setContentText("Orientation updated")
                  .build()
            )
         }
      }
   }

   private fun stopForegroundService() {
      logDebug(TAG,"stopForegroundService")
      stopForeground(STOP_FOREGROUND_REMOVE)
      stopSelf()
   }

   override fun onDestroy() {
      super.onDestroy()
      scope.cancel()
   }

   private fun broadcastOrientation(sensorValues: SensorValues) {

      //logDebug(TAG,"sendBroadcast with orientation values ${formatEpochLatLng(location)}")
      val intent = Intent("LOCATION_UPDATE")
      intent.putExtra("time", sensorValues.time)
      intent.putExtra("yaw", sensorValues.yaw)
      intent.putExtra("pitch", sensorValues.pitch)
      intent.putExtra("roll", sensorValues.roll)
      sendBroadcast(intent)
   }



   companion object {
      private const val TAG = "<-AppSensorService"
      const val ORIENTATION_CHANNEL = "orientation_channel"
   }
}
