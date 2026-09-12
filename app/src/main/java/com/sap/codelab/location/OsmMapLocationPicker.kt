package com.sap.codelab.location

import android.content.Context
import android.content.Intent
import com.sap.codelab.EXTRA_LATITUDE
import com.sap.codelab.EXTRA_LONGITUDE

internal class OsmMapLocationPicker : IMapLocationPicker {

    override fun createIntent(context: Context, initialLocation: LatLng?): Intent =
        Intent(context, OsmMapLocationPickerActivity::class.java).apply {
            initialLocation?.let {
                putExtra(EXTRA_LATITUDE, it.latitude)
                putExtra(EXTRA_LONGITUDE, it.longitude)
            }
        }

    override fun parseResult(data: Intent?): LatLng? {
        val lat = data?.getDoubleExtra(EXTRA_LATITUDE, Double.NaN)
            ?.takeIf { !it.isNaN() } ?: return null
        val lng = data.getDoubleExtra(EXTRA_LONGITUDE, Double.NaN)
            .takeIf { !it.isNaN() } ?: return null
        return LatLng(lat, lng)
    }
}
