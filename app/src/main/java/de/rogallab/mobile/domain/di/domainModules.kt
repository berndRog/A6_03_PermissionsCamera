package de.rogallab.mobile.domain.di

import android.content.Context
import android.hardware.SensorManager
import de.rogallab.mobile.domain.location.AppLocationManager
import de.rogallab.mobile.domain.sensors.AppSensorManager
import de.rogallab.mobile.domain.utilities.logInfo
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

val domainModules: Module = module {

   logInfo("<-domainModules", "single    -> CoroutineDispatcher")
   single<CoroutineDispatcher> { Dispatchers.IO }

   logInfo("<-domainModules", "single    -> AppLocationManager")
   single {  AppLocationManager( get() ) } // get() = Context

   logInfo("<-domainModules", "single    -> AppSensorManager")
   single { AppSensorManager( get()) }

}

