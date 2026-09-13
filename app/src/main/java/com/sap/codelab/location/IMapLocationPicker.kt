package com.sap.codelab.location

import android.content.Context
import android.content.Intent

/**
 * Launches a map screen so the user can pick a geographic location, and parses the result.
 *
 * Abstracts the map library so callers are not coupled to aspecific map SDK.
 */
internal interface IMapLocationPicker {

    /**
     * Creates an [Intent] that starts the map location picker screen.
     *
     * @param context         context used to build the intent.
     * @param initialLocation optional starting position to center the map on when opened.
     * @return intent ready to be passed to [androidx.activity.result.ActivityResultLauncher].
     */
    fun createIntent(context: Context, initialLocation: LatLng? = null): Intent

    /**
     * Parses the location selected by the user from the activity result data.
     *
     * @param data the result [Intent] returned by the picker activity; may be null if the
     *             user cancelled.
     * @return the selected [LatLng], or null if the user cancelled or the data is invalid.
     */
    fun parseResult(data: Intent?): LatLng?
}
