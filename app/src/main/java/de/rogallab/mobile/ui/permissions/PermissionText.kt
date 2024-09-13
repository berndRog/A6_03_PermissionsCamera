package de.rogallab.mobile.ui.permissions


import android.content.Context
import androidx.compose.ui.res.stringResource
import de.rogallab.mobile.R

class PermissionCamera : IPermissionText {
//	<uses-feature
//		android:name="android.hardware.camera"
//		android:required="false" />
//	<uses-permission android:name="android.permission.CAMERA" />
   override fun getDescription(context: Context, isPermanentlyDeclined: Boolean): String {
      return if (isPermanentlyDeclined) {
         context.getString(R.string.declinedCamera)
      } else {
         context.getString(R.string.permissionCamera)
      }
   }
}

class PermissionRecordAudio : IPermissionText {
   override fun getDescription(context: Context, isPermanentlyDeclined: Boolean): String {
      return if (isPermanentlyDeclined) {
         context.getString(R.string.declinedAudio)
      } else {
         context.getString(R.string.permissionAudio)
      }
   }
}
class PermissionPhoneCall : IPermissionText {
   override fun getDescription(context: Context, isPermanentlyDeclined: Boolean): String {
      return if (isPermanentlyDeclined) {
         "Es scheint als hätten Sie den Zugriff auf Anrufen mehrfach abgelehnt. " +
            "Sie können diese Entscheidung nur über die App Einstellungen ändern."
      } else {
         "Die App erfordert den Zugriff auf das Telefon, um einen Anruf durchführen zu können."
      }
   }
}
/*
class PermissionCoarseLocation : IPermissionText {
   override fun getDescription(context: Context, isPermanentlyDeclined: Boolean): String {
      return if (isPermanentlyDeclined) {
         "Es scheint als hätten Sie den Zugriff auf die ungefähre Ortsbestimmung mehrfach abgelehnt. "+
            "Sie können diese Berechtigung nur noch über die App Einstellungen ändern."
      } else {
         "Die App erfordert den Zugriff auf die ungefähre Ortsbestimmung, um ihre Position zu ermitteln."
      }
   }
}

class PermissionFineLocation : IPermissionText {
   override fun getDescription(context: Context, isPermanentlyDeclined: Boolean): String {
      return if (isPermanentlyDeclined) {
         "Es scheint als hätten Sie den Zugriff auf die genaue Ortsbestimmung mehrfach abgelehnt. " +
            "Sie können diese Berechtigung nur noch über die App Einstellungen ändern."
      } else {
         "Die App erfordert die den Zugriff auf die genaue Ortsbestimmung, um ihre genaue Position zu ermitteln."
      }
   }
}
*/