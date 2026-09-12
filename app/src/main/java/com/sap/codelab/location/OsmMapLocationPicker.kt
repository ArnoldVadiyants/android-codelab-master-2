package com.sap.codelab.location

import android.content.Context
import android.content.Intent

internal class OsmMapLocationPicker : IMapLocationPicker {

    override fun createIntent(context: Context, initialLocation: LatLng?): Intent =
        Intent(context, OsmMapLocationPickerActivity::class.java).apply {
            initialLocation?.let {
                putExtra(OsmMapLocationPickerActivity.EXTRA_INITIAL_LATITUDE, it.latitude)
                putExtra(OsmMapLocationPickerActivity.EXTRA_INITIAL_LONGITUDE, it.longitude)
            }
        }

    override fun parseResult(data: Intent?): LatLng? {
        val lat = data?.getDoubleExtra(OsmMapLocationPickerActivity.EXTRA_LATITUDE, Double.NaN)
            ?.takeIf { !it.isNaN() } ?: return null
        val lng = data.getDoubleExtra(OsmMapLocationPickerActivity.EXTRA_LONGITUDE, Double.NaN)
            .takeIf { !it.isNaN() } ?: return null
        return LatLng(lat, lng)
    }
}
