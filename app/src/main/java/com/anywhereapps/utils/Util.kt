package com.anywhereapps.utils

import android.content.Context
import android.location.Address
import android.location.Geocoder
import java.io.IOException
import java.util.*

class Util {

    companion object {
        private const val DEGREE = "\u00b0"


         fun getAddressFromLocation(context: Context, latitude: Double, longitude: Double): String {
            val geocoder = Geocoder(context, Locale.ENGLISH)
            try {
                val addresses: List<Address> = geocoder.getFromLocation(latitude, longitude, 1)
                if (addresses.size > 0) {
                    val fetchedAddress: Address = addresses[0]


                    return fetchedAddress.getAddressLine(0)

//                for (i in 0 until fetchedAddress.getMaxAddressLineIndex()) {
//                    strAddress.append(fetchedAddress.getAddressLine(i)).append(" ")
//                }

                } else {
                    //   txtLocationAddress.setText("Searching Current Address")
                    return ""
                }
            } catch (e: IOException) {
                e.printStackTrace()
                // printToast("Could not get address..!")
                return ""
            }
        }


         fun updateTextDirection(degree: Float) : String {

            val deg =  degree.toInt()
            val value: String
            value = if (degree.toDouble() <= 22.5 || degree.toDouble() >= 337.5) {
                String.format("%s%s N", deg.toString(), DEGREE)
            } else if (degree.toDouble() > 22.5 && degree.toDouble() < 67.5) {
                String.format("%s%s NE", deg.toString(), DEGREE)
            } else if (degree.toDouble() in 67.5 .. 112.5) {
                String.format("%s%s E", deg.toString(), DEGREE)
            } else if (degree.toDouble() > 112.5 && degree.toDouble() < 157.5) {
                String.format("%s%s SE", deg.toString(), DEGREE)
            } else if (degree.toDouble() in 157.5 .. 202.5) {
                String.format("%s%s S", deg.toString(), DEGREE)
            } else if (degree.toDouble() > 202.5 && degree.toDouble() < 247.5) {
                String.format("%s%s SW", deg.toString(), DEGREE)
            } else if (degree.toDouble() in 247.5 .. 292.5) {
                String.format("%s%s W", deg.toString(), DEGREE)
            } else {
                String.format("%s%s NW", deg.toString(), DEGREE)
            }

            return value
        }

    }
}