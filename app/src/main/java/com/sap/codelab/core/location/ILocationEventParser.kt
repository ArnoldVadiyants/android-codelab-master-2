package com.sap.codelab.core.location

import android.content.Intent

/**
 * Parses raw location-trigger broadcast intents into memo identifiers.
 */
internal interface ILocationEventParser {

    /**
     * Extracts the IDs of memos whose location reminders were triggered by the given intent.
     *
     * @return memo IDs list
     */
    fun parseTriggeringMemoIds(intent: Intent): List<Long>
}
