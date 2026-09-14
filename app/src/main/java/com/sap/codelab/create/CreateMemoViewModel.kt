package com.sap.codelab.create

import androidx.lifecycle.ViewModel
import com.sap.codelab.AppDependencies
import com.sap.codelab.core.location.LatLng
import com.sap.codelab.core.model.Memo
import com.sap.codelab.core.repository.Repository
import com.sap.codelab.core.utils.coroutines.ScopeProvider
import kotlinx.coroutines.launch

/**
 * ViewModel for matching CreateMemo view. Handles user interactions.
 */
internal class CreateMemoViewModel : ViewModel() {

    private var memo = Memo()
    private var selectedLocation: LatLng? = null

    val location: LatLng? get() = selectedLocation

    /**
     * Saves the memo and registers a geofence if a location was selected.
     */
    fun saveMemo() {
        ScopeProvider.application.launch {
            val savedId = Repository.saveMemo(memo)
            val location = selectedLocation
            if (location != null) {
                runCatching {
                    AppDependencies.locationReminderManager.addReminder(
                        memoId = savedId,
                        latitude = location.latitude,
                        longitude = location.longitude
                    )
                }
            }
        }
    }

    /**
     * Call this method to update the memo. This is usually needed when the user changed his input.
     */
    fun updateMemo(title: String, description: String) {
        memo = memo.copy(
            title = title,
            description = description,
            reminderLatitude = selectedLocation?.latitude ?: 0.0,
            reminderLongitude = selectedLocation?.longitude ?: 0.0
        )
    }

    /** Sets the location reminder for the memo being created. */
    fun updateLocation(latLng: LatLng) {
        selectedLocation = latLng
    }

    /** Removes any previously selected location so the memo is saved without a reminder. */
    fun clearLocation() {
        selectedLocation = null
    }

    /**
     * @return true if the title and content are not blank; false otherwise.
     */
    fun isMemoValid(): Boolean = memo.title.isNotBlank() && memo.description.isNotBlank()

    /**
     * @return true if the memo text is blank, false otherwise.
     */
    fun hasTextError() = memo.description.isBlank()

    /**
     * @return true if the memo title is blank, false otherwise.
     */
    fun hasTitleError() = memo.title.isBlank()
}
