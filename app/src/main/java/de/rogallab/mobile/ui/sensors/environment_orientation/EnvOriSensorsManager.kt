package de.rogallab.mobile.ui.sensors.environment_orientation

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import de.rogallab.mobile.domain.utilities.logError
import de.rogallab.mobile.domain.utilities.logInfo
import de.rogallab.mobile.domain.utilities.toLocalDateTime
import de.rogallab.mobile.domain.utilities.toTimeString

class EnvOriSensorsManager(
   context: Context,
   private val _updateInterval: Long = 5000L
) {

   // Sensor Manager
   private val _sensorManager: SensorManager =
      context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

   // Enviroment sensors
   private val _pressureSensor = _sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE)
   private val _lightSensor = _sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
   private val _temperatureSensor = _sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE)
   private val _humiditySensor = _sensorManager.getDefaultSensor(Sensor.TYPE_RELATIVE_HUMIDITY)
   // Orientation sensors
   private val _magnetometerSensor = _sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
   private val _gravitySensor = _sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY)
   private val _accSensor = _sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
   private val _rotVecSensor = _sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)

   // Environment data
   private var _pressure: Float = 0f
   private var _light: Float = 0f
   private var _temperature: Float = 0f
   private var _humidity: Float = 0f
   // Orientation data arrays
   private val _magnetometerData = FloatArray(3)
   private val _gravityData = FloatArray(3)
   private val _accData = FloatArray(3)
   private val _rotMatrix = FloatArray(9)
   private val _inclinationMatrix = FloatArray(9)
   private val _orientationAngles = FloatArray(3)

   // Callback for sensor updates
   var onEnvOriValuesChanged: (SensorValues) -> Unit = { }

   private var _currentTime: Long = 0L
   private var _lastUpdateTime = 0L

   // Sensor Event Listener
   private val sensorEventListener = object : SensorEventListener {

      override fun onSensorChanged(event: SensorEvent) {
//       logInfo(TAG, "onSensorChanged()")
         when (event.sensor.type) {
            // Environment sensors
            Sensor.TYPE_PRESSURE -> _pressure = event.values[0]
            Sensor.TYPE_LIGHT -> _light = event.values[0]
            Sensor.TYPE_AMBIENT_TEMPERATURE -> _temperature = event.values[0]
            Sensor.TYPE_RELATIVE_HUMIDITY -> _humidity = event.values[0]

            // Orientation sensors
            Sensor.TYPE_ACCELEROMETER ->
               System.arraycopy(event.values, 0, _accData, 0, _accData.size)
            Sensor.TYPE_MAGNETIC_FIELD ->
               System.arraycopy(event.values, 0, _magnetometerData, 0, _magnetometerData.size)
            Sensor.TYPE_GRAVITY ->
               System.arraycopy(event.values, 0, _gravityData, 0, _gravityData.size)
            Sensor.TYPE_ROTATION_VECTOR -> {
               // If rotation vector is available, use it directly for orientation
               SensorManager.getRotationMatrixFromVector(_rotMatrix, event.values)
               SensorManager.getOrientation(_rotMatrix, _orientationAngles)
            }
         }

         // Only update if the custom interval has passed
         _currentTime = System.currentTimeMillis()
         if (_currentTime - _lastUpdateTime < _updateInterval) return
         _lastUpdateTime = _currentTime

         val dt = toLocalDateTime(_currentTime).toTimeString()
         logInfo(TAG, "updateSensorData() $dt")
         updateEnvOriValues()
      }

      override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
         try {
            // Your existing logic here
            logInfo(TAG, "Sensor accuracy changed: $accuracy")
         } catch (e: Exception) {
            logError(TAG, "Error in onAccuracyChanged: ${e.message}")
         }
      }
   } // SensorEventListener

   private fun updateEnvOriValues() {
      var envOriSensorValues = SensorValues(
         epochMillis = System.currentTimeMillis(),
         pressure = _pressure,
         light = _light,
         temperature = _temperature,
         humidity = _humidity
      )
      if (_accData.isNotEmpty() && _magnetometerData.isNotEmpty()) {
         if (SensorManager.getRotationMatrix(_rotMatrix, _inclinationMatrix, _accData, _magnetometerData)) {
            SensorManager.getOrientation(_rotMatrix, _orientationAngles)
            // Azimuth (orientationAngles[0]) gives the angle relative to magnetic north
            val azimuth = Math.toDegrees(_orientationAngles[0].toDouble()).toFloat()

            // Yaw, Pitch, Roll + Azimuth
            envOriSensorValues = envOriSensorValues.copy(
               yaw = _orientationAngles[0],
               pitch = _orientationAngles[1],
               roll = _orientationAngles[2],
               azimuth = azimuth
            )
         }
      }

      onEnvOriValuesChanged(envOriSensorValues)
   }


   fun startListening() {
      logInfo(TAG, "startListening()")
      val delayType = SensorManager.SENSOR_DELAY_NORMAL

      _pressureSensor?.let { _sensorManager.registerListener(sensorEventListener, it, delayType) }
      _lightSensor?.let { _sensorManager.registerListener(sensorEventListener, it, delayType) }
      _temperatureSensor?.let { _sensorManager.registerListener(sensorEventListener, it, delayType) }
      _humiditySensor?.let { _sensorManager.registerListener(sensorEventListener, it, delayType) }

      _magnetometerSensor?.let { _sensorManager.registerListener(sensorEventListener, it, delayType) }
      _gravitySensor?.let { _sensorManager.registerListener(sensorEventListener, it, delayType) }
      _accSensor?.let { _sensorManager.registerListener(sensorEventListener, it, delayType) }
      _rotVecSensor?.let { _sensorManager.registerListener(sensorEventListener, it, delayType) }
   }

   // Stop listening to sensors
   fun stopListening() {
      _sensorManager.unregisterListener(sensorEventListener)
   }


   companion object {
      const val TAG = "<-OrientationManager"
   }
}
