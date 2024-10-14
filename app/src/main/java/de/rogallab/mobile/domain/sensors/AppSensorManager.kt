package de.rogallab.mobile.domain.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import de.rogallab.mobile.domain.utilities.logInfo
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class AppSensorManager(
   context: Context,
)  {

   // Sensor Manager
   private val _sensorManager: SensorManager =
      context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

   // Sensor event listener
   private var _sensorEventListener: SensorEventListener? = null

   // Environment sensors
   private val _pressureSensor = _sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE)
   private val _lightSensor = _sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
   private val _proximitySensor = _sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY)

   // Orientation sensors
   private val _accSensor = _sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
   private val _magnetometerSensor = _sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
   private val _rotVecSensor = _sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)

   // Environment data
   private var _pressure: Float = 0f
   private var _light: Float = 0f
   private var _proximity: Float = 0f
   // Orientation data arrays
   private val _magnetometerData = FloatArray(3)
   private val _accData = FloatArray(3)
   private val _rotVector = FloatArray(4)
   private val _rotMatrix = FloatArray(9)
   private val _inclinationMatrix = FloatArray(9)
   private val _orientationAngles = FloatArray(3)
   private var _isRotVecSensorAvailable = false

   private var _currentTime: Long = 0L
   private var _lastUpdateTime = 0L
   private var _updateInterval: Long = 1000L

   init {
      val deviceSensors: List<Sensor> = _sensorManager.getSensorList(Sensor.TYPE_ALL)
      logInfo(TAG, "Available sensors: ${deviceSensors.size}")
      deviceSensors.forEach { it ->
         logInfo(TAG, "Available sensor: ${it.name} - ${it.stringType}")
      }
   }

   fun setUpdateInterval(interval: Long) {
      logInfo(TAG, "setUpdateInterval($interval)")
      _updateInterval = interval
   }

   fun startListening() {
      _sensorEventListener?.let { listener ->
         logInfo(TAG, "startListening()")
         val delayType = SensorManager.SENSOR_DELAY_NORMAL

         _pressureSensor?.let { _sensorManager.registerListener(listener, it, delayType) }
         _lightSensor?.let { _sensorManager.registerListener(listener, it, delayType) }
         _proximitySensor?.let { _sensorManager.registerListener(listener, it, delayType) }

         _accSensor?.let { _sensorManager.registerListener(listener, it, delayType) }
         _magnetometerSensor?.let { _sensorManager.registerListener(listener, it, delayType) }
         _rotVecSensor?.let { _sensorManager.registerListener(listener, it, delayType) }
      }
   }

   // Stop listening to sensors
   fun stopListening() {
      _sensorEventListener?.let { listener ->
         logInfo(TAG, "stopListening()")
         _sensorManager.unregisterListener(listener)
      }
   }

   // Flow that emits new sensor values when available
   fun sensorValuesFlow(): Flow<SensorValues> = callbackFlow {

      _sensorEventListener = object : SensorEventListener {
         override fun onSensorChanged(event: SensorEvent?) {
            event?.let {
               // Update sensor values and emit them
               val (shouldSend, sensorValues) = updateSensorValues(event)
               if(shouldSend)
                  trySend(sensorValues!!).isSuccess // Emit the new sensor values
            }
         }
         override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
            logInfo(TAG, "Sensor accuracy changed: $accuracy")
         }
      }

      // Start listening to sensor updates
      startListening()

      // Await closure when the listener is unregistered
      awaitClose {
         // Stop listening when the flow collector stops
         stopListening()
      }
   }

   private fun updateSensorValues(event: SensorEvent): Pair<Boolean, SensorValues?> { 

      when (event.sensor.type) {
         // Environment sensors
         Sensor.TYPE_PRESSURE -> _pressure = event.values[0]
         Sensor.TYPE_LIGHT -> _light = event.values[0]
         Sensor.TYPE_PROXIMITY -> _proximity = event.values[0]

         // Orientation sensors
         Sensor.TYPE_ACCELEROMETER ->
            System.arraycopy(event.values, 0, _accData, 0, _accData.size)
         Sensor.TYPE_MAGNETIC_FIELD ->
            System.arraycopy(event.values, 0, _magnetometerData, 0, _magnetometerData.size)
         Sensor.TYPE_ROTATION_VECTOR -> {
            if (event.values.size >= 4) {
               System.arraycopy(event.values, 0, _rotVector, 0, 4)
               // Optionally handle the fifth value if needed
               val headingAccuracy = event.values[4]
            }
            _isRotVecSensorAvailable = true
         }
      }

      // Only update if the custom interval has passed
      _currentTime = System.currentTimeMillis()
      if (_currentTime - _lastUpdateTime <= _updateInterval) return Pair(false, null)

      _lastUpdateTime = _currentTime

      var sensorValues = processSensorValues()
      return Pair(true, sensorValues)
   }

   private fun processSensorValues(): SensorValues {

      var sensorValues = SensorValues(
         time = _currentTime,
         pressure = _pressure,
         light = _light
      )
      if (_isRotVecSensorAvailable) {
         // Use rotation vector sensor if available (higher accuracy)
         if (_rotVector.size == 4) {
            // Normalize the quaternion
            val q0 = _rotVector[3]
            val q1 = _rotVector[0]
            val q2 = _rotVector[1]
            val q3 = _rotVector[2]
            val norm = Math.sqrt((q0 * q0 + q1 * q1 + q2 * q2 + q3 * q3).toDouble())
            _rotVector[0] = (q1 / norm).toFloat()
            _rotVector[1] = (q2 / norm).toFloat()
            _rotVector[2] = (q3 / norm).toFloat()
            _rotVector[3] = (q0 / norm).toFloat()
         }
         SensorManager.getRotationMatrixFromVector(_rotMatrix, _rotVector)
         SensorManager.getOrientation(_rotMatrix, _orientationAngles)
      } else if (_accData.isNotEmpty() && _magnetometerData.isNotEmpty()) {
         // Fallback to accelerometer and magnetometer (less accurate)
         if (SensorManager.getRotationMatrix(_rotMatrix, _inclinationMatrix, _accData, _magnetometerData)) {
            SensorManager.getOrientation(_rotMatrix, _orientationAngles)
         }
      }

      // Yaw, Pitch, Roll + Azimuth
      val yaw = _orientationAngles[0] * RADIANS_TO_DEGREES
      val pitch = _orientationAngles[1] * RADIANS_TO_DEGREES
      val roll = _orientationAngles[2] * RADIANS_TO_DEGREES

      // Transform accelerometer data to global coordinate system
      //      val globalAccData = FloatArray(3)
      //      globalAccData[0] = _rotMatrix[0] * _accData[0] + _rotMatrix[1] * _accData[1] + _rotMatrix[2] * _accData[2]
      //      globalAccData[1] = _rotMatrix[3] * _accData[0] + _rotMatrix[4] * _accData[1] + _rotMatrix[5] * _accData[2]
      //      globalAccData[2] = _rotMatrix[6] * _accData[0] + _rotMatrix[7] * _accData[1] + _rotMatrix[8] * _accData[2]

      sensorValues = sensorValues.copy(
         yaw = yaw,
         pitch = pitch,
         roll = roll,
         accLx = _accData[0],
         accLy = _accData[1],
         accLz = _accData[2],
      )
      return sensorValues
   }

   companion object {
      private const val TAG = "<-AppSensorManager"
      private const val RADIANS_TO_DEGREES = 57.29578f
   }
}
