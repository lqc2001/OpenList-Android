# AList Android 技术架构文档

## 📋 文档信息
- **版本**: 1.0.0
- **最后更新**: 2025年9月28日
- **维护者**: AList Android Team

---

## 🏗️ 总体架构

### 架构模式
采用 **MVVM (Model-View-ViewModel)** 架构模式，结合 **Clean Architecture** 原则：

```
┌─────────────────────────────────────────────────────────────────┐
│                        Presentation Layer                       │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  ┌─────────┐ │
│  │LoginScreen  │  │FileListScreen│  │VideoPlayer  │  │Settings│ │
│  │   (View)    │  │    (View)    │  │  Screen     │  │ Screen │ │
│  └─────────────┘  └─────────────┘  └─────────────┘  └─────────┘ │
├─────────────────────────────────────────────────────────────────┤
│                      ViewModel Layer                            │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  ┌─────────┐ │
│  │AuthViewModel│  │FileViewModel │  │PlayerViewModel│ │HistoryVM│ │
│  └─────────────┘  └─────────────┘  └─────────────┘  └─────────┘ │
├─────────────────────────────────────────────────────────────────┤
│                     Domain Layer                                │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐               │
│  │  Use Cases  │  │  Repository │  │   Entities  │               │
│  └─────────────┘  └─────────────┘  └─────────────┘               │
├─────────────────────────────────────────────────────────────────┤
│                       Data Layer                                 │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  ┌─────────┐ │
│  │AList API    │  │  Database   │  │Preferences │  │Security │ │
│  │(Remote)     │  │  (Local)    │  │   (Local)   │  │ Module  │ │
│  └─────────────┘  └─────────────┘  └─────────────┘  └─────────┘ │
└─────────────────────────────────────────────────────────────────┘
```

### 架构原则

1. **单一数据源 (Single Source of Truth)**
   - 每个数据源只有一个权威提供者
   - 使用 Flow/RxJava 确保数据一致性

2. **依赖倒置 (Dependency Inversion)**
   - 高层模块不依赖低层模块
   - 接口编程，面向抽象

3. **关注点分离 (Separation of Concerns)**
   - UI 只负责显示和用户交互
   - ViewModel 处理业务逻辑
   - Data Layer 处理数据获取和存储

4. **响应式编程 (Reactive Programming)**
   - 使用 Kotlin Flow 处理异步数据流
   - 状态驱动 UI 更新

---

## 📁 项目结构

### 目录结构详解
```
app/src/main/java/com/alist/android/
├── AListApplication.kt              # 应用程序入口
├── MainActivity.kt                  # 主活动
│
├── data/                            # 数据层
│   ├── api/                         # API 接口定义
│   │   ├── AListApiService.kt      # AList API 服务
│   │   └── model/                   # API 数据模型
│   │       ├── FileModels.kt
│   │       └── AuthModels.kt
│   ├── database/                    # 本地数据库
│   │   ├── AppDatabase.kt           # 数据库定义
│   │   ├── dao/                     # 数据访问对象
│   │   │   ├── PlayHistoryDao.kt
│   │   │   └── FileInfoDao.kt
│   │   ├── entity/                   # 数据库实体
│   │   │   ├── PlayHistoryEntity.kt
│   │   │   └── FileInfoEntity.kt
│   │   └── DatabaseModule.kt         # 数据库模块
│   ├── error/                       # 错误处理
│   │   ├── GlobalErrorHandler.kt    # 全局错误处理器
│   │   ├── ErrorHandlingUtils.kt    # 错误处理工具
│   │   ├── AListException.kt        # 自定义异常
│   │   └── NetworkException.kt      # 网络异常
│   ├── model/                       # 数据模型
│   │   ├── FileInfo.kt               # 文件信息模型
│   │   ├── PlayHistory.kt            # 播放历史模型
│   │   └── UserModels.kt             # 用户模型
│   ├── network/                     # 网络模块
│   │   ├── NetworkModule.kt         # 网络配置
│   │   ├── NetworkConnectivityMonitor.kt  # 网络监控
│   │   └── interceptor/              # 拦截器
│   │       └── AuthInterceptor.kt
│   ├── preferences/                 # 偏好设置
│   │   ├── PreferencesRepository.kt # 偏好设置仓库
│   │   ├── DataStoreModule.kt       # DataStore 配置
│   │   └── model/                   # 偏好设置模型
│   └── security/                    # 安全模块
│       ├── SecurityHelper.kt        # 安全助手
│       └── crypto/                   # 加密工具
│
├── player/                          # 播放器模块
│   ├── core/                        # 核心播放器
│   │   ├── SimpleExoPlayerManager.kt    # ExoPlayer 管理器
│   │   ├── PlaybackState.kt         # 播放状态
│   │   └── AudioTrackManager.kt      # 音轨管理
│   └── service/                     # 播放器服务
│       └── PlayerService.kt         # 后台播放服务
│
├── ui/                              # UI 层
│   ├── component/                   # UI 组件
│   │   ├── NetworkStatusIndicator.kt    # 网络状态指示器
│   │   ├── FileItem.kt              # 文件项组件
│   │   ├── PlayerControls.kt        # 播放控制器
│   │   └── LoadingIndicator.kt      # 加载指示器
│   ├── screen/                      # 屏幕页面
│   │   ├── LoginScreen.kt           # 登录界面
│   │   ├── FileListScreen.kt        # 文件列表界面
│   │   ├── VideoPlayerScreen.kt     # 视频播放界面
│   │   ├── PlayHistoryScreen.kt     # 播放历史界面
│   │   └── SettingsScreen.kt        # 设置界面
│   ├── theme/                       # 主题样式
│   │   ├── Theme.kt                 # 主题配置
│   │   ├── Color.kt                 # 颜色定义
│   │   └── Type.kt                  # 字体定义
│   └── viewmodel/                   # 视图模型
│       ├── SimpleAuthViewModel.kt   # 认证视图模型
│       ├── SimpleFileViewModel.kt   # 文件视图模型
│       ├── SimplePlayerViewModel.kt # 播放器视图模型
│       └── SimplePlayHistoryViewModel.kt # 历史记录视图模型
│
└── utils/                           # 工具类
    ├── Extensions.kt                # 扩展函数
    ├── Constants.kt                 # 常量定义
    └── DateUtils.kt                 # 日期工具
```

---

## 🔧 核心模块详解

### 1. 数据层 (Data Layer)

#### 1.1 网络模块 (`data/network`)

**NetworkConnectivityMonitor.kt**
```kotlin
class NetworkConnectivityMonitor private constructor(private val context: Context) {

    // 网络状态流
    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    // 连接类型
    private val _connectionType = MutableStateFlow(ConnectionType.NONE)
    val connectionType: StateFlow<ConnectionType> = _connectionType.asStateFlow()

    // 连接质量
    private val _connectionQuality = MutableStateFlow(ConnectionQuality.POOR)
    val connectionQuality: StateFlow<ConnectionQuality> = _connectionQuality.asStateFlow()

    // 网络质量评估
    private fun updateNetworkStatus(networkCapabilities: NetworkCapabilities?) {
        when {
            capabilities.hasCapability(NET_CAPABILITY_NOT_METERED) ->
                _connectionQuality.value = ConnectionQuality.EXCELLENT
            capabilities.hasTransport(TRANSPORT_WIFI) ->
                _connectionQuality.value = ConnectionQuality.GOOD
            capabilities.hasTransport(TRANSPORT_CELLULAR) ->
                _connectionQuality.value = ConnectionQuality.FAIR
            else -> _connectionQuality.value = ConnectionQuality.POOR
        }
    }
}
```

**NetworkModule.kt**
```kotlin
object NetworkModule {

    fun provideOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor,
        preferencesRepository: PreferencesRepository
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(preferencesRepository))
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    fun provideRetrofit(
        okHttpClient: OkHttpClient,
        gson: Gson
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://your-alist-server.com/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }
}
```

#### 1.2 错误处理模块 (`data/error`)

**GlobalErrorHandler.kt**
```kotlin
class GlobalErrorHandler(private val context: Context) {

    fun getCoroutineExceptionHandler(): CoroutineExceptionHandler {
        return CoroutineExceptionHandler { _, throwable ->
            handleError(throwable)
        }
    }

    fun handleError(throwable: Throwable) {
        val errorMessage = when (throwable) {
            is UnknownHostException -> "无法连接到服务器"
            is SocketTimeoutException -> "连接超时"
            is SSLException -> "安全连接失败"
            else -> "发生未知错误"
        }

        // 显示错误提示
        CoroutineScope(Dispatchers.Main).launch {
            Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
        }
    }

    fun validateNetworkState(): Boolean {
        return try {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val network = connectivityManager.activeNetwork
            val capabilities = connectivityManager.getNetworkCapabilities(network)
            capabilities != null &&
            (capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
             capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED))
        } catch (e: Exception) {
            false
        }
    }
}
```

#### 1.3 安全模块 (`data/security`)

**SecurityHelper.kt**
```kotlin
class SecurityHelper(private val context: Context) {

    private val keyStore = KeyStore.getInstance("AndroidKeyStore")
    private val masterKeyAlias = "alist_master_key"

    init {
        keyStore.load(null)
        ensureMasterKeyExists()
    }

    private fun ensureMasterKeyExists() {
        if (!keyStore.containsAlias(masterKeyAlias)) {
            val keyGenerator = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore"
            )

            val spec = KeyGenParameterSpec.Builder(
                masterKeyAlias,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setUserAuthenticationRequired(false)
                .build()

            keyGenerator.init(spec)
            keyGenerator.generateKey()
        }
    }

    fun encrypt(data: String): String {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, getSecretKey())

        val encryptedData = cipher.doFinal(data.toByteArray())
        return Base64.encodeToString(encryptedData, Base64.DEFAULT)
    }

    fun decrypt(encryptedData: String): String {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, getSecretKey())

        val decodedData = Base64.decode(encryptedData, Base64.DEFAULT)
        val decryptedData = cipher.doFinal(decodedData)
        return String(decryptedData)
    }

    private fun getSecretKey(): SecretKey {
        return keyStore.getKey(masterKeyAlias, null) as SecretKey
    }
}
```

### 2. 播放器模块 (`player`)

#### 2.1 核心播放器

**SimpleExoPlayerManager.kt**
```kotlin
class SimpleExoPlayerManager(private val context: Context) {

    private var exoPlayer: ExoPlayer? = null
    private val _playbackState = MutableStateFlow(PlaybackState.IDLE)
    val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

    private val _audioTracks = MutableStateFlow<List<String>>(emptyList())
    val audioTracks: StateFlow<List<String>> = _audioTracks.asStateFlow()

    private val _selectedAudioTrack = MutableStateFlow<String?>(null)
    val selectedAudioTrack: StateFlow<String?> = _selectedAudioTrack.asStateFlow()

    init {
        initializePlayer()
    }

    private fun initializePlayer() {
        exoPlayer = ExoPlayer.Builder(context)
            .setMediaItemFactory(DefaultMediaItemFactory.Builder().build())
            .setTrackSelector(DefaultTrackSelector(context))
            .setLoadControl(DefaultLoadControl.Builder()
                .setBufferDurationsMs(
                    32 * 1000,  // min buffer
                    64 * 1000,  // max buffer
                    1024,       // buffer for playback
                    1024        // buffer for replay
                ).build()
            ).build()

        exoPlayer?.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                _playbackState.value = when (playbackState) {
                    Player.STATE_IDLE -> PlaybackState.IDLE
                    Player.STATE_BUFFERING -> PlaybackState.BUFFERING
                    Player.STATE_READY -> PlaybackState.READY
                    Player.STATE_ENDED -> PlaybackState.ENDED
                    else -> PlaybackState.IDLE
                }
            }

            override fun onTracksChanged(tracks: Tracks) {
                updateAudioTracks(tracks)
            }
        })
    }

    fun preparePlayer(mediaUrl: String) {
        val mediaItem = MediaItem.Builder()
            .setUri(mediaUrl)
            .setMimeType(getMimeTypeFromUrl(mediaUrl))
            .build()

        exoPlayer?.setMediaItem(mediaItem)
        exoPlayer?.prepare()
    }

    fun play() {
        exoPlayer?.play()
        _playbackState.value = PlaybackState.PLAYING
    }

    fun pause() {
        exoPlayer?.pause()
        _playbackState.value = PlaybackState.PAUSED
    }

    fun seekTo(positionMs: Long) {
        exoPlayer?.seekTo(positionMs)
    }

    fun getCurrentPosition(): Long {
        return exoPlayer?.currentPosition ?: 0L
    }

    fun getDuration(): Long {
        return exoPlayer?.duration ?: 0L
    }

    fun isPlaying(): Boolean {
        return exoPlayer?.isPlaying == true
    }

    fun setPlaybackSpeed(speed: Float) {
        exoPlayer?.setPlaybackSpeed(speed)
    }

    fun selectAudioTrack(track: String) {
        exoPlayer?.let { player ->
            val trackSelector = player.trackSelector as DefaultTrackSelector
            val trackParameters = trackSelector.parameters
                .buildUpon()
                .setOverrideForType(
                    TrackSelectionOverride(C.FORMAT_TYPE_AUDIO, track)
                )
                .build()
            trackSelector.setParameters(trackParameters)
            _selectedAudioTrack.value = track
        }
    }

    private fun updateAudioTracks(tracks: Tracks) {
        val audioTrackList = tracks.groups
            .filter { it.type == C.TRACK_TYPE_AUDIO }
            .flatMap { group ->
                group.tracks.map {
                    "${it.format.language ?: "未知"} (${it.format.channelCount}声道)"
                }
            }

        _audioTracks.value = audioTrackList
        if (_selectedAudioTrack.value == null && audioTrackList.isNotEmpty()) {
            _selectedAudioTrack.value = audioTrackList[0]
        }
    }

    fun release() {
        exoPlayer?.release()
        exoPlayer = null
    }

    companion object {
        fun getMimeTypeFromUrl(url: String): String {
            return when {
                url.endsWith(".mp4") -> "video/mp4"
                url.endsWith(".mkv") -> "video/x-matroska"
                url.endsWith(".avi") -> "video/x-msvideo"
                url.endsWith(".mp3") -> "audio/mpeg"
                url.endsWith(".wav") -> "audio/wav"
                url.endsWith(".flac") -> "audio/flac"
                else -> "*/*"
            }
        }
    }
}
```

### 3. UI 层 (`ui`)

#### 3.1 视图模型

**SimpleAuthViewModel.kt**
```kotlin
class SimpleAuthViewModel(
    val application: Application
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Initial)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _savedCredentials = MutableStateFlow<SavedCredentials?>(null)
    val savedCredentials: StateFlow<SavedCredentials?> = _savedCredentials.asStateFlow()

    // 全局错误处理器
    private val globalErrorHandler = GlobalErrorHandler.getInstance(application)

    // 网络连接监控器
    private val networkMonitor = NetworkConnectivityMonitor.getInstance(application)

    // 数据存储和网络组件
    private val preferencesRepository: PreferencesRepository by lazy {
        DataStoreModule.providePreferencesRepository(
            DataStoreModule.provideDataStore(application),
            application
        )
    }

    private val apiService: AListApiService by lazy {
        NetworkModule.provideAListApiService(
            NetworkModule.provideRetrofit(
                NetworkModule.provideOkHttpClient(NetworkModule.provideHttpLoggingInterceptor()),
                NetworkModule.provideGson(),
                preferencesRepository
            )
        )
    }

    init {
        loadSavedCredentials()
    }

    fun login(username: String, password: String, rememberMe: Boolean) {
        viewModelScope.launch(globalErrorHandler.getCoroutineExceptionHandler()) {
            _authState.value = AuthState.Loading

            // 验证输入参数
            val validationResult = ErrorHandlingUtils.validateRequiredFields(
                mapOf("username" to username, "password" to password)
            )

            if (validationResult is ValidationResult.Error) {
                _authState.value = AuthState.Error(validationResult.exception.message ?: "输入参数错误")
                return@launch
            }

            // 检查网络连接
            if (!networkMonitor.isConnected.value) {
                val networkInfo = networkMonitor.getCurrentNetworkInfo()
                val errorMessage = when {
                    !networkInfo.hasInternet -> "设备未连接到网络"
                    !networkInfo.isValidated -> "网络连接已建立，但无法访问互联网"
                    else -> "网络连接失败，请检查网络设置"
                }
                _authState.value = AuthState.Error(errorMessage)
                return@launch
            }

            // 检查网络质量
            if (!networkMonitor.isNetworkAvailableForApi()) {
                val recommendation = networkMonitor.getNetworkRecommendation()
                _authState.value = AuthState.Error(recommendation)
                return@launch
            }

            val serverUrl = preferencesRepository.getServerUrl().first() ?: "http://localhost:5244"

            // 使用安全API调用
            ErrorHandlingUtils.safeApiCall(application) {
                val loginRequest = LoginRequest(username, password)
                apiService.login(loginRequest)
            }.collect { result ->
                result.fold(
                    onSuccess = { response ->
                        if (response.isSuccessful) {
                            val loginResponse = response.body()
                            if (loginResponse != null) {
                                // 保存认证信息
                                preferencesRepository.saveAuthToken(loginResponse.token)
                                preferencesRepository.saveUsername(username)
                                preferencesRepository.saveRememberMe(rememberMe)

                                if (rememberMe) {
                                    preferencesRepository.savePassword(password)
                                } else {
                                    preferencesRepository.savePassword("")
                                }

                                _savedCredentials.value = SavedCredentials(
                                    serverUrl = serverUrl,
                                    username = username,
                                    password = if (rememberMe) password else "",
                                    authToken = loginResponse.token,
                                    rememberMe = rememberMe,
                                    refreshToken = ""
                                )

                                _authState.value = AuthState.Authenticated
                            } else {
                                _authState.value = AuthState.Error("登录响应为空")
                            }
                        } else {
                            val errorMessage = globalErrorHandler.getApiErrorMessage(response.code())
                            _authState.value = AuthState.Error(errorMessage)
                        }
                    },
                    onFailure = { exception ->
                        val errorMessage = when (exception) {
                            is AuthenticationException -> "用户名或密码错误"
                            is NetworkException -> globalErrorHandler.getNetworkErrorMessage(exception)
                            else -> "登录失败: ${exception.message}"
                        }
                        _authState.value = AuthState.Error(errorMessage)
                    }
                )
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(
                modelClass: Class<T>,
                extras: CreationExtras
            ): T {
                val application = extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as Application
                return SimpleAuthViewModel(application) as T
            }
        }
    }
}
```

#### 3.2 UI 组件

**NetworkStatusIndicator.kt**
```kotlin
@Composable
fun NetworkStatusIndicator(
    networkMonitor: NetworkConnectivityMonitor,
    modifier: Modifier = Modifier
) {
    val isConnected by networkMonitor.isConnected.collectAsStateWithLifecycle()
    val connectionType by networkMonitor.connectionType.collectAsStateWithLifecycle()
    val connectionQuality by networkMonitor.connectionQuality.collectAsStateWithLifecycle()

    if (!isConnected) {
        Card(
            modifier = modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFFFEBEE)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.WifiOff,
                    contentDescription = "网络断开",
                    tint = Color.Red
                )
                Text(
                    text = "网络连接已断开",
                    color = Color.Red,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    } else {
        // 显示网络质量指示器
        val (icon, tint, text) = when (connectionQuality) {
            NetworkConnectivityMonitor.ConnectionQuality.EXCELLENT ->
                Triple(Icons.Default.Wifi, Color.Green, "网络优秀")
            NetworkConnectivityMonitor.ConnectionQuality.GOOD ->
                Triple(Icons.Default.Wifi, Color(0xFF4CAF50), "网络良好")
            NetworkConnectivityMonitor.ConnectionQuality.FAIR ->
                Triple(Icons.Default.Wifi, Color(0xFFFF9800), "网络一般")
            NetworkConnectivityMonitor.ConnectionQuality.POOR ->
                Triple(Icons.Default.Wifi, Color.Red, "网络较差")
        }

        val typeText = when (connectionType) {
            NetworkConnectivityMonitor.ConnectionType.WIFI -> "WiFi"
            NetworkConnectivityMonitor.ConnectionType.CELLULAR -> "移动网络"
            NetworkConnectivityMonitor.ConnectionType.ETHERNET -> "有线网络"
            else -> "未知网络"
        }

        Card(
            modifier = modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFF5F5F5)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = text,
                    tint = tint,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "$typeText · $text",
                    color = Color.Gray,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
```

---

## 🔄 数据流设计

### 状态管理模式

采用 **单向数据流 (UDF)** 模式：

```
User Action → ViewModel → State Update → UI Update
     ↓
   Repository
     ↓
   Data Source
```

### 状态流示例

**文件浏览状态流:**
```kotlin
// ViewModel 层
private val _files = MutableStateFlow<List<FileInfo>>(emptyList())
val files: StateFlow<List<FileInfo>> = _files.asStateFlow()

private val _isLoading = MutableStateFlow(false)
val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

private val _error = MutableStateFlow<String?>(null)
val error: StateFlow<String?> = _error.asStateFlow()

// UI 层
val files by viewModel.files.collectAsState()
val isLoading by viewModel.isLoading.collectAsState()
val error by viewModel.error.collectAsState()

// 用户交互
Button(onClick = { viewModel.refreshFiles() }) {
    if (isLoading) {
        CircularProgressIndicator()
    } else {
        LazyColumn {
            items(files) { file ->
                FileItem(file)
            }
        }
    }

    error?.let { errorMessage ->
        ErrorMessage(message = errorMessage)
    }
}
```

---

## 🚀 性能优化

### 1. 内存优化
- 使用 **LazyColumn** 进行列表虚拟化
- **ExoPlayer** 实例复用
- **图片加载** 使用 Coil 库进行内存缓存
- **协程** 正确管理生命周期

### 2. 网络优化
- **HTTP 缓存** 策略
- **请求合并** 减少网络调用
- **数据预加载** 提升用户体验
- **离线模式** 部分功能支持

### 3. 播放器优化
- **自适应码率** 根据网络质量调整
- **预加载策略** 平滑播放体验
- **内存管理** 及时释放资源

---

## 🔒 安全架构

### 数据安全
- **Android Keystore** 存储敏感信息
- **加密存储** 用户凭据和令牌
- **安全传输** HTTPS + 证书校验
- **权限最小化** 原则

### 认证安全
- **Bearer Token** 无状态认证
- **令牌刷新** 自动过期处理
- **会话管理** 安全超时机制
- **设备绑定** 防止令牌泄露

---

## 📊 监控与分析

### 性能监控
- **启动时间** 优化冷启动
- **内存使用** 监控内存泄漏
- **电池消耗** 优化后台任务
- **网络性能** 监控API响应

### 错误监控
- **全局异常** 捕获和上报
- **网络错误** 分类统计
- **播放错误** 诊断和优化
- **用户反馈** 问题追踪

---

## 🧪 测试策略

### 单元测试
- **ViewModel** 逻辑测试
- **Repository** 数据层测试
- **Utility** 工具类测试
- **Use Case** 业务逻辑测试

### 集成测试
- **API 调用** 集成测试
- **数据库操作** 数据持久化测试
- **播放器功能** 媒体播放测试
- **认证流程** 完整流程测试

### UI 测试
- **Compose UI** 组件测试
- **用户交互** 操作流程测试
- **屏幕导航** 页面跳转测试
- **无障碍** 可访问性测试

---

## 📱 平台兼容性

### 系统版本支持
- **最低版本**: Android 6.0 (API 23)
- **目标版本**: Android 14 (API 35)
- **兼容测试**: API 23-35 全版本覆盖

### 设备适配
- **屏幕尺寸**: 4-12 英寸全面适配
- **分辨率**: HD 到 4K 分辨率支持
- **屏幕方向**: 强制竖屏，播放器支持横屏
- **输入方式**: 触摸、键盘、外接手柄

### 语言支持
- **界面语言**: 简体中文
- **内容编码**: UTF-8 全字符集支持
- **RTL 布局**: 阿拉伯语系预留支持

---

## 🔄 版本管理

### 版本号规范
```
MAJOR.MINOR.PATCH
1.0.0
│ │ └── 补丁版本 (bug修复)
│ └──── 次版本 (功能更新)
└────── 主版本 (重大变更)
```

### 发布流程
1. **代码审查** 确保代码质量
2. **自动化测试** 运行完整测试套件
3. **性能测试** 验证性能指标
4. **安全扫描** 检查安全漏洞
5. **版本打包** 生成发布版本
6. **灰度发布** 逐步推送更新

---

这个技术架构文档涵盖了 AList Android 应用的核心技术设计和实现细节，为开发和维护提供了详细的技术指导。