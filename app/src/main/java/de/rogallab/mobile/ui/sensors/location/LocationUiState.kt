package de.rogallab.mobile.ui.sensors.location

import de.rogallab.mobile.domain.location.LocationValue
import de.rogallab.mobile.domain.utilities.RingBuffer

data class LocationUiState(
   val last: LocationValue = LocationValue(),
   val ringBuffer: RingBuffer<LocationValue> = RingBuffer(600)
)