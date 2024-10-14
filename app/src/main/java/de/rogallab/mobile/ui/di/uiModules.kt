package de.rogallab.mobile.ui.di

import android.app.Application
import android.content.Context
import android.hardware.SensorManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import de.rogallab.mobile.domain.sensors.AppSensorManager
import de.rogallab.mobile.domain.utilities.logInfo
import de.rogallab.mobile.ui.ResourceProvider
import de.rogallab.mobile.ui.camera.CameraViewModel
import de.rogallab.mobile.ui.errors.ErrorResources
import de.rogallab.mobile.ui.home.HomeViewModel
import de.rogallab.mobile.ui.navigation.NavigationViewModel
import de.rogallab.mobile.ui.people.PeopleViewModel
import de.rogallab.mobile.ui.sensors.environment_orientation.SensorsViewModel
import de.rogallab.mobile.ui.sensors.location.LocationsViewModel
import de.rogallab.mobile.ui.settings.SettingsViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val uiModules: Module = module {

   val tag = "|-uiModules"
   //logInfo("<-uiModules", "single -> Application")
   //single { get<Application>() }


   // Provide HomeViewModel ----------------------------------------------
   logInfo(tag, "viewModel -> HomeViewModel")
   viewModel<HomeViewModel> { HomeViewModel() }

   // Provide PeopleViewModel --------------------------------------------
   logInfo(tag, "single    -> ResourceProvider")
   single { ResourceProvider( androidContext() ) }

   logInfo("<-uiModules", "single    -> ErrorResources")
   single { ErrorResources( get() ) } // get() -> resourceProvider

   logInfo("<-uiModules", "viewModel -> PeopleViewModel")
   viewModel { PeopleViewModel( get(), get() ) } // get() -> IPeopleRepository, ErrorResources

   logInfo("<-uiModules", "viewModel -> CameraViewModel")
   viewModel{ CameraViewModel( get(), get() ) } // get() -> Context, LifeCycleOwner


   // Provide SensorsViewModel -------------------------------------------


   logInfo("<-uiModules", "viewModel -> SensorsViewModel")
   viewModel<SensorsViewModel> { SensorsViewModel( get() ) }

   logInfo("<-uiModules", "viewModel -> LocationsViewModel")
   viewModel<LocationsViewModel> { LocationsViewModel( get() ) }



   // Provide NavigationViewModel ----------------------------------------
   logInfo("<-uiModules", "viewModel -> NavigationViewModel")
   viewModel<NavigationViewModel> { NavigationViewModel() }

   // Provide SettingsViewModel ----------------------------------------
   logInfo("<-uiModules", "viewModel -> SettingsViewModel")
   viewModel<SettingsViewModel> { SettingsViewModel( get() ) }

}


