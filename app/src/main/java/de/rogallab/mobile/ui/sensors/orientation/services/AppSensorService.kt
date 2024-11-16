package de.rogallab.mobile.ui.sensors.orientation.services

import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import de.rogallab.mobile.CoroutineScopeIO
import de.rogallab.mobile.R
import de.rogallab.mobile.domain.IAppSensorManager
import de.rogallab.mobile.domain.model.SensorValue
import de.rogallab.mobile.domain.utilities.logDebug
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject

// in order to start the service by manifest, the class must have a default constructor
class AppSensorService() : Service() {

   private val _sensorManager: IAppSensorManager by inject<IAppSensorManager>()
   private val _coroutineScope: CoroutineScope by inject<CoroutineScopeIO>()

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

      _coroutineScope.launch {
         _sensorManager.sensorValuesFlow().collect { sensorValues ->

            // send orientation async as broadcast using Dispatchers.Main
            withContext(Dispatchers.Main) {
               broadcastOrientation(sensorValues)
            }
            // send orientation async as notification using Dispatchers.Main
            withContext(Dispatchers.Main) {
               val text = "orientation: azimuth: ${sensorValues.yaw}"
               notificationManager.notify(2, notification.setContentText(text)
                  .build()
               )
            }
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
      _coroutineScope.cancel()
   }

   enum class Action {
      START, STOP
   }

   private fun broadcastOrientation(sensorValues: SensorValue) {

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
