package com.sap.codelab.location

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.sap.codelab.EXTRA_LATITUDE
import com.sap.codelab.EXTRA_LONGITUDE
import com.sap.codelab.R
import com.sap.codelab.databinding.ActivityOsmMapLocationPickerBinding
import com.sap.codelab.utils.extensions.applyWindowInsets
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

internal class OsmMapLocationPickerActivity : AppCompatActivity() {

    companion object {
        private const val DEFAULT_ZOOM = 5.0
        private const val LOCATION_ZOOM = 20.0
    }

    private lateinit var binding: ActivityOsmMapLocationPickerBinding
    private lateinit var myLocationOverlay: MyLocationNewOverlay
    private var selectedPoint: GeoPoint? = null
    private var marker: Marker? = null

    // Set when user taps "Open Settings" in the location-disabled dialog.
    // onResume re-checks and animates once location is available.
    private var pendingLocationCenter = false

    private val permissionLauncher = registerForActivityResult(RequestPermission()) { granted ->
        if (granted) {
            enableAndCenter()
        } else {
            Toast.makeText(this, R.string.location_permission_denied, Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Configuration.getInstance().userAgentValue = packageName
        binding = ActivityOsmMapLocationPickerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        applyWindowInsets(binding.root, binding.appBar)

        setupMap()
        restoreInitialLocationOrCenter()

        binding.myLocationFab.setOnClickListener { requestLocationOrCenter() }
    }

    private fun setupMap() {
        myLocationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(this), binding.mapView)

        binding.mapView.apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            controller.setZoom(DEFAULT_ZOOM)
            controller.setCenter(GeoPoint(0.0, 0.0))
            overlays.add(myLocationOverlay)
            overlays.add(MapEventsOverlay(object : MapEventsReceiver {
                override fun singleTapConfirmedHelper(p: GeoPoint): Boolean {
                    placeMarker(p)
                    return true
                }

                override fun longPressHelper(p: GeoPoint) = false
            }))
        }
    }

    private fun restoreInitialLocationOrCenter() {
        val lat = intent.getDoubleExtra(EXTRA_LATITUDE, Double.NaN)
        val lng = intent.getDoubleExtra(EXTRA_LONGITUDE, Double.NaN)
        if (!lat.isNaN() && !lng.isNaN()) {
            val point = GeoPoint(lat, lng)
            placeMarker(point)
            myLocationOverlay.enableMyLocation()
        } else {
            requestLocationOrCenter()
        }
    }

    private fun requestLocationOrCenter() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ) {
            enableAndCenter()
        } else {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun enableAndCenter() {
        if (!myLocationOverlay.enableMyLocation()) {
            showLocationDisabledDialog()
            return
        }
        val current = myLocationOverlay.myLocation
        if (current != null) {
            centerImmediately(current)
        } else {
            myLocationOverlay.runOnFirstFix {
                runOnUiThread {
                    myLocationOverlay.myLocation?.let { centerImmediately(it) }
                }
            }
        }
    }

    private fun centerImmediately(point: GeoPoint) {
        binding.mapView.controller.setZoom(LOCATION_ZOOM)
        binding.mapView.controller.setCenter(point)
    }

    private fun showLocationDisabledDialog() {
        AlertDialog.Builder(this)
            .setTitle(R.string.location_disabled_title)
            .setMessage(R.string.location_disabled_message)
            .setPositiveButton(R.string.location_disabled_open_settings) { _, _ ->
                pendingLocationCenter = true
                startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun placeMarker(point: GeoPoint) {
        selectedPoint = point
        marker?.let { binding.mapView.overlays.remove(it) }
        marker = Marker(binding.mapView).apply {

            position = point
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            title = getString(R.string.selected_location)
        }
        binding.mapView.overlays.add(marker)
        centerImmediately(point)
        binding.mapView.invalidate()
        invalidateOptionsMenu()
    }

    override fun onResume() {
        super.onResume()
        binding.mapView.onResume()

        if (pendingLocationCenter &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ) {
            if (myLocationOverlay.enableMyLocation()) {
                pendingLocationCenter = false
                myLocationOverlay.myLocation?.let {
                    centerImmediately(it)
                } ?: myLocationOverlay.runOnFirstFix {
                    runOnUiThread {
                        myLocationOverlay.myLocation?.let { centerImmediately(it) }
                    }
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        binding.mapView.onPause()
        myLocationOverlay.disableMyLocation()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_map_picker, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        menu.findItem(R.id.action_confirm_location)?.isEnabled = selectedPoint != null
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        android.R.id.home -> {
            setResult(RESULT_CANCELED)
            finish()
            true
        }
        R.id.action_confirm_location -> {
            val point = selectedPoint
            if (point != null) {
                val result = Intent().apply {
                    putExtra(EXTRA_LATITUDE, point.latitude)
                    putExtra(EXTRA_LONGITUDE, point.longitude)
                }
                setResult(RESULT_OK, result)
                finish()
            }
            true
        }
        else -> super.onOptionsItemSelected(item)
    }
}
