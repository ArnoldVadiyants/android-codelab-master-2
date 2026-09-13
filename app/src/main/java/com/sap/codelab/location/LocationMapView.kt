package com.sap.codelab.location

interface LocationMapView {
    fun showLocation(latLng: LatLng)
    fun onResume()
    fun onPause()
}
