package com.sap.codelab.location

import android.content.Context
import android.content.Intent

internal interface IMapLocationPicker {
    fun createIntent(context: Context, initialLocation: LatLng? = null): Intent
    fun parseResult(data: Intent?): LatLng?
}
