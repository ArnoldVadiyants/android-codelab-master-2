package com.sap.codelab.location

import android.content.Intent
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent

internal class GmsGeofenceEventParser : IGeofenceEventParser {

    override fun parseTriggeringMemoIds(intent: Intent): List<Long> {
        val event = GeofencingEvent.fromIntent(intent) ?: return emptyList()
        if (event.hasError()) return emptyList()
        if (event.geofenceTransition != Geofence.GEOFENCE_TRANSITION_ENTER) return emptyList()
        return event.triggeringGeofences
            ?.mapNotNull { it.requestId.toLongOrNull() }
            ?: emptyList()
    }
}
