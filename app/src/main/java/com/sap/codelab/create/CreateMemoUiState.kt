package com.sap.codelab.create

import com.sap.codelab.core.location.LatLng

internal data class CreateMemoUiState(
    val location: LatLng? = null,
    val titleError: Boolean = false,
    val descriptionError: Boolean = false
)
