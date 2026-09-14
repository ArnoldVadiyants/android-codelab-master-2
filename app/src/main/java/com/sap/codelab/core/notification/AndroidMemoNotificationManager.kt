package com.sap.codelab.core.notification

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import com.sap.codelab.R
import com.sap.codelab.core.model.Memo
import com.sap.codelab.core.utils.KEY_MEMO_ID
import com.sap.codelab.detail.ViewMemo

private const val CHANNEL_ID = "location_reminders"
private const val DESCRIPTION_MAX_LENGTH = 140

/**
 * [MemoNotificationManager] implementation
 *
 * @param context app context
 */
internal class AndroidMemoNotificationManager(private val context: Context) :
    MemoNotificationManager {

    init {
        createNotificationChannel()
    }

    override fun showLocationReminder(memo: Memo) {
        Log.d("NotificationManager", "Showing location reminder for memo: $memo")

        val hasNotificationPermission = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU || ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasNotificationPermission) {
            Log.d("NotificationManager", "Notification permission not granted")
            return
        }
        val pendingIntent = createPendingIntent(memo)
        val notification = buildNotification(memo, pendingIntent)
        Log.d("NotificationManager", "Showing notification for memo: $memo")
        NotificationManagerCompat.from(context).notify(memo.id.toInt(), notification)
    }

    // Use memo.id as the request code so each memo gets its own distinct PendingIntent
    private fun createPendingIntent(memo: Memo): PendingIntent {
        val contentIntent = Intent(context, ViewMemo::class.java).apply {
            putExtra(KEY_MEMO_ID, memo.id)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingFlags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        return PendingIntent.getActivity(context, memo.id.toInt(), contentIntent, pendingFlags)
    }

    private fun buildNotification(memo: Memo, pendingIntent: PendingIntent): Notification {
        val description = memo.description.take(DESCRIPTION_MAX_LENGTH)
        val largeIcon = ContextCompat
            .getDrawable(context, R.drawable.ic_location_filled)
            ?.toBitmap()
        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_location_on)
            .setLargeIcon(largeIcon)
            .setContentTitle(memo.title)
            .setContentText(description)
            .setStyle(NotificationCompat.BigTextStyle().bigText(description))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.notification_channel_location_reminders),
            NotificationManager.IMPORTANCE_DEFAULT
        )
        context.getSystemService(NotificationManager::class.java)
            .createNotificationChannel(channel)
    }
}
