package de.rogallab.mobile.data.di

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Resources
import de.rogallab.mobile.data.PeopleRepository
import de.rogallab.mobile.data.local.DataStore
import de.rogallab.mobile.data.local.IDataStore
import de.rogallab.mobile.data.repositories.SettingsRepository
import de.rogallab.mobile.domain.IPeopleRepository
import de.rogallab.mobile.domain.ISettingsRepository
import de.rogallab.mobile.domain.utilities.logInfo
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext.get
import org.koin.dsl.module

val dataModules = module {

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

   // Provide Resources
   logInfo("<-dataModules", "single    -> Resources")
   single<Resources> { androidContext().resources }

   // Provide SharedPreferences
   logInfo("<-dataModules", "single    -> SharedPreferences")
   single<SharedPreferences> {
      androidContext().getSharedPreferences("sensor_settings", Context.MODE_PRIVATE)
   }

   // Provide IDataStore
   logInfo("<-dataModules", "single    -> DataStore: IDataStore")
   single<IDataStore>{ DataStore( get(), get() )}

   // Provide IPeopleRepository
   logInfo("<-dataModules", "single    -> PeopleRepository: IPeopleRepository")
   single<IPeopleRepository> {   PeopleRepository( get() ) }

   // Provide IPeopleRepository
   logInfo("<-dataModules", "single    -> SettingsRepository: ISettingsRepository")
   single<ISettingsRepository>{ SettingsRepository( get() ) }


   //  logInfo("[Koin]", "singleOf -> ImagesRepository")
 //  singleOf(::ImagesRepositoryImpl) { bind<ImagesRepository>() }


}