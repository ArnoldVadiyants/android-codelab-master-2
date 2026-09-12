package com.sap.codelab.notification

import android.Manifest
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
import com.sap.codelab.KEY_MEMO_ID
import com.sap.codelab.R
import com.sap.codelab.model.Memo
import com.sap.codelab.view.detail.ViewMemo

private const val CHANNEL_ID = "location_reminders"
private const val DESCRIPTION_MAX_LENGTH = 140

internal class AndroidMemoNotificationManager(private val context: Context) :
    MemoNotificationManager {

    init {
        createNotificationChannel()
    }

    override fun showLocationReminder(memo: Memo) {
        Log.d("ARNOLD", "Showing location reminder for memo: $memo")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.d("ARNOLD", "Notification permission not granted")
            return
        }

        val contentIntent = Intent(context, ViewMemo::class.java).apply {
            putExtra(KEY_MEMO_ID, memo.id)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingFlags =
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        val pendingIntent = PendingIntent.getActivity(
            context,
            memo.id.toInt(),
            contentIntent,
            pendingFlags
        )

        val largeIcon = ContextCompat
            .getDrawable(context, R.drawable.ic_location_filled)
            ?.toBitmap()

        Log.d("ARNOLD", "Showing notification for memo: $memo, largeIcon: $largeIcon")
        val description = memo.description.take(DESCRIPTION_MAX_LENGTH)

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_location_on)
            .setLargeIcon(largeIcon)
            .setContentTitle(memo.title)
            .setContentText(description)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(description)
            )
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(memo.id.toInt(), notification)
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
