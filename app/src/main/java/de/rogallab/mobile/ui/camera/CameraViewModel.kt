package de.rogallab.mobile.ui.camera

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import androidx.camera.video.Recording
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.compose.runtime.remember
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import de.rogallab.mobile.domain.utilities.logDebug
import de.rogallab.mobile.ui.base.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class CameraViewModel(
   context: Context,
   lifecyleOwner: LifecycleOwner
): BaseViewModel(TAG) {

   val CAMERAX_PERMISSIONS = arrayOf(
      android.Manifest.permission.CAMERA,
      android.Manifest.permission.RECORD_AUDIO
      //android.Manifest.permission.WRITE_EXTERNAL_STORAGE
   )

   val cameraController = LifecycleCameraController(context)
      .apply {
         bindToLifecycle(lifecyleOwner)
         setEnabledUseCases(
            CameraController.IMAGE_CAPTURE or
            CameraController.VIDEO_CAPTURE
         )
      }


   private val _bitmapsStateFlow: MutableStateFlow<List<Bitmap>>
      = MutableStateFlow(emptyList())
   val bitmapsStateFlow: StateFlow<List<Bitmap>>
      = _bitmapsStateFlow.asStateFlow()

   fun onAddPhoto(bitmap: Bitmap) {
      _bitmapsStateFlow.update { bitmaps ->
         // add the new bitmap to the observable list of bitmaps
         bitmaps + bitmap
      }
      logDebug(TAG, "onAddPhoto: ${_bitmapsStateFlow.value.size}")
   }

   companion object {
      private const val TAG = "<-CameraViewModel"



   }
}