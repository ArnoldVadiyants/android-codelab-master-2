package com.sap.codelab.notification

import com.sap.codelab.model.Memo

internal interface MemoNotificationManager {

    fun showLocationReminder(memo: Memo)
}
