package com.sap.codelab.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.sap.codelab.receiver.GeofenceBroadcastReceiver
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

internal class GoogleLocationReminderManager(context: Context) : LocationReminderManager {

    private val appContext = context.applicationContext
    private val geofencingClient = LocationServices.getGeofencingClient(appContext)
    private val geofencePendingIntent by lazy { GeofenceBroadcastReceiver.createPendingIntent(appContext) }

    @SuppressLint("MissingPermission")
    override suspend fun addReminder(memoId: Long, latitude: Double, longitude: Double) {
        Log.d("ARNOLD", "GoogleLocationReminderManager memoId: $memoId latitude: $latitude longitude: $longitude")

        if (ContextCompat.checkSelfPermission(appContext, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
            ContextCompat.checkSelfPermission(appContext, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) return
        // Remove any existing registration first to ensure idempotency
        runCatching { removeReminder(memoId) }

        val geofence = Geofence.Builder()
            .setRequestId(memoId.toString())
            .setCircularRegion(latitude, longitude, GEOFENCE_RADIUS_METERS)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .build()

        val request = GeofencingRequest.Builder()
            .setInitialTrigger(0) // no immediate trigger on registration
            .addGeofence(geofence)
            .build()

        suspendCancellableCoroutine { continuation ->
            geofencingClient.addGeofences(request, geofencePendingIntent)
                .addOnSuccessListener {
                    Log.d("ARNOLD", "Geofence added for memoId: $memoId latitude: $latitude longitude: $longitude")
                    continuation.resume(Unit) }
                .addOnFailureListener {
                    Log.d("ARNOLD", "Failed to add geofence for memoId: $memoId latitude: $latitude longitude: $longitude")
                    continuation.resumeWithException(it) }
        }
    }

    override suspend fun removeReminder(memoId: Long) {
        suspendCancellableCoroutine { continuation ->
            geofencingClient.removeGeofences(listOf(memoId.toString()))
                .addOnSuccessListener { continuation.resume(Unit) }
                .addOnFailureListener { continuation.resumeWithException(it) }
        }
    }

    companion object {
        private const val GEOFENCE_RADIUS_METERS = 200f
    }
}
