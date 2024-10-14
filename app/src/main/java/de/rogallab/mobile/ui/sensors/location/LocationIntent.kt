package de.rogallab.mobile.ui.sensors.location

sealed class LocationIntent {
   data object GetLocation : LocationIntent()
   data object StartLocationService : LocationIntent()
   data object StopLocationService : LocationIntent()
}