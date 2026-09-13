package com.sap.codelab.location

import android.content.Intent

internal interface IGeofenceEventParser {
    fun parseTriggeringMemoIds(intent: Intent): List<Long>
}
