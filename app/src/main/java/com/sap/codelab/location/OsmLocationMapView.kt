package com.sap.codelab.location

import android.content.Context
import android.util.AttributeSet
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

/**
 * [LocationMapView] implementation backed by OSMDroid.
 *
 * Can be placed directly in XML layouts. Initialises OSMDroid's user-agent from the
 * application package name and uses the MAPNIK tile source.
 */
class OsmLocationMapView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : MapView(context, attrs), LocationMapView {

    init {
        Configuration.getInstance().userAgentValue = context.packageName
        setTileSource(TileSourceFactory.MAPNIK)
        setMultiTouchControls(true)
    }

    override fun showLocation(latLng: LatLng) {
        val point = GeoPoint(latLng.latitude, latLng.longitude)
        controller.setZoom(15.0)
        controller.setCenter(point)
        overlays.clear()
        overlays.add(Marker(this).apply {
            position = point
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            infoWindow = null
        })
        invalidate()
    }
}
