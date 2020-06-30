package com.anywhereapps.handlers

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager

class SensorHandler(var mContext: Context, var mListener: SensorListener) : Thread(), SensorEventListener {


//    public static final float GEOMAGNETIC_SMOOTHING_FACTOR = 0.4f;
//    public static final float GRAVITY_SMOOTHING_FACTOR = 0.1f;

    val GEOMAGNETIC_SMOOTHING_FACTOR = 0.035f
    val GRAVITY_SMOOTHING_FACTOR = 0.035f

    private var inclinationMatrix = FloatArray(9)
    private var azimuth = 0.0
    private var mGeomagnetic: FloatArray? = FloatArray(3)
    private var mGravity: FloatArray? = FloatArray(3)
    private var rotationMatrix = FloatArray(9)

    private val mAccelerometer: Sensor
    private val mMagneticField: Sensor
    private val mSensorManager: SensorManager

    private var lastAzimuth = 0.0
    private var orientation = FloatArray(3)
    private var success = false

    interface SensorListener {
        fun onSensorChanged(lastAzimuth: Float, azimuth: Float)
        fun onAccuracyChanged(accuracy: Int)
    }


    init {
        mSensorManager = mContext.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        mMagneticField = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
    }

    fun register() {
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_GAME)
        mSensorManager.registerListener(this, mMagneticField, SensorManager.SENSOR_DELAY_GAME)
    }

    fun unRegister() {
        mSensorManager.unregisterListener(this)
    }

    private fun exponentialSmoothing(newValue: FloatArray, lastValue: FloatArray?, alpha: Float): FloatArray {
        if (lastValue == null) {
            return newValue
        }
        for (i in newValue.indices) {
            lastValue[i] = lastValue[i] + alpha * (newValue[i] - lastValue[i]) / 2.0f
        }
        return lastValue
    }

    override fun onSensorChanged(event: SensorEvent) {
        mListener.onAccuracyChanged(event.accuracy)
        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> exponentialSmoothing(event.values, mGravity, GRAVITY_SMOOTHING_FACTOR)
            Sensor.TYPE_MAGNETIC_FIELD -> exponentialSmoothing(event.values, mGeomagnetic, GEOMAGNETIC_SMOOTHING_FACTOR)
        }
        if (mGravity != null && mGeomagnetic != null) {

            success = SensorManager.getRotationMatrix(rotationMatrix, inclinationMatrix, mGravity, mGeomagnetic)

            if (success) {
                SensorManager.getOrientation(rotationMatrix, orientation)
                azimuth = Math.toDegrees(orientation[0].toDouble()).toFloat().toDouble()
                if (179.0 == azimuth && -179.0 == lastAzimuth) {
                    mListener.onSensorChanged(azimuth.toFloat(), azimuth.toFloat())
                }
                if (-179.0 == azimuth && 179.0 == lastAzimuth) {
                    mListener.onSensorChanged(azimuth.toFloat(), azimuth.toFloat())
                } else if (azimuth >= 0.0 && lastAzimuth >= 0.0 || azimuth <= 0.0 && lastAzimuth <= 0.0) {
                    mListener.onSensorChanged(lastAzimuth.toFloat(), azimuth.toFloat())
                }
                lastAzimuth = azimuth
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        mListener.onAccuracyChanged(accuracy)
    }


}