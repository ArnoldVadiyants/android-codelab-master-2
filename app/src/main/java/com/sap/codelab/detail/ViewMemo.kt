package com.sap.codelab.detail

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.sap.codelab.R
import com.sap.codelab.core.location.LocationMapView
import com.sap.codelab.core.model.Memo
import com.sap.codelab.core.utils.KEY_MEMO_ID
import com.sap.codelab.core.utils.extensions.applyWindowInsets
import com.sap.codelab.databinding.ActivityViewMemoBinding
import kotlinx.coroutines.launch

/**
 * Activity that allows a user to see the details of a memo.
 */
internal class ViewMemo : AppCompatActivity() {

    private lateinit var binding: ActivityViewMemoBinding
    private lateinit var mapView: LocationMapView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewMemoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        mapView = binding.contentCreateMemo.locationMapView as LocationMapView
        applyWindowInsets(binding.root, binding.appBar)
        // Initialize views with the passed memo id
        val model = ViewModelProvider(this)[ViewMemoViewModel::class.java]
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                model.memo.collect { value ->
                    value?.let { memo ->
                        updateUI(memo)
                    }
                }
            }
        }
        if (savedInstanceState == null) {
            val id = intent.getLongExtra(KEY_MEMO_ID, -1)
            model.loadMemo(id)
        }
    }

    /**
     * Updates the UI with the given memo details.
     *
     * @param memo - the memo whose details are to be displayed.
     */
    private fun updateUI(memo: Memo) {
        setupTextFields(memo)
        updateLocationSection(memo)
    }

    private fun setupTextFields(memo: Memo) {
        binding.contentCreateMemo.run {
            memoTitle.setText(memo.title)
            memoTitle.isEnabled = false
            memoDescription.setText(memo.description)
            memoDescription.isEnabled = false
        }
    }

    private fun updateLocationSection(memo: Memo) {
        val location = memo.reminderLocation
        binding.contentCreateMemo.run {
            if (location != null) {
                locationEmptyContainer.visibility = View.GONE
                locationSelectedContainer.visibility = View.VISIBLE
                locationMapView.visibility = View.VISIBLE
                locationCoordinates.text = getString(R.string.location_coordinates, location.latitude, location.longitude)
                changeLocationButton.visibility = View.GONE
                clearLocationButton.visibility = View.GONE
                mapView.showLocation(location)
            } else {
                locationEmptyContainer.visibility = View.GONE
                locationSelectedContainer.visibility = View.GONE
                locationMapView.visibility = View.GONE
            }
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
}
