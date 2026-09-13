package com.sap.codelab.notification

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.sap.codelab.model.Memo
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Test

class AndroidMemoNotificationManagerTest {

    private val context = mockk<Context>(relaxed = true)
    private val notifManager = mockk<NotificationManagerCompat>(relaxed = true)

    @Before
    fun setUp() {
        mockkStatic(ContextCompat::class)
        mockkStatic(NotificationManagerCompat::class)
        every { NotificationManagerCompat.from(any()) } returns notifManager
        every { ContextCompat.getDrawable(any(), any()) } returns null
        mockkStatic(PendingIntent::class)
        every { PendingIntent.getActivity(any(), any(), any(), any()) } returns mockk()
        mockkConstructor(NotificationCompat.Builder::class)
        val builderMock = mockk<NotificationCompat.Builder>(relaxed = true)
        every { anyConstructed<NotificationCompat.Builder>().setSmallIcon(any<Int>()) } returns builderMock
        every { context.getSystemService(NotificationManager::class.java) } returns mockk(relaxed = true)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `showLocationReminder posts notification`() {
        AndroidMemoNotificationManager(context).showLocationReminder(testMemo())

        verify(exactly = 1) { notifManager.notify(any<Int>(), any<Notification>()) }
    }

    @Test
    fun `showLocationReminder uses memo id as the notification id`() {
        AndroidMemoNotificationManager(context).showLocationReminder(testMemo(id = 7L))

        verify { notifManager.notify(7, any<Notification>()) }
    }

    private fun testMemo(
        id: Long = 1L,
        title: String = "Test Title",
        description: String = "Test description"
    ) = Memo(
        id = id,
        title = title,
        description = description,
        reminderDate = 0L,
        reminderLatitude = 52.0.toBits(),
        reminderLongitude = 13.0.toBits()
    )
}
