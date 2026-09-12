package com.sap.codelab.view.create

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.sap.codelab.R
import com.sap.codelab.databinding.ActivityCreateMemoBinding
import com.sap.codelab.location.IMapLocationPicker
import com.sap.codelab.location.LatLng
import com.sap.codelab.location.OsmMapLocationPicker
import com.sap.codelab.utils.extensions.empty
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Marker

/**
 * Activity that allows a user to create a new Memo.
 */
internal class CreateMemo : AppCompatActivity() {

    private lateinit var binding: ActivityCreateMemoBinding
    private lateinit var model: CreateMemoViewModel
    private val locationPicker: IMapLocationPicker = OsmMapLocationPicker()

    private val mapPickerLauncher = registerForActivityResult(StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            locationPicker.parseResult(result.data)?.let { latLng ->
                model.updateLocation(latLng)
                showLocationSelected(latLng)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Configuration.getInstance().userAgentValue = packageName
        binding = ActivityCreateMemoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        model = ViewModelProvider(this)[CreateMemoViewModel::class.java]

        initPreviewMap()
        setupLocationButtons()

        model.location?.let { showLocationSelected(it) } ?: showLocationEmpty()
    }

    private fun initPreviewMap() {
        binding.contentCreateMemo.locationMapPreview.apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
        }
    }

    private fun setupLocationButtons() {
        binding.contentCreateMemo.run {
            pickLocationButton.setOnClickListener {
                mapPickerLauncher.launch(locationPicker.createIntent(this@CreateMemo, model.location))
            }
            changeLocationButton.setOnClickListener {
                mapPickerLauncher.launch(locationPicker.createIntent(this@CreateMemo, model.location))
            }
            clearLocationButton.setOnClickListener {
                model.clearLocation()
                showLocationEmpty()
            }
        }
    }

    private fun showLocationSelected(latLng: LatLng) {
        binding.contentCreateMemo.run {
            locationEmptyContainer.visibility = View.GONE
            locationSelectedContainer.visibility = View.VISIBLE
            locationMapPreview.visibility = View.VISIBLE
            locationCoordinates.text = getString(R.string.location_coordinates, latLng.latitude, latLng.longitude)

            val point = GeoPoint(latLng.latitude, latLng.longitude)
            locationMapPreview.apply {
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
        }
    }

    private fun showLocationEmpty() {
        binding.contentCreateMemo.run {
            locationSelectedContainer.visibility = View.GONE
            locationMapPreview.visibility = View.GONE
            locationEmptyContainer.visibility = View.VISIBLE
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

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_create_memo, menu)
        return true
    }

    /**
     * Handles actionbar interactions.
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_save -> {
                saveMemo()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    /**
     * Saves the memo if the input is valid; otherwise shows the corresponding error messages.
     */
    private fun saveMemo() {
        binding.contentCreateMemo.run {
            model.updateMemo(memoTitle.text.toString(), memoDescription.text.toString())
            if (model.isMemoValid()) {
                model.saveMemo()
                setResult(RESULT_OK)
                finish()
            } else {
                memoTitleContainer.error =
                    getErrorMessage(model.hasTitleError(), R.string.memo_title_empty_error)
                memoDescription.error =
                    getErrorMessage(model.hasTextError(), R.string.memo_text_empty_error)
            }
        }
    }

    /**
     * Returns the error message if there is an error, or an empty string otherwise.
     *
     * @param hasError          - whether there is an error.
     * @param errorMessageResId - the resource id of the error message to show.
     * @return the error message if there is an error, or an empty string otherwise.
     */
    private fun getErrorMessage(hasError: Boolean, @StringRes errorMessageResId: Int): String {
        return if (hasError) {
            getString(errorMessageResId)
        } else {
            String.empty()
        }
    }
}