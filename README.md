# AList Android 管理工具

<div align="center">

![AList Logo](https://img.shields.io/badge/AList-Android-green?style=for-the-badge&logo=android&logoColor=white)
![Version](https://img.shields.io/badge/version-1.0.0-blue?style=for-the-badge)
![Build](https://img.shields.io/badge/build-passing-brightgreen?style=for-the-badge)
![License](https://img.shields.io/badge/license-MIT-yellow?style=for-the-badge)

**基于 AList 官方 API 开发的 Android 文件管理工具**

[功能特色](#-功能特色) • [快速开始](#-快速开始) • [安装指南](#-安装指南) • [使用说明](#-使用说明) • [技术架构](#-技术架构) • [开发文档](#-开发文档)

</div>

---

## 🌟 功能特色

### 📁 文件管理
- **远程文件浏览** - 访问 AList 服务器上的所有文件
- **文件夹导航** - 支持多级目录浏览和快速导航
- **文件搜索** - 按文件名和内容搜索文件
- **文件操作** - 创建文件夹、删除文件和文件夹
- **文件链接** - 获取文件直接下载链接

### 🎵 媒体播放
- **音视频播放** - 基于 ExoPlayer 的高性能播放器
- **格式支持** - 支持主流音视频格式
- **播放控制** - 播放/暂停、进度拖动、音量调节
- **音轨切换** - 支持多音轨文件切换
- **画中画** - Android 8.0+ 支持画中画播放
- **播放记录** - 自动记录播放历史

### 🔐 安全认证
- **用户认证** - 支持 AList 用户名密码登录
- **令牌管理** - 自动认证令牌获取和刷新
- **数据加密** - 使用 Android Keystore 加密敏感数据
- **记住密码** - 可选的密码保存功能

### 🌐 网络优化
- **智能连接** - 实时网络状态监控
- **错误处理** - 友好的网络错误提示
- **连接质量** - 网络质量评估和优化建议
- **离线提示** - 网络断开时明确提示

### ♿ 无障碍支持
- **完全支持** - 遵循 Android 无障碍设计规范
- **屏幕阅读器** - 支持 TalkBack 等辅助技术
- **语音控制** - 支持语音命令操作
- **大字体支持** - 自适应系统字体大小设置

---

## 🚀 快速开始

### 系统要求
- **Android 版本**: 6.0+ (API 23+)
- **存储空间**: 至少 50MB 可用空间
- **网络**: 需要互联网连接
- **AList 服务器**: 版本 3.0+ 的 AList 服务

### 主要功能演示

#### 1. 登录认证
```kotlin
// 网络状态自动检测
if (networkMonitor.isConnected.value) {
    viewModel.login(username, password, rememberMe)
}
```

#### 2. 文件浏览
```kotlin
// 实时文件列表加载
viewModel.loadFiles("/path/to/folder")
```

#### 3. 媒体播放
```kotlin
// 支持多种媒体格式
viewModel.playFile(fileInfo, playlist, playUrl)
```

---

## 📱 安装指南

### 从源码构建

1. **克隆项目**
```bash
git clone https://github.com/your-username/alist-android.git
cd alist-android
```

2. **环境要求**
- Android Studio Arctic Fox | 2020.3.1+
- Android Gradle Plugin 8.6+
- Kotlin 2.0+
- Android SDK API 35

3. **构建项目**
```bash
# 克隆依赖
./gradlew build

# 生成调试版本 APK
./gradlew assembleDebug

# 生成发布版本 APK
./gradlew assembleRelease
```

4. **安装到设备**
```bash
# 连接设备后安装
adb install app/build/outputs/apk/debug/app-debug.apk
```

### 直接安装 APK

1. 下载最新的 APK 文件：[app-debug.apk](app/build/outputs/apk/debug/app-debug.apk)
2. 在设备上启用"未知来源应用"安装选项
3. 点击 APK 文件进行安装

---

## 📖 使用说明

### 首次设置

1. **启动应用**
   - 打开 AList Android 应用
   - 同意应用权限请求

2. **配置服务器**
   - 输入 AList 服务器地址 (例如: `https://your-alist-server.com`)
   - 输入用户名和密码
   - 可选择"记住密码"以便下次自动登录

3. **开始使用**
   - 登录成功后自动进入文件管理界面
   - 可以浏览、搜索和播放文件

### 主要界面

#### 🔑 登录界面
- 服务器地址配置
- 用户名密码输入
- 网络状态实时显示
- 记住密码选项

#### 📁 文件管理界面
- 文件和文件夹列表显示
- 文件搜索功能
- 文件排序和筛选
- 文件操作菜单
- 面包屑导航

#### 🎬 播放器界面
- 视频预览和播放控制
- 进度条和时间显示
- 音轨选择菜单
- 播放速度调节
- 画中画模式切换

#### 📚 播放历史
- 播放记录列表
- 播放时间统计
- 快速重新播放
- 历史记录清理

### 高级功能

#### 网络状态监控
应用顶部显示实时网络状态：
- 🟢 **网络优秀** - WiFi/有线网络连接
- 🟡 **网络一般** - 移动网络连接
- 🔴 **网络断开** - 无网络连接
- ⚠️ **网络信号弱** - 可能影响使用体验

#### 画中画模式
1. 播放视频时按 Home 键
2. 视频自动切换到小窗口模式
3. 可以继续观看视频同时使用其他应用

#### 文件操作
- **创建文件夹**: 在当前目录创建新文件夹
- **删除文件**: 长按文件选择删除操作
- **获取链接**: 复制文件直接下载链接
- **文件分享**: 通过其他应用分享文件链接

---

## 🏗️ 技术架构

### 架构设计
```
┌─────────────────────────────────────────────────┐
│                    UI Layer                     │
├─────────────────┬─────────────────┬───────────────┤
│  Login Screen   │ File List Screen│Player Screen │
├─────────────────┴─────────────────┴───────────────┤
│              ViewModels (MVVM)                  │
├─────────────────┬─────────────────┬───────────────┤
│ AuthViewModel   │ FileViewModel    │PlayerViewModel│
├─────────────────┴─────────────────┴───────────────┤
│               Repository Layer                     │
├─────────────────┬─────────────────┬───────────────┤
│   AList API     │   Database      │ Preferences  │
├─────────────────┴─────────────────┴───────────────┤
│               Data Layer                           │
├─────────────────┬─────────────────┬───────────────┤
│ Network Module  │ Security Module  │ Error Handler│
└─────────────────┴─────────────────┴───────────────┘
```

### 技术栈

#### 核心框架
- **语言**: Kotlin 2.0+
- **UI 框架**: Jetpack Compose
- **架构**: MVVM (Model-View-ViewModel)
- **异步**: Kotlin Coroutines + Flow

#### 数据存储
- **数据库**: Room (播放历史记录)
- **偏好设置**: DataStore
- **安全存储**: Android Keystore
- **缓存**: 内存缓存 + 文件缓存

#### 网络通信
- **HTTP 客户端**: OkHttp + Retrofit
- **JSON 解析**: Gson
- **认证**: Bearer Token
- **文件下载**: OkHttp 流式下载

#### 媒体播放
- **播放器**: ExoPlayer (Media3)
- **音视频解码**: 系统原生解码器
- **后台播放**: Foreground Service
- **画中画**: Picture-in-Picture API

#### 依赖注入
- **主要**: 手动依赖注入 (简化版)
- **配置**: 模块化设计
- **生命周期**: Android Architecture Components

### 核心模块

#### 🌐 网络模块 (`data/network`)
```kotlin
// 网络监控
NetworkConnectivityMonitor
NetworkModule

// API 服务
AListApiService
NetworkModule
```

#### 🔐 安全模块 (`data/security`)
```kotlin
// 数据加密
SecurityHelper
EncryptedPreferences
```

#### ⚠️ 错误处理 (`data/error`)
```kotlin
// 全局错误处理
GlobalErrorHandler
ErrorHandlingUtils
NetworkException
```

#### 🎥 播放器模块 (`player/`)
```kotlin
// 播放器管理
SimpleExoPlayerManager
PlaybackState
```

#### 🗄️ 数据模块 (`data/`)
```kotlin
// 本地数据
DatabaseModule
PreferencesRepository
PlayHistoryDao
```

---

## 📚 开发文档

### 项目结构
```
app/
├── src/main/java/com/alist/android/
│   ├── data/                    # 数据层
│   │   ├── api/                # API 接口
│   │   ├── database/           # 数据库
│   │   ├── error/              # 错误处理
│   │   ├── model/              # 数据模型
│   │   ├── network/            # 网络模块
│   │   ├── preferences/        # 偏好设置
│   │   └── security/           # 安全模块
│   ├── player/                 # 播放器
│   │   ├── core/               # 核心播放器
│   │   └── service/            # 播放器服务
│   ├── ui/                     # UI 层
│   │   ├── component/          # UI 组件
│   │   ├── screen/             # 屏幕页面
│   │   ├── theme/              # 主题样式
│   │   └── viewmodel/          # 视图模型
│   ├── AListApplication.kt     # 应用类
│   └── MainActivity.kt         # 主活动
└── build.gradle.kts            # 构建配置
```

### 添加新功能

#### 1. 新增 API 接口
```kotlin
interface AListApiService {
    @GET("/api/fs/list")
    suspend fun getFiles(
        @Header("Authorization") token: String,
        @Query("path") path: String
    ): Response<FileListResponse>
}
```

#### 2. 创建 ViewModel
```kotlin
class NewFeatureViewModel(
    private val application: Application
) : ViewModel() {

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val application = extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as Application
                return NewFeatureViewModel(application) as T
            }
        }
    }
}
```

#### 3. 添加 UI 屏幕
```kotlin
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewFeatureScreen(
    onBack: () -> Unit,
    viewModel: NewFeatureViewModel = viewModel(factory = NewFeatureViewModel.Factory)
) {
    // Compose UI 实现
}
```

### 自定义配置

#### 修改服务器地址
编辑 `data/preferences/DataStoreModule.kt` 中的默认服务器配置。

#### 添加新的文件类型支持
在 `player/core/SimpleExoPlayerManager.kt` 中添加新的 MIME 类型处理。

#### 自定义主题
在 `ui/theme/Theme.kt` 中修改颜色和样式配置。

### 构建和发布

#### 调试版本
```bash
./gradlew assembleDebug
```

#### 发布版本
```bash
./gradlew assembleRelease
```

#### 运行测试
```bash
./gradlew test
./gradlew lint
```

---

## 🔧 故障排除

### 常见问题

#### 1. 登录失败
**问题**: 显示"网络连接失败"错误
**解决方案**:
- 检查网络连接
- 验证服务器地址是否正确
- 确认 AList 服务正在运行
- 检查用户名密码是否正确

#### 2. 播放失败
**问题**: 视频无法播放
**解决方案**:
- 检查网络连接质量
- 确认文件格式支持
- 尝试重新启动应用
- 清除应用缓存

#### 3. 文件列表加载慢
**问题**: 文件列表加载缓慢
**解决方案**:
- 检查服务器响应时间
- 优化网络连接
- 减少文件夹内文件数量

#### 4. 画中画不可用
**问题**: 无法使用画中画功能
**解决方案**:
- 确认 Android 版本 8.0+
- 在系统设置中启用画中画权限
- 检查应用是否在前台启动

### 日志调试

启用调试日志:
```kotlin
// 在 Application 类中启用
Timber.plant(Timber.DebugTree())
```

查看网络请求日志:
```kotlin
// OkHttp 日志拦截器
val logging = HttpLoggingInterceptor()
logging.setLevel(HttpLoggingInterceptor.Level.BODY)
```

---

## 🤝 贡献指南

### 开发环境设置
1. Fork 本仓库
2. 创建功能分支: `git checkout -b feature/new-feature`
3. 提交更改: `git commit -m 'Add new feature'`
4. 推送分支: `git push origin feature/new-feature`
5. 创建 Pull Request

### 代码规范
- 遵循 Kotlin 官方编码规范
- 使用 Material Design 3 设计规范
- 添加必要的注释和文档
- 确保代码可访问性

### 测试要求
- 新功能需要包含单元测试
- UI 组件需要包含界面测试
- 确保所有测试通过
- 运行代码质量检查

---

## 📄 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情。

## 🙏 致谢

- [AList](https://alist.nn.ci) - 优秀的文件列表程序
- [Android Developers](https://developer.android.com) - Android 开发文档
- [Jetpack Compose](https://developer.android.com/jetpack/compose) - 现代 UI 工具包
- [ExoPlayer](https://developer.android.com/media/media3/exoplayer) - 媒体播放框架

## 📞 联系我们

- **问题反馈**: [GitHub Issues](https://github.com/your-username/alist-android/issues)
- **功能建议**: [GitHub Discussions](https://github.com/your-username/alist-android/discussions)
- **邮件联系**: your-email@example.com

---

<div align="center">

**⭐ 如果这个项目对您有帮助，请给我们一个 Star！**

![Star History](https://img.shields.io/github/stars/your-username/alist-android?style=social)

</div>