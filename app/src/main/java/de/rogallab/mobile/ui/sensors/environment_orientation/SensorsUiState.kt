package de.rogallab.mobile.ui.sensors.environment_orientation

import de.rogallab.mobile.domain.sensors.SensorValues
import de.rogallab.mobile.domain.utilities.RingBuffer

data class SensorsUiState(
   val last: SensorValues? = null,
   val ringBuffer: RingBuffer<SensorValues> = RingBuffer(600),
)