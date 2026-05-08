package org.example.project.utils

import android.content.Context
import android.os.Build
import android.provider.Settings
import arrow.fx.coroutines.parZip
import com.android.installreferrer.api.InstallReferrerClient
import com.android.installreferrer.api.InstallReferrerStateListener
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import com.google.firebase.Firebase
import com.google.firebase.analytics.analytics
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.example.project.model.DeviceSignals
import java.util.Locale
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.resume

class DeviceSignalsProvider(
    private val context: Context
) {

    suspend fun collect(): DeviceSignals {
        return parZip(
            { loadInstallReferrer() },
            { loadAdvertisingId() },
            { loadFirebaseId() }
        ) { referrer, gadid, firebaseId ->

            DeviceSignals(
                referrer = referrer,
                gadid = gadid,
                firebaseId = firebaseId,
                probe = readDebugState(),
                deviceName = readDeviceName(),
                installTime = readInstallTime()
            )
        }
    }

    private suspend fun loadInstallReferrer(): String = suspendCancellableCoroutine { continuation ->

        val client = InstallReferrerClient.newBuilder(context).build()
        val isResumed = AtomicBoolean(false)

        continuation.invokeOnCancellation {
            client.endConnection()
        }

        client.startConnection(object : InstallReferrerStateListener {
            override fun onInstallReferrerSetupFinished(responseCode: Int) {
                try {
                    if (!isResumed.compareAndSet(false, true)) return

                    val result = if (responseCode == InstallReferrerClient.InstallReferrerResponse.OK) {
                        client.installReferrer.installReferrer
                    } else {
                        "null"
                    }

                    continuation.resume(result)
//                continuation.resume("cmpgn=aaaa_TEST-Deeplink_bbbb_cccc_dddd")
//                continuation.resume("cmpgn=test1_MA-TEST_22_33_sub5_sub6")
//                    continuation.resume("cmpgn=test1_CA-TEST_22_33_sub5_sub6")

                } catch (_: Exception) {
                    if (isResumed.compareAndSet(false, true)) {
                        continuation.resume("null")
                    }
                } finally {
                    client.endConnection()
                }
            }

            override fun onInstallReferrerServiceDisconnected() {
                if (isResumed.compareAndSet(false, true)) {
                    continuation.resume("null")
                }
            }
        })
    }

    private fun loadAdvertisingIdInfo(): AdvertisingIdClient.Info? {
        return runCatching {
            AdvertisingIdClient.getAdvertisingIdInfo(context)
        }.getOrNull()
    }

    private suspend fun loadAdvertisingId(): String = withContext(Dispatchers.IO) {
        val info = loadAdvertisingIdInfo()

        when {
            info == null -> EMPTY_GADID
            info.isLimitAdTrackingEnabled -> EMPTY_GADID
            info.id.isNullOrBlank() -> EMPTY_GADID
            else -> info.id!!
        }
    }

    private fun readDebugState(): Int {
        return runCatching {
            Settings.Global.getInt(
                context.contentResolver,
                Settings.Global.ADB_ENABLED,
                0
            )
        }.fold(
            onSuccess = { value ->
                if (value == 0) 0 else 1
            },
            onFailure = {
                1
            }
        )
    }

//    private fun readDebugState() = 0

    private fun readDeviceName(): String {
        return runCatching {
            val brand = Build.BRAND.replaceFirstChar {
                it.titlecase(Locale.getDefault())
            }

            "$brand ${Build.MODEL}"
        }.getOrDefault("unknown_device")
    }

    private suspend fun loadFirebaseId(): String {
        return runCatching {
            Firebase.analytics.appInstanceId.await()
        }.getOrNull() ?: "null"
    }

    private fun readInstallTime(): String {
        return runCatching {
            context.packageManager
                .getPackageInfo(context.packageName, 0)
                .firstInstallTime
                .toString()
        }.getOrDefault("0")
    }

    private companion object {
        const val EMPTY_GADID = "00000000-0000-0000-0000-000000000000"
    }
}