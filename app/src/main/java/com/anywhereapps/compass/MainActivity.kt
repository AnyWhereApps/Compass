package com.anywhereapps.compass

import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.anywhereapps.handlers.LocationHandler
import com.anywhereapps.handlers.SensorHandler
import com.anywhereapps.utils.LocationConverter
import com.anywhereapps.utils.Util
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() , SensorHandler.SensorListener,  LocationHandler.LocationListener{

    private val TAG = "MainActivity"

    private lateinit var compassImage: ImageView
    private lateinit var mHeading: TextView
    private lateinit var mAddress: TextView
    private lateinit var mCoordinate:TextView

    var mSensorHandler : SensorHandler? = null
    var mLocationHandler : LocationHandler? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        compassImage = findViewById(R.id.imageViewCompass)
        mHeading = findViewById(R.id.heading)
        mAddress = findViewById(R.id.locationAddress)
        mCoordinate = findViewById(R.id.coordinates)


        mSensorHandler = SensorHandler(this, this)
        mSensorHandler?.start()

        mLocationHandler = LocationHandler(this, this)
        mLocationHandler?.getLocation()


    }

    override fun onResume() {
        super.onResume()

        mSensorHandler?.register()
    }

    override fun onPause() {
        super.onPause()

        mSensorHandler?.unRegister()
    }

    override fun onStart() {
        super.onStart()
        mLocationHandler?.requestLocationUpdates()
    }

    override fun onStop() {
        super.onStop()
        mLocationHandler?.removeLocationUpdates()
    }


    /**
     * Callback received when a permissions request has been completed.
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String?>,
                                            grantResults: IntArray) {
        Log.i(TAG, "onRequestPermissionResult")
        mLocationHandler!!.onRequestPermissionsResult(requestCode , permissions, grantResults)
    }

    override fun onLocationChange(location: Location?) {
        if(location == null)
            return

        val address = Util.getAddressFromLocation(this, location.latitude , location.longitude)

        if (address.isNotEmpty())
        mAddress.text = address

        mCoordinate.text = LocationConverter.latitudeAsDMS(location.latitude, 0) + " " +
                LocationConverter.longitudeAsDMS(location.longitude, 0)

    }

    override fun onPermissionDenied() {
        TODO("Not yet implemented")
    }

    override fun onSensorChanged(lastAzimuth: Float, _azimuth: Float) {

        var azimuth = _azimuth
        if (_azimuth.toDouble() <= 0.5) {
            azimuth += 360.0f
        }

        rotateCompass(lastAzimuth, azimuth)
        setHeading(mHeading, azimuth)
    }

    override fun onAccuracyChanged(accuracy: Int) {
        // if accuracy is 0, Sensor is not reliable. User should shake device in infinity path to reset magnet sensor.
    }


    fun rotateCompass(source : Float, destination : Float){
        val anim: Animation = RotateAnimation(-source, -destination, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
        anim.interpolator = LinearInterpolator()
        anim.fillAfter = true
        anim.duration = 250
        compassImage.startAnimation(anim)
    }


    private fun setHeading(tView: TextView?, degree: Float) {
        tView?.text = Util.updateTextDirection(degree)
    }
}
