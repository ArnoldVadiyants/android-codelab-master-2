package com.sap.codelab.view.detail

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.sap.codelab.R
import com.sap.codelab.databinding.ActivityViewMemoBinding
import com.sap.codelab.model.Memo
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Marker

internal const val BUNDLE_MEMO_ID: String = "memoId"

/**
 * Activity that allows a user to see the details of a memo.
 */
internal class ViewMemo : AppCompatActivity() {

    private lateinit var binding: ActivityViewMemoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Configuration.getInstance().userAgentValue = packageName
        binding = ActivityViewMemoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        // Initialize views with the passed memo id
        val model = ViewModelProvider(this)[ViewMemoViewModel::class.java]
        lifecycleScope.launch {
            model.memo.collect { value ->
                value?.let { memo ->
                    updateUI(memo)
                }
            }
        }
        if (savedInstanceState == null) {
            val id = intent.getLongExtra(BUNDLE_MEMO_ID, -1)
            model.loadMemo(id)
        }
    }

    /**
     * Updates the UI with the given memo details.
     *
     * @param memo - the memo whose details are to be displayed.
     */
    private fun updateUI(memo: Memo) {
        binding.contentCreateMemo.run {
            memoTitle.setText(memo.title)
            memoTitle.isEnabled = false
            memoDescription.setText(memo.description)
            memoDescription.isEnabled = false

            val hasLocation = memo.reminderLatitude != 0L || memo.reminderLongitude != 0L
            if (hasLocation) {
                val lat = Double.fromBits(memo.reminderLatitude)
                val lng = Double.fromBits(memo.reminderLongitude)

                locationEmptyContainer.visibility = View.GONE
                locationSelectedContainer.visibility = View.VISIBLE
                locationMapPreview.visibility = View.VISIBLE
                locationCoordinates.text = getString(R.string.location_coordinates, lat, lng)

                changeLocationButton.visibility = View.GONE
                clearLocationButton.visibility = View.GONE

                locationMapPreview.apply {
                    setTileSource(TileSourceFactory.MAPNIK)
                    setMultiTouchControls(true)
                    val point = GeoPoint(lat, lng)
                    controller.setZoom(15.0)
                    controller.setCenter(point)
                    overlays.clear()
                    val marker = Marker(this)
                    marker.position = point
                    marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    marker.infoWindow = null
                    overlays.add(marker)
                    invalidate()
                }
            } else {
                locationEmptyContainer.visibility = View.GONE
                locationSelectedContainer.visibility = View.GONE
                locationMapPreview.visibility = View.GONE
            }
        }
    }

    override fun onResume() {
        super.onResume()
        binding.contentCreateMemo.locationMapPreview.onResume()
    }

    override fun onPause() {
        super.onPause()
        binding.contentCreateMemo.locationMapPreview.onPause()
    }
}