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

class AppSensorsManager(
   context: Context,
   private val _updateInterval: Long = 60000L
) {

   // Sensor Manager
   private val _sensorManager: SensorManager =
      context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

   // Environment sensors
   private val _pressureSensor = _sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE)
   private val _lightSensor = _sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
   private val _proximitySensor = _sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY)

   // Orientation sensors
   private val _accSensor = _sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
   private val _magnetometerSensor = _sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
// private val _rotVecSensor = _sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)

   // Environment data
   private var _pressure: Float = 0f
   private var _light: Float = 0f
   // Orientation data arrays
   private val _magnetometerData = FloatArray(3)
   private val _accData = FloatArray(3)
// private val _rotVector = FloatArray(4)
   private val _rotMatrix = FloatArray(9)
   private val _inclinationMatrix = FloatArray(9)
   private val _orientationAngles = FloatArray(3)
   private var _isRotVecSensorAvailable = false


   init {
      val deviceSensors: List<Sensor> = _sensorManager.getSensorList(Sensor.TYPE_ALL)
      logInfo(TAG, "Available sensors: ${deviceSensors.size}")
      deviceSensors.forEach { it ->
         logInfo(TAG, "Available sensor: ${it.name} - ${it.stringType}")
      }
   }

   // Callback for sensor updates
   var onSensorValuesChanged: (SensorValues) -> Unit = { }

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

            // Orientation sensors
            Sensor.TYPE_ACCELEROMETER ->
               System.arraycopy(event.values, 0, _accData, 0, _accData.size)
            Sensor.TYPE_MAGNETIC_FIELD ->
               System.arraycopy(event.values, 0, _magnetometerData, 0, _magnetometerData.size)
//          Sensor.TYPE_ROTATION_VECTOR -> {
//             System.arraycopy(event.values, 0,  _rotVector, 0, event.values.size)
//             _isRotVecSensorAvailable = true
//          }
         }
         // Only update if the custom interval has passed
         _currentTime = System.currentTimeMillis()
         if (_currentTime - _lastUpdateTime < _updateInterval) return
         _lastUpdateTime = _currentTime

         updateSensorValues()
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

   private fun updateSensorValues() {

      var sensorValues = SensorValues(
         epochMillis = _currentTime,
         pressure = _pressure,
         light = _light
      )
//      if (_isRotVecSensorAvailable) {
//         // Use rotation vector sensor if available (higher accuracy)
//         if (_rotVector.size == 4) {
//            // Normalize the quaternion
//            val q0 = _rotVector[3]
//            val q1 = _rotVector[0]
//            val q2 = _rotVector[1]
//            val q3 = _rotVector[2]
//            val norm = Math.sqrt((q0 * q0 + q1 * q1 + q2 * q2 + q3 * q3).toDouble())
//            _rotVector[0] = (q1 / norm).toFloat()
//            _rotVector[1] = (q2 / norm).toFloat()
//            _rotVector[2] = (q3 / norm).toFloat()
//            _rotVector[3] = (q0 / norm).toFloat()
//         }
//         SensorManager.getRotationMatrixFromVector(_rotMatrix, _rotVector)
//         SensorManager.getOrientation(_rotMatrix, _orientationAngles)
//      } else if
      if (_accData.isNotEmpty() && _magnetometerData.isNotEmpty()) {
         // Fallback to accelerometer and magnetometer (less accurate)
         if (SensorManager.getRotationMatrix(_rotMatrix, _inclinationMatrix, _accData, _magnetometerData)) {
            SensorManager.getOrientation(_rotMatrix, _orientationAngles)
         }
      }

      // Yaw, Pitch, Roll + Azimuth
      val yaw = _orientationAngles[0]*RADIANS_TO_DEGREES
      val pitch = _orientationAngles[1]*RADIANS_TO_DEGREES
      val roll = _orientationAngles[2]*RADIANS_TO_DEGREES

      sensorValues = sensorValues.copy(
         yaw = yaw,
         pitch = pitch,
         roll = roll,
      )

      onSensorValuesChanged(sensorValues)
   }


   fun startListening() {
      logInfo(TAG, "startListening()")
      val delayType = SensorManager.SENSOR_DELAY_NORMAL

      _pressureSensor?.let { _sensorManager.registerListener(sensorEventListener, it, delayType) }
      _lightSensor?.let { _sensorManager.registerListener(sensorEventListener, it, delayType) }

      _accSensor?.let { _sensorManager.registerListener(sensorEventListener, it, delayType) }
      _magnetometerSensor?.let { _sensorManager.registerListener(sensorEventListener, it, delayType) }
//    _rotVecSensor?.let { _sensorManager.registerListener(sensorEventListener, it, delayType) }
   }

   // Stop listening to sensors
   fun stopListening() {
      _sensorManager.unregisterListener(sensorEventListener)
   }


   companion object {
      private const val TAG = "<-AppSensorsManager"
      private const val RADIANS_TO_DEGREES = 57.29578f
   }
}
