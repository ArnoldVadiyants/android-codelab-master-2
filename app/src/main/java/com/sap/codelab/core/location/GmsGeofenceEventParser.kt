package com.sap.codelab.core.location

import android.content.Intent
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent

/**
 * [ILocationEventParser] implementation backed by GMS geofencing.
 *
 * Parses [com.google.android.gms.location.GeofencingEvent] from the broadcast intent,
 * filtering to ENTER transitions only and mapping each geofence request ID to a memo ID.
 */
internal class GmsGeofenceEventParser : ILocationEventParser {

    /**
     * @return memo IDs that had an ENTER transition;
     * empty list if the intent carries no relevant event, contains an error,
     * or is not an ENTER transition.
     * */
    override fun parseTriggeringMemoIds(intent: Intent): List<Long> {
        val event = GeofencingEvent.fromIntent(intent) ?: return emptyList()
        if (event.hasError()) return emptyList()
        if (event.geofenceTransition != Geofence.GEOFENCE_TRANSITION_ENTER) return emptyList()
        return event.triggeringGeofences
            ?.mapNotNull { it.requestId.toLongOrNull() }
            ?: emptyList()
    }
}
