package com.sap.codelab.view.create

import androidx.lifecycle.ViewModel
import com.sap.codelab.AppDependencies
import com.sap.codelab.location.LatLng
import com.sap.codelab.model.Memo
import com.sap.codelab.repository.Repository
import com.sap.codelab.utils.coroutines.ScopeProvider
import com.sap.codelab.utils.extensions.empty
import kotlinx.coroutines.launch

/**
 * ViewModel for matching CreateMemo view. Handles user interactions.
 */
internal class CreateMemoViewModel : ViewModel() {

    private var memo = Memo(0, String.empty(), String.empty(), 0, 0, 0, false)
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
        memo = Memo(
            title = title,
            description = description,
            id = 0,
            reminderDate = 0,
            // Coordinates are persisted as the raw bit-pattern of the Double so they fit in a
            // single Long column; 0L is the sentinel for "no location set"
            reminderLatitude = selectedLocation?.latitude?.toBits() ?: 0L,
            reminderLongitude = selectedLocation?.longitude?.toBits() ?: 0L,
            isDone = false
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
