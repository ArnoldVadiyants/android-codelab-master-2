package com.sap.codelab.core.location

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GoogleLocationReminderManagerTest {

    private val context = mockk<Context>(relaxed = true)
    private lateinit var geofencingClient: GeofencingClient

    @Before
    fun setUp() {
        mockkStatic(LocationServices::class)
        mockkStatic(ContextCompat::class)
        mockkStatic(PendingIntent::class)
        geofencingClient = mockk(relaxed = true)
        every { context.applicationContext } returns context
        every { LocationServices.getGeofencingClient(any<Context>()) } returns geofencingClient
        every { PendingIntent.getBroadcast(any(), any(), any(), any()) } returns mockk()
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `addReminder does nothing when ACCESS_FINE_LOCATION is not granted`() = runTest {
        every {
            ContextCompat.checkSelfPermission(any(), Manifest.permission.ACCESS_FINE_LOCATION)
        } returns PackageManager.PERMISSION_DENIED

        GoogleLocationReminderManager(context).addReminder(1L, 52.0, 13.0)

        verify(exactly = 0) { geofencingClient.addGeofences(any(), any()) }
    }

    @Test
    fun `addReminder calls geofencing client when ACCESS_FINE_LOCATION is granted`() = runTest {
        every {
            ContextCompat.checkSelfPermission(any(), Manifest.permission.ACCESS_FINE_LOCATION)
        } returns PackageManager.PERMISSION_GRANTED
        setupSuccessTask()

        GoogleLocationReminderManager(context).addReminder(1L, 52.0, 13.0)

        verify(exactly = 1) { geofencingClient.addGeofences(any(), any()) }
    }

    @Test
    fun `removeReminder delegates to geofencing client with memo id as string`() = runTest {
        setupSuccessTask()

        GoogleLocationReminderManager(context).removeReminder(42L)

        verify { geofencingClient.removeGeofences(listOf("42")) }
    }

    private fun setupSuccessTask() {
        val task = mockk<Task<Void>>()
        every { task.addOnSuccessListener(any<OnSuccessListener<Void>>()) } answers {
            firstArg<OnSuccessListener<Void>>().onSuccess(null)
            task
        }
        every { task.addOnFailureListener(any()) } returns task
        every { geofencingClient.addGeofences(any(), any()) } returns task
        every { geofencingClient.removeGeofences(any<List<String>>()) } returns task
    }
}
