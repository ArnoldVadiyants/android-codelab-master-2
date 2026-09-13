package com.sap.codelab.location

import android.content.Intent

/**
 * Parses raw geofence broadcast intents into memo identifiers.
 *
 * Abstracts the underlying geofencing SDK so the broadcast receiver and workers
 * remain decoupled from the GMS implementation.
 */
internal interface IGeofenceEventParser {

    /**
     * Extracts the IDs of memos whose geofences were triggered by the given intent.
     *
     * @return memo IDs list
     */
    fun parseTriggeringMemoIds(intent: Intent): List<Long>
}
