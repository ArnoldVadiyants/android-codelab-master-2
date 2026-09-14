package com.sap.codelab.create

import androidx.lifecycle.ViewModel
import com.sap.codelab.AppDependencies
import com.sap.codelab.core.location.LatLng
import com.sap.codelab.core.model.Memo
import com.sap.codelab.core.repository.Repository
import com.sap.codelab.core.utils.coroutines.ScopeProvider
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for matching CreateMemo view. Handles user interactions.
 */
internal class CreateMemoViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(CreateMemoUiState())
    val uiState: StateFlow<CreateMemoUiState> = _uiState.asStateFlow()

    private val _savedEvent = Channel<Unit>()
    val savedEvent: Flow<Unit> = _savedEvent.receiveAsFlow()

    /**
     * Validates the memo and saves it if valid; otherwise updates [uiState] with validation errors.
     */
    fun trySave(title: String, description: String) {
        val titleError = title.isBlank()
        val descriptionError = description.isBlank()
        if (!titleError && !descriptionError) {
            val location = _uiState.value.location
            persistMemo(
                memo = Memo(
                    title = title,
                    description = description,
                    reminderLatitude = location?.latitude ?: 0.0,
                    reminderLongitude = location?.longitude ?: 0.0
                ),
                location = location
            )
            _savedEvent.trySend(Unit)
        } else {
            _uiState.update { it.copy(titleError = titleError, descriptionError = descriptionError) }
        }
    }

    /**
     * Saves the memo and registers a geofence if a location was selected.
     */
    private fun persistMemo(memo: Memo, location: LatLng?) {
        ScopeProvider.application.launch {
            val savedId = Repository.saveMemo(memo)
            if (location != null) {
                AppDependencies.locationReminderManager.addReminder(
                    memoId = savedId,
                    latitude = location.latitude,
                    longitude = location.longitude
                )
            }
        }
    }

    /** Sets the location reminder for the memo being created. */
    fun updateLocation(latLng: LatLng) {
        _uiState.update { it.copy(location = latLng) }
    }

    /** Removes any previously selected location so the memo is saved without a reminder. */
    fun clearLocation() {
        _uiState.update { it.copy(location = null) }
    }
}
