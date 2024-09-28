package de.rogallab.mobile.ui.sensors.environment_orientation

import de.rogallab.mobile.domain.utilities.RingBuffer

data class EnvOriUiState(
   val last: SensorValues? = null,
   val ringBuffer: RingBuffer<SensorValues> = RingBuffer(600),
)