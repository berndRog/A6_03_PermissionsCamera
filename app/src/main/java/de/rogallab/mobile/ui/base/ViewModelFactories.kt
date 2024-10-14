package de.rogallab.mobile.ui.base

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import de.rogallab.mobile.ui.camera.CameraViewModel
import de.rogallab.mobile.ui.people.PeopleViewModel
import de.rogallab.mobile.ui.sensors.environment_orientation.SensorsViewModel
import de.rogallab.mobile.ui.sensors.location.LocationsViewModel
import de.rogallab.mobile.ui.settings.SettingsViewModel

//class PeopleViewModelFactory(
//   private val application: Application
//) : ViewModelProvider.Factory {
//   override fun <T : ViewModel> create(modelClass: Class<T>): T {
//      if (modelClass.isAssignableFrom(PeopleViewModel::class.java)) {
//         @Suppress("UNCHECKED_CAST")
//         return PeopleViewModel(application) as T
//      }
//      throw IllegalArgumentException("Unknown ViewModel class")
//   }
//}

//class LocationsViewModelFactory(
//   private val application: Application
//) : ViewModelProvider.Factory {
//   override fun <T : ViewModel> create(modelClass: Class<T>): T {
//      if (modelClass.isAssignableFrom(LocationsViewModel::class.java)) {
//         @Suppress("UNCHECKED_CAST")
//         return LocationsViewModel(application) as T
//      }
//      throw IllegalArgumentException("Unknown ViewModel class")
//   }
//}

//class SensorsViewModelFactory(
//   private val application: Application
//) : ViewModelProvider.Factory {
//   override fun <T : ViewModel> create(modelClass: Class<T>): T {
//      if (modelClass.isAssignableFrom(SensorsViewModel::class.java)) {
//         @Suppress("UNCHECKED_CAST")
//         return SensorsViewModel(application) as T
//      }
//      throw IllegalArgumentException("Unknown ViewModel class")
//   }
//}
//
//class CameraViewModelFactory(
//   private val application: Application
//) : ViewModelProvider.Factory {
//   override fun <T : ViewModel> create(modelClass: Class<T>): T {
//      if (modelClass.isAssignableFrom(CameraViewModel::class.java)) {
//         @Suppress("UNCHECKED_CAST")
//         return CameraViewModel(application) as T
//      }
//      throw IllegalArgumentException("Unknown ViewModel class")
//   }
//}
//
//class SettingsViewModelFactory(
//   private val application: Application
//) : ViewModelProvider.Factory {
//   override fun <T : ViewModel> create(modelClass: Class<T>): T {
//      if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
//         @Suppress("UNCHECKED_CAST")
//         return SettingsViewModel(application) as T
//      }
//      throw IllegalArgumentException("Unknown ViewModel class")
//   }
//}