package com.sap.codelab.view.create

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.sap.codelab.R
import com.sap.codelab.databinding.ActivityCreateMemoBinding
import com.sap.codelab.location.IMapLocationPicker
import com.sap.codelab.location.LatLng
import com.sap.codelab.location.LocationMapView
import com.sap.codelab.location.OsmMapLocationPicker
import com.sap.codelab.utils.extensions.applyWindowInsets
import com.sap.codelab.utils.extensions.empty

/**
 * Activity that allows a user to create a new Memo.
 */
internal class CreateMemo : AppCompatActivity() {

    private lateinit var binding: ActivityCreateMemoBinding
    private lateinit var model: CreateMemoViewModel
    private lateinit var mapView: LocationMapView
    private val locationPicker: IMapLocationPicker = OsmMapLocationPicker()

    private val notificationPermissionLauncher = registerForActivityResult(RequestPermission()) {
        // Result ignored
    }

    private val backgroundLocationLauncher = registerForActivityResult(RequestPermission()) {
        // Android requires background location and POST_NOTIFICATIONS to be requested separately
        // and in sequence. Background location is requested first; regardless of the outcome
        // we proceed to ask for notification permission next.
        requestNotificationPermission()
    }

    private val mapPickerLauncher = registerForActivityResult(StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            locationPicker.parseResult(result.data)?.let { latLng ->
                model.updateLocation(latLng)
                showLocationSelected(latLng)
                showPermissionRationaleAndRequest()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateMemoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        mapView = binding.contentCreateMemo.locationMapView as LocationMapView
        model = ViewModelProvider(this)[CreateMemoViewModel::class.java]
        applyWindowInsets(binding.root, binding.appBar)

        setupLocationButtons()

        model.location?.let { showLocationSelected(it) } ?: showLocationEmpty()
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
            locationMapView.visibility = View.VISIBLE
            locationCoordinates.text = getString(R.string.location_coordinates, latLng.latitude, latLng.longitude)
        }
        mapView.showLocation(latLng)
    }

    private fun showLocationEmpty() {
        binding.contentCreateMemo.run {
            locationSelectedContainer.visibility = View.GONE
            locationMapView.visibility = View.GONE
            locationEmptyContainer.visibility = View.VISIBLE
        }
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
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
                memoDescriptionContainer.error =
                    getErrorMessage(model.hasTextError(), R.string.memo_text_empty_error)
            }
        }
    }

    private fun showPermissionRationaleAndRequest() {
        if (!needsAnyPermission()) {
            return
        }
        AlertDialog.Builder(this)
            .setTitle(R.string.permission_rationale_title)
            .setMessage(R.string.permission_rationale_message)
            .setPositiveButton(R.string.permission_rationale_confirm) { _, _ ->
                requestBackgroundLocationIfNeeded()
            }
            .show()
    }

    private fun needsAnyPermission(): Boolean {
        val needsBackgroundLocation = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED
        val needsNotification = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        return needsBackgroundLocation || needsNotification
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Log.d("CreateMemo", "Requesting notification permission")
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private fun requestBackgroundLocationIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            backgroundLocationLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
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