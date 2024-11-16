package de.rogallab.mobile.ui.sensors.orientation

import de.rogallab.mobile.domain.model.SensorValue
import de.rogallab.mobile.domain.utilities.RingBuffer

data class SensorsUiState(
   val last: SensorValue? = null,
   val ringBuffer: RingBuffer<SensorValue> = RingBuffer(600),
)