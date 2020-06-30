package com.anywhereapps.handlers

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*

class LocationHandler (context: Context, listener: LocationListener) {


    private val TAG = LocationHandler::class.java.simpleName

    private lateinit var mLocationRequest: LocationRequest
    private  var mFusedLocationClient: FusedLocationProviderClient
    private lateinit var mLocationCallback : LocationCallback

    private var mContext: Context
    private  var mLocationListener : LocationListener

    init {
        mContext = context
        mLocationListener = listener

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(mContext)
    }

    private val REQUEST_PERMISSIONS_REQUEST_CODE = 34

    private val UPDATE_INTERVAL: Long = 5000 // Every 10 seconds.
    private val FASTEST_UPDATE_INTERVAL: Long = 10000 // Every 10 seconds
    private val MAX_WAIT_TIME = UPDATE_INTERVAL * 2 // Ever


    interface LocationListener {

        fun onLocationChange(location: Location?)
        fun onPermissionDenied()
    }



     fun getLocation(){

        if(!checkPermissions())
            requestPermissions()

        createLocationRequest()
        registerLocationCallback()

        mFusedLocationClient.lastLocation.addOnSuccessListener { location : Location? ->
            println("LOCATION : ${location?.latitude} longitude : ${location?.longitude}")
            mLocationListener.onLocationChange(location)
        }

    }

    private fun createLocationRequest() {
        mLocationRequest = LocationRequest()
        mLocationRequest.setInterval(UPDATE_INTERVAL)
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL)
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
        mLocationRequest.setMaxWaitTime(MAX_WAIT_TIME)
    }

    private fun registerLocationCallback(){

        mLocationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                if (locationResult == null) {
                    return
                }
                for (location in locationResult.getLocations()) {
                    // Update UI with location data
                    // ...

                    mLocationListener.onLocationChange(location)
                    println("LOCATION : ${location.latitude} longitude : ${location.longitude}")

                }
            }
        }
    }


    /**
     * Return the current state of the permissions needed.
     */
    private fun checkPermissions(): Boolean {
        val permissionState = ActivityCompat.checkSelfPermission(mContext,
                Manifest.permission.ACCESS_FINE_LOCATION)
        return permissionState == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermissions() {
        val shouldProvideRationale = ActivityCompat.shouldShowRequestPermissionRationale(mContext as Activity,
                Manifest.permission.ACCESS_FINE_LOCATION)
        if (shouldProvideRationale) {
            Log.i(TAG, "Displaying permission rationale to provide additional context.")
//            Snackbar.make(
//                    findViewById(R.id.activity_main),
//                    R.string.permission_rationale,
//                    Snackbar.LENGTH_INDEFINITE)
//                    .setAction(R.string.ok, object : OnClickListener() {
//                        fun onClick(view: View?) { // Request permission
//                            ActivityCompat.requestPermissions(this@MainActivity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
//                                    REQUEST_PERMISSIONS_REQUEST_CODE)
//                        }
//                    })
//                    .show()
        } else {
            Log.i(TAG, "Requesting permission")
            ActivityCompat.requestPermissions(mContext as Activity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    REQUEST_PERMISSIONS_REQUEST_CODE)
        }
    }

     fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String?>,
                                            grantResults: IntArray) {
        Log.i(TAG, "onRequestPermissionResult")
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.size <= 0) {
                Log.i(TAG, "User interaction was cancelled.")
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) { // Permission was granted.
                requestLocationUpdates()
            } else {
                /*Snackbar.make(
                        findViewById(R.id.activity_main),
                        R.string.permission_denied_explanation,
                        Snackbar.LENGTH_INDEFINITE)
                        .setAction(R.string.settings, object : OnClickListener() {
                            fun onClick(view: View?) { // Build intent that displays the App settings screen.
                                val intent = Intent()
                                intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                                val uri: Uri = Uri.fromParts("package",
                                        BuildConfig.APPLICATION_ID, null)
                                intent.data = uri
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                startActivity(intent)
                            }
                        })
                        .show()*/
            }
        }
    }

    fun requestLocationUpdates() {
        try {
            Log.i(TAG, "Starting location updates")
            mFusedLocationClient!!.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.getMainLooper())
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }



    fun removeLocationUpdates() {
        Log.i(TAG, "Removing location updates")
        mFusedLocationClient.removeLocationUpdates(mLocationCallback)
    }
}