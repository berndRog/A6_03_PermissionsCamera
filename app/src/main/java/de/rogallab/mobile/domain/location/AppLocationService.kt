package de.rogallab.mobile.domain.location

import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.IBinder
import androidx.core.app.NotificationCompat
import de.rogallab.mobile.R
import de.rogallab.mobile.domain.utilities.formatEpochLatLng
import de.rogallab.mobile.domain.utilities.logDebug
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

// in order to start the service by manifest, the class must have a default constructor
class AppLocationService() : Service() {


    private val _locationManager: AppLocationManager by inject()

    private val scope = CoroutineScope(
        SupervisorJob() + Dispatchers.IO
    )

    override fun onBind(intent: Intent?): IBinder? {
        return null
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
            .Builder(this, LOCATION_CHANNEL)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Location Tracker")
            .setStyle(NotificationCompat.BigTextStyle())

        startForeground(1, notification.build())

        scope.launch {
            _locationManager.trackLocation().collect { location ->
                // send location as broadcast
                broadcastLocation(location)

                // send location as notification
                notificationManager.notify(
                    1,
                    notification.setContentText(formatEpochLatLng(location))
                       .build()
                )
            }
        }

    }

    private fun stopForegroundService() {
        logDebug(TAG,"stopForeGroundService")
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }

    enum class Action {
        START, STOP
    }

    private fun broadcastLocation(location: Location) {

        logDebug(TAG,"send broadcast ${formatEpochLatLng(location)}")
        val intent = Intent("LOCATION_UPDATE")
        intent.putExtra("time", location.time)
        intent.putExtra("latitude", location.latitude)
        intent.putExtra("longitude", location.longitude)
        sendBroadcast(intent)
    }

    companion object {
        private const val TAG = "<-AppLocationService"
        const val LOCATION_CHANNEL = "location_channel"
    }
}