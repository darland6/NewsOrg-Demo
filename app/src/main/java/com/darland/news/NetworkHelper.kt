package com.darland.news

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkInfo
import android.os.Build
import com.darland.domain.ApiHelper

class NetworkHelper(private val context: Context) : ApiHelper {
    private var hasInternet = false

    init {
        initialize(context)
    }

    private fun initialize(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            (context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager)?.let {
                it.registerDefaultNetworkCallback(object : ConnectivityManager.NetworkCallback() {
                    @SuppressLint("StaticFieldLeak")
                    override fun onAvailable(network: Network) {
                        super.onAvailable(network)
                        hasInternet = true
                    }

                    override fun onLosing(network: Network, maxMsToLive: Int) {
                        super.onLosing(network, maxMsToLive)
                    }

                    override fun onLost(network: Network) {
                        super.onLost(network)
                        hasInternet = false
                    }

                    override fun onUnavailable() {
                        super.onUnavailable()
                        hasInternet = false
                    }
                })
            }
        }
    }

    override fun hasInternet(): Boolean {
        return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            var connected = false
            (context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager)?.let { cm ->
                val networks = cm.allNetworks
                networks.forEach { network ->
                    if (cm.getNetworkInfo(network)?.state == NetworkInfo.State.CONNECTED) connected =
                        true
                }
            }
            connected
        } else {
            hasInternet
        }
    }
}
