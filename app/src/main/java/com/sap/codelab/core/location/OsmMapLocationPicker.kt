package com.sap.codelab.core.location

import android.content.Context
import android.content.Intent
import com.sap.codelab.core.utils.EXTRA_LATITUDE
import com.sap.codelab.core.utils.EXTRA_LONGITUDE

/**
 * [IMapLocationPicker] implementation that launches [OsmMapLocationPickerActivity].
 *
 * Passes an optional initial location via intent extras and reads the selected
 * coordinates back from the result intent.
 */
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
