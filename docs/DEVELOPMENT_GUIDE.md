# AList Android 开发指南

## 📖 开发环境配置

### 系统要求
- **操作系统**: Windows 10/11, macOS, Linux
- **Android Studio**: Arctic Fox | 2020.3.1 或更高版本
- **Java JDK**: 11 或更高版本
- **Android SDK**: API 35 (Android 14)
- **Kotlin**: 2.0+

### 必要组件
1. **Android Gradle Plugin**: 8.6+
2. **Kotlin Multiplatform Mobile**: 1.9+
3. **Android NDK**: r25b 或更高版本（如需原生开发）

---

## 🛠️ 项目结构

```
app/
├── src/main/java/com/alist/android/
│   ├── data/                    # 数据层
│   │   ├── api/                # API 接口定义
│   │   ├── database/           # 数据库相关
│   │   ├── error/              # 错误处理
│   │   ├── model/              # 数据模型
│   │   ├── network/            # 网络模块
│   │   ├── preferences/        # 偏好设置
│   │   └── security/           # 安全模块
│   ├── player/                 # 播放器模块
│   │   ├── core/               # 核心播放器
│   │   └── service/            # 播放器服务
│   ├── ui/                     # UI 层
│   │   ├── component/          # 可重用组件
│   │   ├── screen/             # 屏幕页面
│   │   ├── theme/              # 主题样式
│   │   └── viewmodel/          # 视图模型
│   ├── AListApplication.kt     # 应用入口
│   └── MainActivity.kt         # 主活动
└── build.gradle.kts            # 构建配置
```

---

## 🏗️ 架构设计

### MVVM 架构模式

```
┌─────────────────────────────────────────────────┐
│                    UI Layer                     │
│              (Jetpack Compose)                  │
├─────────────────┬─────────────────┬───────────────┤
│  Login Screen   │ File List Screen│Player Screen │
├─────────────────┴─────────────────┴───────────────┤
│              ViewModels (MVVM)                  │
│        (StateFlow + Coroutines)                 │
├─────────────────┬─────────────────┬───────────────┤
│ AuthViewModel   │ FileViewModel    │PlayerViewModel│
├─────────────────┴─────────────────┴───────────────┤
│               Repository Layer                     │
│          (Single Source of Truth)                │
├─────────────────┬─────────────────┬───────────────┤
│   AList API     │   Database      │ Preferences  │
├─────────────────┴─────────────────┴───────────────┤
│               Data Layer                           │
│        (Network + Security + Storage)            │
└─────────────────────────────────────────────────┘
```

### 数据流设计

```
用户操作 → UI事件 → ViewModel → Repository → API/数据库
                                                    ↓
响应/数据 ← UI更新 ← StateFlow ← ViewModel ← Repository
```

---

## 📦 依赖管理

### 核心依赖

```kotlin
// build.gradle.kts
dependencies {
    // Android 核心库
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")

    // UI 框架
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation("androidx.compose.ui:ui:1.5.4")
    implementation("androidx.compose.material3:material3:1.1.2")

    // 网络库
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // 数据存储
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")
    implementation("androidx.datastore:datastore-preferences:1.0.0")

    // 媒体播放
    implementation("androidx.media3:media3-exoplayer:1.2.1")
    implementation("androidx.media3:media3-ui:1.2.1")
    implementation("androidx.media3:media3-exoplayer-hls:1.2.1")

    // 安全库
    implementation("androidx.security:security-crypto:1.1.0-alpha06")

    // 测试库
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.mockito:mockito-core:5.8.0")
}
```

### 开发工具依赖

```kotlin
// build.gradle.kts
dependencies {
    // 调试工具
    debugImplementation("com.squareup.leakcanary:leakcanary-android:2.12")
    debugImplementation("com.github.chuckerteam.chucker:library:4.0.0")
    releaseImplementation("com.github.chuckerteam.chucker:library-no-op:4.0.0")

    // 代码生成
    ksp("dev.zacsweers.moshix:moshi-ksp:0.25.1")
}
```

---

## 🔧 开发工作流

### 1. 创建新功能

#### 步骤1：定义数据模型
```kotlin
// data/model/NewFeatureModel.kt
data class NewFeatureData(
    val id: String,
    val name: String,
    val description: String?
)
```

#### 步骤2：创建API接口
```kotlin
// data/api/AListApiService.kt
@GET("/api/new-feature")
suspend fun getNewFeature(
    @Header("Authorization") token: String,
    @Query("id") id: String
): Response<NewFeatureResponse>
```

#### 步骤3：实现Repository
```kotlin
// data/repository/NewFeatureRepository.kt
class NewFeatureRepository @Inject constructor(
    private val apiService: AListApiService
) {
    suspend fun getFeature(id: String): Result<NewFeatureData> {
        return try {
            val response = apiService.getNewFeature(getToken(), id)
            if (response.isSuccessful) {
                Result.success(response.body()?.data!!)
            } else {
                Result.failure(Exception(response.message()))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

#### 步骤4：创建ViewModel
```kotlin
// ui/viewmodel/NewFeatureViewModel.kt
class NewFeatureViewModel @Inject constructor(
    private val repository: NewFeatureRepository
) : ViewModel() {

    private val _featureState = MutableStateFlow<FeatureState>(FeatureState.Loading)
    val featureState: StateFlow<FeatureState> = _featureState.asStateFlow()

    fun loadFeature(id: String) {
        viewModelScope.launch {
            _featureState.value = FeatureState.Loading
            when (val result = repository.getFeature(id)) {
                is Result.Success -> {
                    _featureState.value = FeatureState.Success(result.data)
                }
                is Result.Failure -> {
                    _featureState.value = FeatureState.Error(result.exception.message ?: "Unknown error")
                }
            }
        }
    }
}
```

#### 步骤5：创建UI组件
```kotlin
// ui/screen/NewFeatureScreen.kt
@Composable
fun NewFeatureScreen(
    viewModel: NewFeatureViewModel = viewModel(),
    onBack: () -> Unit
) {
    val featureState by viewModel.featureState.collectAsStateWithLifecycle()

    when (val state = featureState) {
        is FeatureState.Loading -> {
            LoadingIndicator()
        }
        is FeatureState.Success -> {
            FeatureContent(state.data)
        }
        is FeatureState.Error -> {
            ErrorMessage(state.message)
        }
    }
}
```

### 2. 测试策略

#### 单元测试
```kotlin
// test/ExampleUnitTest.kt
@Test
fun `test login success`() = runTest {
    // Given
    val mockApiService = mockk<AListApiService>()
    val repository = AuthRepository(mockApiService)

    val loginRequest = LoginRequest("test", "password")
    val loginResponse = LoginResponse("token", User(1, "test", "***", "/", 2, false, 255, null, null))

    coEvery { mockApiService.login(loginRequest) } returns Response.success(loginResponse)

    // When
    val result = repository.login(loginRequest)

    // Then
    assertTrue(result.isSuccess)
    assertEquals("token", result.getOrNull()?.token)
}
```

#### 集成测试
```kotlin
// test/ExampleInstrumentedTest.kt
@Test
fun testFileApiIntegration() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val apiService = createTestApiService(context)

    runBlocking {
        val response = apiService.getFiles("test-token", "/")
        assertTrue(response.isSuccessful)
        assertNotNull(response.body())
    }
}
```

### 3. 构建和部署

#### 本地构建
```bash
# 调试版本
./gradlew assembleDebug

# 发布版本
./gradlew assembleRelease

# 运行测试
./gradlew test

# 代码检查
./gradlew lint
```

#### CI/CD 配置
```yaml
# .github/workflows/android.yml
name: Android CI

jobs:
  build:
    runs-on: ubuntu-latest

    steps:
    - uses: actions/checkout@v3

    - name: Set up JDK 17
      uses: actions/setup-java@v3
      with:
        java-version: '17'
        distribution: 'temurin'

    - name: Grant execute permission for gradlew
      run: chmod +x gradlew

    - name: Build with Gradle
      run: ./gradlew build
```

---

## 🎨 UI 开发指南

### Material3 设计原则

#### 颜色系统
```kotlin
// ui/theme/Color.kt
private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF1976D2),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFBBDEFB),
    onPrimaryContainer = Color(0xFF0D47A1),

    secondary = Color(0xFF00796B),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFB2DFDB),
    onSecondaryContainer = Color(0xFF004D40),

    background = Color(0xFFFFFBFE),
    onBackground = Color(0xFF1C1B1F),

    surface = Color(0xFFFFFBFE),
    onSurface = Color(0xFF1C1B1F)
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF90CAF9),
    onPrimary = Color(0xFF0D47A1),
    primaryContainer = Color(0xFF1565C0),
    onPrimaryContainer = Color(0xFFE3F2FD),

    secondary = Color(0xFF4DB6AC),
    onSecondary = Color(0xFF004D40),
    secondaryContainer = Color(0xFF00695C),
    onSecondaryContainer = Color(0xFFE0F2F1),

    background = Color(0xFF1C1B1F),
    onBackground = Color(0xFFE6E1E5),

    surface = Color(0xFF1C1B1F),
    onSurface = Color(0xFFE6E1E5)
)
```

#### 主题切换
```kotlin
// ui/theme/Theme.kt
@Composable
fun AListTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        DarkColorScheme
    } else {
        LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}
```

### 响应式设计
```kotlin
@Composable
fun ResponsiveLayout(
    modifier: Modifier = Modifier
) {
    val windowSizeClass = calculateWindowSizeClass(LocalActivity.current)

    when (windowSizeClass.widthSizeClass) {
        WindowWidthSizeClass.Compact -> {
            // 手机布局
            CompactLayout(modifier)
        }
        WindowWidthSizeClass.Medium -> {
            // 平板布局
            MediumLayout(modifier)
        }
        WindowWidthSizeClass.Expanded -> {
            // 桌面布局
            ExpandedLayout(modifier)
        }
    }
}
```

### 无障碍支持
```kotlin
@Composable
fun AccessibleButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .semantics { this.contentDescription = contentDescription }
            .clearAndSetSemantics {  }
    ) {
        Text("点击我")
    }
}
```

---

## 🔐 安全开发指南

### 敏感数据存储

#### 使用 Android Keystore
```kotlin
// data/security/SecurityHelper.kt
object SecurityHelper {
    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    private const val KEY_ALIAS = "AListKey"

    fun encryptData(data: String): String {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        val secretKey = getOrCreateSecretKey(keyStore)

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)

        val iv = cipher.iv
        val encryptedData = cipher.doFinal(data.toByteArray())

        return Base64.encodeToString(iv + encryptedData, Base64.DEFAULT)
    }

    fun decryptData(encryptedData: String): String {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        val secretKey = getOrCreateSecretKey(keyStore)

        val data = Base64.decode(encryptedData, Base64.DEFAULT)
        val iv = data.sliceArray(0..11)
        val encrypted = data.sliceArray(12 until data.size)

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, secretKey, GCMParameterSpec(128, iv))

        return String(cipher.doFinal(encrypted))
    }
}
```

#### 安全的网络通信
```kotlin
// data/network/NetworkModule.kt
fun createOkHttpClient(context: Context): OkHttpClient {
    val trustManager = createTrustManager()
    val sslSocketFactory = createSSLSocketFactory(trustManager)

    return OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .addInterceptor(AuthInterceptor(context))
        .sslSocketFactory(sslSocketFactory, trustManager)
        .hostnameVerifier { hostname, session -> true }
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
}
```

### 权限管理
```kotlin
// MainActivity.kt
private fun checkAndRequestPermissions() {
    val requiredPermissions = mutableListOf<String>()

    if (ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET)
        != PackageManager.PERMISSION_GRANTED) {
        requiredPermissions.add(Manifest.permission.INTERNET)
    }

    if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
        != PackageManager.PERMISSION_GRANTED) {
        requiredPermissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
    }

    if (requiredPermissions.isNotEmpty()) {
        ActivityCompat.requestPermissions(
            this,
            requiredPermissions.toTypedArray(),
            PERMISSION_REQUEST_CODE
        )
    }
}
```

---

## 🚀 性能优化

### 内存优化

#### 图片加载优化
```kotlin
// 使用 Coil 进行图片加载
@Composable
fun OptimizedImageLoader(
    imageUrl: String,
    modifier: Modifier = Modifier
) {
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(imageUrl)
            .crossfade(true)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            .build(),
        contentDescription = null,
        modifier = modifier,
        contentScale = ContentScale.Crop
    )
}
```

#### 列表性能优化
```kotlin
@Composable
fun OptimizedLazyColumn(
    items: List<FileInfo>,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(
            count = items.size,
            key = { index -> items[index].path }
        ) { index ->
            FileItem(file = items[index])
        }
    }
}
```

### 网络优化

#### 请求缓存
```kotlin
// data/network/CacheInterceptor.kt
class CacheInterceptor @Inject constructor(
    private val context: Context
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)

        // 缓存文件列表响应
        if (request.url.encodedPath.contains("/api/fs/list")) {
            val cacheControl = CacheControl.Builder()
                .maxAge(5, TimeUnit.MINUTES)
                .build()

            return response.newBuilder()
                .header("Cache-Control", cacheControl.toString())
                .build()
        }

        return response
    }
}
```

#### 请求重试机制
```kotlin
// data/network/RetryInterceptor.kt
class RetryInterceptor @Inject constructor() : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        var response: Response? = null
        var retryCount = 0
        val maxRetries = 3

        while (retryCount < maxRetries) {
            try {
                response = chain.proceed(request)
                if (response.isSuccessful) {
                    return response
                }
            } catch (e: IOException) {
                if (retryCount == maxRetries - 1) {
                    throw e
                }
            }

            retryCount++
            Thread.sleep(1000L * retryCount) // 指数退避
        }

        return response ?: chain.proceed(request)
    }
}
```

---

## 🐛 调试和测试

### 日志系统
```kotlin
// AListApplication.kt
class AListApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        // 初始化网络监控
        NetworkConnectivityMonitor.getInstance(this)
    }
}
```

### 性能监控
```kotlin
// utils/PerformanceMonitor.kt
object PerformanceMonitor {
    fun measureTime(tag: String, operation: suspend () -> Unit) {
        val startTime = System.currentTimeMillis()
        try {
            operation()
        } finally {
            val endTime = System.currentTimeMillis()
            Timber.d("$tag took ${endTime - startTime}ms")
        }
    }
}
```

---

## 📝 代码规范

### Kotlin 编码规范

#### 命名规范
```kotlin
// 类名使用 PascalCase
class NetworkConnectivityMonitor

// 方法名使用 camelCase
fun getCurrentNetworkInfo(): NetworkInfo

// 常量使用 UPPER_SNAKE_CASE
const val MAX_RETRY_COUNT = 3

// 私有属性使用 _ 前缀
private val _isConnected = MutableStateFlow(false)
```

#### 代码组织
```kotlin
// 按照以下顺序组织类成员
class ExampleClass {
    // 1. 伴生对象和常量
    companion object {
        const val TAG = "ExampleClass"
    }

    // 2. 私有属性
    private val _data = MutableStateFlow<Data>()

    // 3. 公共属性
    val data: StateFlow<Data> = _data.asStateFlow()

    // 4. 构造函数
    constructor()

    // 5. 私有方法
    private fun privateMethod() {}

    // 6. 公共方法
    fun publicMethod() {}
}
```

### Git 工作流

#### 分支命名规范
```bash
# 功能分支
feature/login-screen
feature/file-upload
feature/pip-mode

# 修复分支
bugfix/network-timeout
bugfix/crash-on-startup

# 发布分支
release/v1.0.0
release/v1.1.0
```

#### 提交信息规范
```bash
# 功能添加
feat: 添加用户登录功能

# 修复问题
fix: 修复网络连接超时问题

# 文档更新
docs: 更新API文档

# 代码重构
refactor: 重构网络模块

# 测试相关
test: 添加登录单元测试
```

---

## 🚀 部署指南

### 发布流程

#### 1. 版本控制
```kotlin
// app/build.gradle.kts
android {
    defaultConfig {
        versionCode = 1
        versionName = "1.0.0"
    }

    signingConfigs {
        create("release") {
            storeFile = file("../keystore/release.keystore")
            storePassword = System.getenv("KEYSTORE_PASSWORD")
            keyAlias = System.getenv("KEY_ALIAS")
            keyPassword = System.getenv("KEY_PASSWORD")
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"))
        }
    }
}
```

#### 2. 应用签名
```bash
# 生成签名密钥
keytool -genkey -v -keystore release.keystore -alias alist -keyalg RSA -keysize 2048 -validity 10000

# 签名APK
jarsigner -verbose -sigalg SHA256withRSA -digestalg SHA-256 -keystore release.keystore app-release-unsigned.apk alist

# 验证签名
jarsigner -verify -verbose -certs app-release.apk
```

#### 3. 应用上架
```bash
# 优化APK大小
./gradlew bundleRelease

# 生成APK
./gradlew assembleRelease

# 检查APK
aapt dump badging app-release.apk
```

---

*本开发指南涵盖了 AList Android 应用的完整开发流程，包括环境配置、架构设计、开发规范、性能优化和部署流程。遵循这些指南可以确保代码质量和应用性能。*