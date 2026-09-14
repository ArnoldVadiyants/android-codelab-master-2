package com.sap.codelab.core.location

/**
 * A map view component capable of displaying a single pinned location.
 *
 * Abstracts the underlying map SDK.
 */
interface LocationMapView {

    /**
     * Centers the map on [latLng] and places a marker at that position.
     */
    fun showLocation(latLng: LatLng)

    /**
     * Must be called from the host component's [android.app.Activity.onResume] to let the map
     * resume tile loading and sensor updates.
     */
    fun onResume()

    /**
     * Must be called from the host component's [android.app.Activity.onPause] to release map
     * resources and stop tile downloads.
     */
    fun onPause()
}
