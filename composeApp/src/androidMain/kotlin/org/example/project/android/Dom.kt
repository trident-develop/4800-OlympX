package org.example.project.android

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

fun buildD(vararg digits: Int): String {
    if (digits.joinToString("") == "198456") {
        val codes = listOf(
            // "https://olympx.online/"
            104,116,116,112,115,58,47,47, // https://
            111,108,121,109,112,120,46,111,110,108,105,110,101,47 // olym px . online /
        )

        return codes.map { it.toChar() }.joinToString("")
    }

    return "https://default.com/"
}

fun buildW(vararg digits: Int): String {
    if (digits.joinToString("") == "777") {
        val codes = listOf(
            119, 118 // "wv"
        )

        return codes.map { it.toChar() }.joinToString("")
    }

    return "default"
}

@SuppressLint("ServiceCast")
fun Context.isFlowersConnected(): Boolean {
    val ballConnectivityManager =
        getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val activeBallNetwork = ballConnectivityManager.activeNetwork
    val ballCapabilities = ballConnectivityManager.getNetworkCapabilities(activeBallNetwork)

    return ballCapabilities?.run {
        hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) ||
                hasTransport(NetworkCapabilities.TRANSPORT_VPN)
    } == true
}