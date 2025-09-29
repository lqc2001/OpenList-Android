package com.openlist.android.data.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.openlist.android.data.error.GlobalErrorHandler
import android.net.NetworkCapabilities.*

class NetworkConnectivityMonitor private constructor(private val context: Context) {

    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private val globalErrorHandler = GlobalErrorHandler.getInstance(context)

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private val _connectionType = MutableStateFlow(ConnectionType.NONE)
    val connectionType: StateFlow<ConnectionType> = _connectionType.asStateFlow()

    private val _connectionQuality = MutableStateFlow(ConnectionQuality.POOR)
    val connectionQuality: StateFlow<ConnectionQuality> = _connectionQuality.asStateFlow()

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            updateNetworkStatus()
        }

        override fun onLost(network: Network) {
            updateNetworkStatus()
        }

        override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
            updateNetworkStatus(networkCapabilities)
        }
    }

    init {
        registerNetworkCallback()
        updateNetworkStatus()
    }

    private fun registerNetworkCallback() {
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NET_CAPABILITY_INTERNET)
            .addCapability(NET_CAPABILITY_VALIDATED)
            .build()

        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
    }

    private fun updateNetworkStatus() {
        val currentNetwork = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(currentNetwork)

        if (capabilities != null) {
            updateNetworkStatus(capabilities)
        } else {
            _isConnected.value = false
            _connectionType.value = ConnectionType.NONE
            _connectionQuality.value = ConnectionQuality.POOR
        }
    }

    private fun updateNetworkStatus(capabilities: NetworkCapabilities) {
        val hasInternet = capabilities.hasCapability(NET_CAPABILITY_INTERNET)
        val isValidated = capabilities.hasCapability(NET_CAPABILITY_VALIDATED)

        _isConnected.value = hasInternet && isValidated

        // 检测网络类型
        _connectionType.value = when {
            capabilities.hasTransport(TRANSPORT_WIFI) -> ConnectionType.WIFI
            capabilities.hasTransport(TRANSPORT_CELLULAR) -> ConnectionType.CELLULAR
            capabilities.hasTransport(TRANSPORT_ETHERNET) -> ConnectionType.ETHERNET
            else -> ConnectionType.UNKNOWN
        }

        // 评估网络质量
        _connectionQuality.value = when {
            capabilities.hasCapability(NET_CAPABILITY_NOT_METERED) -> ConnectionQuality.EXCELLENT
            capabilities.hasCapability(NET_CAPABILITY_TEMPORARILY_NOT_METERED) -> ConnectionQuality.GOOD
            capabilities.hasTransport(TRANSPORT_WIFI) -> ConnectionQuality.GOOD
            capabilities.hasTransport(TRANSPORT_ETHERNET) -> ConnectionQuality.EXCELLENT
            capabilities.hasTransport(TRANSPORT_CELLULAR) -> {
                // 移动网络默认质量评估
                // 由于signalStrength需要API 29+，这里使用保守估计
                ConnectionQuality.FAIR
            }
            else -> ConnectionQuality.POOR
        }
    }

    fun getCurrentNetworkInfo(): NetworkInfo {
        val currentNetwork = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(currentNetwork)

        return NetworkInfo(
            isConnected = _isConnected.value,
            connectionType = _connectionType.value,
            connectionQuality = _connectionQuality.value,
            hasInternet = capabilities?.hasCapability(NET_CAPABILITY_INTERNET) ?: false,
            isValidated = capabilities?.hasCapability(NET_CAPABILITY_VALIDATED) ?: false,
            isMetered = capabilities?.hasCapability(NET_CAPABILITY_NOT_METERED) == false
        )
    }

    fun isNetworkAvailableForApi(): Boolean {
        return _isConnected.value && _connectionQuality.value != ConnectionQuality.POOR
    }

    fun getNetworkRecommendation(): String {
        return when (_connectionQuality.value) {
            ConnectionQuality.EXCELLENT -> "网络连接优秀"
            ConnectionQuality.GOOD -> "网络连接良好"
            ConnectionQuality.FAIR -> "网络连接一般，可能影响体验"
            ConnectionQuality.POOR -> "网络连接较差，建议切换网络"
        }
    }

    fun unregister() {
        connectivityManager.unregisterNetworkCallback(networkCallback)
    }

    data class NetworkInfo(
        val isConnected: Boolean,
        val connectionType: ConnectionType,
        val connectionQuality: ConnectionQuality,
        val hasInternet: Boolean,
        val isValidated: Boolean,
        val isMetered: Boolean
    )

    enum class ConnectionType {
        NONE, WIFI, CELLULAR, ETHERNET, UNKNOWN
    }

    enum class ConnectionQuality {
        POOR, FAIR, GOOD, EXCELLENT
    }

    companion object {
        @Volatile
        private var instance: NetworkConnectivityMonitor? = null

        fun getInstance(context: Context): NetworkConnectivityMonitor {
            return instance ?: synchronized(this) {
                instance ?: NetworkConnectivityMonitor(context.applicationContext).also { instance = it }
            }
        }
    }

    // 添加context属性以便其他组件使用
    val appContext: Context get() = context.applicationContext

    // 提供公共访问器给诊断组件使用
    val contextForDiagnostics: Context get() = context.applicationContext
}