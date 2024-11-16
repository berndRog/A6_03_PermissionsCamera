package de.rogallab.mobile


import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import de.rogallab.mobile.data.IDataStore
import de.rogallab.mobile.data.devices.AppLocationManager
import de.rogallab.mobile.data.devices.AppSensorManager
import de.rogallab.mobile.data.local.datastore.DataStore
import de.rogallab.mobile.data.mediastore.MediaStoreRepository
import de.rogallab.mobile.data.repositories.PeopleRepository
import de.rogallab.mobile.data.repositories.SettingsRepository
import de.rogallab.mobile.domain.IAppLocationManager
import de.rogallab.mobile.domain.IAppSensorManager
import de.rogallab.mobile.domain.IMediaStoreRepository
import de.rogallab.mobile.domain.IPeopleRepository
import de.rogallab.mobile.domain.ISettingsRepository
import de.rogallab.mobile.domain.utilities.logError
import de.rogallab.mobile.domain.utilities.logInfo
import de.rogallab.mobile.ui.camera.CameraViewModel
import de.rogallab.mobile.ui.home.HomeViewModel
import de.rogallab.mobile.ui.navigation.NavigationViewModel
import de.rogallab.mobile.ui.people.PeopleViewModel
import de.rogallab.mobile.ui.people.PersonValidator
import de.rogallab.mobile.ui.sensors.location.LocationsViewModel
import de.rogallab.mobile.ui.sensors.location.services.AppLocationService
import de.rogallab.mobile.ui.sensors.orientation.SensorsViewModel
import de.rogallab.mobile.ui.sensors.orientation.services.AppSensorService
import de.rogallab.mobile.ui.settings.SettingsViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module



typealias CoroutineDispatcherMain = CoroutineDispatcher
typealias CoroutineDispatcherIO = CoroutineDispatcher
typealias CoroutineScopeIO = CoroutineScope


val coroutineExceptionHandler = CoroutineExceptionHandler { _, exception ->
   // Handle the exception here
   logError("<-E R R O R", "Coroutine exception: ${exception.localizedMessage}")
}

val domainModules: Module = module {
   val tag = "<-domainModules"

   logInfo( tag, "single    -> CoroutineDispatcherMain")
   single<CoroutineDispatcherMain> { Dispatchers.Main }

   logInfo( tag, "single    -> CoroutineDispatcherIO")
   single<CoroutineDispatcherIO> { Dispatchers.IO }

   logInfo( tag, "single    -> CoroutineDispatcher")
   single<CoroutineScopeIO> {
      CoroutineScope(
         SupervisorJob() +
            coroutineExceptionHandler +
            get<CoroutineDispatcherIO>()
      )
   }
}

val uiModules: Module = module {
   val tag = "<-uiModules"

   logInfo(tag, "single    -> LifecycleOwner")
   single<LifecycleOwner> {
      ProcessLifecycleOwner.get()
   }

   // initialize the Navigation Controller in MAinActivity
   single{ (navController: NavHostController) -> navController }

   // Provide HomeViewModel ----------------------------------------------
   logInfo(tag, "viewModel -> HomeViewModel")
   viewModel<HomeViewModel> { HomeViewModel() }


   // Provide PeopleViewModel --------------------------------------------
   logInfo(tag, "single    -> PeopleInputValidator")
   single<PersonValidator> {
      PersonValidator( androidContext() )
   }
   logInfo(tag, "viewModel -> PeopleViewModel")
   viewModel<PeopleViewModel> {
      PeopleViewModel(
         get<IPeopleRepository>(),
         get<PersonValidator>()
      )
   }
   // Provide CameraSensorsViewModel -------------------------------------
   logInfo(tag, "viewModel -> CameraViewModel")
   // get() -> Context, LifeCycleOwner
   viewModel<CameraViewModel>{
      CameraViewModel(
         androidContext(),
         get<LifecycleOwner>()
      )
   }
   // Provide AppLocationService & LocationsViewModel --------------------
   logInfo(tag, "single    -> AppLocationService")
   single<AppLocationService> { AppLocationService( ) }

   logInfo(tag, "viewModel -> LocationsViewModel")
   viewModel<LocationsViewModel> {
      LocationsViewModel(
         androidApplication(),
         get<IAppLocationManager>(),
         get<CoroutineDispatcherIO>()
      )
   }

   // Provide  AppSensorService & SensorsViewModel -----------------------
   logInfo(tag, "single    -> AppSensorService")
   single<AppSensorService> { AppSensorService( ) }

   logInfo(tag, "viewModel -> SensorsViewModel")
   viewModel<SensorsViewModel> {
      SensorsViewModel(
         androidContext(),
         get<IAppSensorManager>(),
         get<CoroutineDispatcherIO>()
      )
   }

   // Provide NavigationViewModel ----------------------------------------
   logInfo(tag, "viewModel -> NavigationViewModel")
   viewModel<NavigationViewModel> { NavigationViewModel() }

   // Provide SettingsViewModel ----------------------------------------
   logInfo(tag, "viewModel -> SettingsViewModel")
   viewModel<SettingsViewModel> { SettingsViewModel( androidApplication() ) }

}

val dataModules = module {

   val tag = "<-dataModules"

//   logInfo("<-dataModules", "single -> AppDatabase")
//   single {
//      Room.databaseBuilder(androidContext(), AppDatabase::class.java, "app_database")
//         .build()
//   }
//   single { get<AppDatabase>().userDao() }

//   logInfo("<-dataModules", "single -> HttpClientBuilder")
//   single<HttpClient> {
//      HttpClientBuilder()
//      .protocol(URLProtocol.HTTP)
//      .host(AppStart.BASEURL)
//      .build()
//   }

//   logInfo("[Koin]", "singleOf -> WebService")
//   singleOf(::WebService)

   logInfo(tag, "single    -> SharedPreferences")
   single<SharedPreferences> {
      androidContext().getSharedPreferences("sensor_settings", Context.MODE_PRIVATE)
   }

   logInfo(tag, "single    -> DataStore: IDataStore")
   single<IDataStore>{
      DataStore(
         androidContext(),
         get<CoroutineDispatcherMain>(),
         get<CoroutineDispatcherIO>()
      )
   }

   logInfo(tag, "single    -> MediaStoreRepository: IMediaStoreRepository")
   single<IMediaStoreRepository>{
      MediaStoreRepository( androidContext() )
   }

   logInfo(tag, "single    -> PeopleRepository: IPeopleRepository")
   single<IPeopleRepository> {
      PeopleRepository(
         get<IDataStore>(),
         get<CoroutineDispatcherIO>()
      )
   }

   logInfo(tag, "single    -> SettingsRepository: ISettingsRepository")
   single<ISettingsRepository>{ SettingsRepository( androidContext() ) }


   logInfo(tag, "single    -> AppLocationManager")
   single<IAppLocationManager> {  AppLocationManager( androidContext() )  }

   logInfo(tag, "single    -> AppSensorManager")
   single<IAppSensorManager> {  AppSensorManager( androidContext() )  }}