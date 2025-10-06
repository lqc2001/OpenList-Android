# OpenList Android 管理工具

<div align="center">

![OpenList Logo](https://img.shields.io/badge/OpenList-Android-green?style=for-the-badge&logo=android&logoColor=white)
![Version](https://img.shields.io/badge/version-1.0.0-blue?style=for-the-badge)
![Build](https://img.shields.io/badge/build-passing-brightgreen?style=for-the-badge)
![License](https://img.shields.io/badge/license-MIT-yellow?style=for-the-badge)
![Platform](https://img.shields.io/badge/platform-android-brightgreen?style=for-the-badge)

**基于 OpenList 协议开发的现代化 Android 文件管理工具**

[功能特色](#-功能特色) • [快速开始](#-快速开始) • [安装指南](#-安装指南) • [使用说明](#-使用说明) • [技术架构](#-技术架构) • [开发文档](#-开发文档)

[📱 下载 APK](./releases) • [🐛 报告问题](./issues) • [💡 功能建议](./discussions) • [📖 在线文档](./wiki)

</div>

---

## 🌟 功能特色

### 📁 文件管理
- **远程文件浏览** - 访问 OpenList 服务器上的所有文件
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
- **用户认证** - 支持 OpenList 用户名密码登录
- **令牌管理** - 自动认证令牌获取和刷新
- **数据加密** - 使用 Android Keystore 加密敏感数据
- **记住密码** - 可选的密码保存功能

### 🌐 网络优化
- **智能连接** - 实时网络状态监控
- **错误处理** - 友好的网络错误提示
- **连接质量** - 网络质量评估和优化建议
- **离线提示** - 网络断开时明确提示

### 🎨 现代化界面
- **Material Design 3** - 采用最新设计规范
- **深色模式** - 支持浅色/深色主题切换
- **响应式布局** - 适配不同屏幕尺寸
- **流畅动画** - 丝滑的交互动画效果

### ♿ 无障碍支持
- **完全支持** - 遵循 Android 无障碍设计规范
- **屏幕阅读器** - 支持 TalkBack 等辅助技术
- **语音控制** - 支持语音命令操作

---

## 🚀 快速开始

### 系统要求
- **Android 版本**: 6.0+ (API 23+)
- **存储空间**: 至少 50MB 可用空间
- **网络**: 需要互联网连接
- **OpenList 服务器**: 版本 3.0+ 的 OpenList 服务

### 下载安装

#### 方式1：直接下载 APK
```bash
# 下载最新版本
wget https://github.com/lqc2001/OpenList-Android/releases/latest/download/OpenList-Android.apk

# 安装 APK
adb install OpenList-Android.apk
```

#### 方式2：从源码构建
```bash
# 克隆仓库
git clone https://github.com/lqc2001/OpenList-Android.git
cd OpenList-Android

# 构建项目
./gradlew assembleDebug

# 安装到设备
./gradlew installDebug
```

### 首次设置

1. **启动应用**
   - 打开 OpenList Android 应用
   - 同意应用权限请求

2. **配置服务器**
   - 输入 OpenList 服务器地址 (例如: `https://your-openlist-server.com`)
   - 输入用户名和密码
   - 可选择"记住密码"以便下次自动登录

3. **开始使用**
   - 登录成功后自动进入文件管理界面
   - 可以浏览、搜索和播放文件

---

## 📖 使用说明

### 主要界面

#### 🔐 登录界面
- **服务器地址**: 支持 HTTP/HTTPS 协议
- **智能格式化**: 自动补全协议前缀
- **网络诊断**: 实时检测服务器连接状态
- **记住我**: 可选保存登录凭据

#### 📂 文件列表界面
- **浏览文件**: 支持文件夹层级导航
- **搜索功能**: 快速查找文件
- **排序选项**: 按名称、大小、修改时间排序
- **多选操作**: 批量选择和操作文件

#### 🎵 播放器界面
- **基础控制**: 播放/暂停、进度条、音量
- **高级功能**: 画中画、音轨切换、字幕选择
- **播放列表**: 自动连续播放
- **历史记录**: 快速恢复播放进度

#### ⚙️ 设置界面
- **账户管理**: 清除保存的凭据
- **播放设置**: 默认播放器设置
- **主题设置**: 外观个性化选项

### 快捷操作

| 功能 | 操作 |
|------|------|
| 刷新文件列表 | 下拉刷新 |
| 返回上一级 | 点击返回按钮或滑动边缘 |
| 多选模式 | 长按文件进入多选 |
| 快速搜索 | 点击搜索图标 |
| 播放媒体 | 点击媒体文件 |

---

## 🛠️ 技术架构

### 架构设计

```
┌─────────────────────────────────────────┐
│                Presentation Layer       │
│  ┌─────────────┐ ┌─────────────────────┐ │
│  │  MainActivity │ │   Compose UI        │ │
│  └─────────────┘ └─────────────────────┘ │
└─────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────┐
│              Domain Layer               │
│  ┌─────────────┐ ┌─────────────────────┐ │
│  │ ViewModels   │ │    Use Cases        │ │
│  └─────────────┘ └─────────────────────┘ │
└─────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────┐
│               Data Layer                 │
│  ┌─────────────┐ ┌─────────────────────┐ │
│  │ Repositories │ │  Data Sources       │ │
│  └─────────────┘ └─────────────────────┘ │
└─────────────────────────────────────────┘
```

### 核心技术栈

- **编程语言**: Kotlin + Coroutines
- **UI 框架**: Jetpack Compose + Material3
- **架构模式**: MVVM + Clean Architecture
- **依赖注入**: Hilt
- **网络**: Retrofit + OkHttp
- **数据库**: Room
- **媒体播放**: ExoPlayer (Media3)
- **数据存储**: DataStore + EncryptedSharedPreferences

### 项目结构

```
app/src/main/java/com/openlist/android/
├── data/
│   ├── api/           # API 接口定义
│   ├── database/      # Room 数据库
│   ├── model/         # 数据模型
│   ├── network/       # 网络配置
│   ├── repository/    # 数据仓库
│   ├── security/      # 安全相关
│   └── utils/         # 工具类
├── di/                # 依赖注入模块
├── ui/
│   ├── screen/        # 屏幕界面
│   ├── theme/         # 主题配置
│   └── viewmodel/     # 视图模型
├── AListApplication.kt # 应用入口
└── MainActivity.kt    # 主活动
```

---

## 🔧 开发文档

### 环境配置

#### 系统要求
- **操作系统**: Windows 10+, macOS 10.14+, 或 Linux
- **Java 版本**: JDK 11 或更高版本
- **Android Studio**: Arctic Fox 或更高版本
- **Android SDK**: API 23-35
- **NDK**: (可选，如需本地库)

#### 设置开发环境

1. **克隆项目**
```bash
git clone https://github.com/lqc2001/OpenList-Android.git
cd OpenList-Android
```

2. **打开 Android Studio**
   - 启动 Android Studio
   - 选择 "Open an existing project"
   - 选择项目目录

3. **同步项目**
   - Android Studio 会自动检测并提示同步 Gradle
   - 点击 "Sync Now" 等待完成

4. **构建项目**
```bash
# 清理项目
./gradlew clean

# 构建调试版本
./gradlew assembleDebug

# 构建发布版本
./gradlew assembleRelease
```

### 代码规范

#### Kotlin 代码风格
- 使用 Kotlin 官方代码风格
- 函数和属性使用有意义的命名
- 适当的注释和文档

#### Git 提交规范
```
类型(范围): 简短描述

详细描述（可选）

🤖 Generated with [Claude Code](https://claude.com/claude-code)

Co-Authored-By: Claude <noreply@anthropic.com>
```

**提交类型：**
- `feat`: 新功能
- `fix`: 修复 bug
- `docs`: 文档更新
- `style`: 代码格式化
- `refactor`: 代码重构
- `test`: 测试相关
- `chore`: 构建或工具变动

### 测试指南

#### 单元测试
```bash
# 运行单元测试
./gradlew test

# 运行特定测试类
./gradlew test --tests "*ViewModelTest"
```

#### 集成测试
```bash
# 运行集成测试
./gradlew connectedAndroidTest

# 生成测试覆盖率报告
./gradlew createDebugCoverageReport
```

---

## 📄 许可证

本项目采用 MIT 许可证 - 详见 [LICENSE](LICENSE) 文件

```
MIT License

Copyright (c) 2025 OpenList Android

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

---

## 🤝 贡献指南

我们欢迎所有形式的贡献！请阅读 [CONTRIBUTING.md](CONTRIBUTING.md) 了解如何参与项目开发。

### 如何贡献

1. **Fork 本仓库**
2. **创建功能分支** (`git checkout -b feature/AmazingFeature`)
3. **提交更改** (`git commit -m 'Add some AmazingFeature'`)
4. **推送分支** (`git push origin feature/AmazingFeature`)
5. **创建 Pull Request**

### 开发指南

- 查看 [CONTRIBUTING.md](CONTRIBUTING.md) 了解详细的开发指南
- 遵循项目的代码规范和提交规范
- 确保所有测试通过

---

## 📞 支持

### 获取帮助

- 📖 **文档**: 查看 [Wiki](./wiki) 获取详细文档
- 🐛 **问题报告**: 使用 [GitHub Issues](./issues) 报告 bug
- 💡 **功能建议**: 在 [GitHub Discussions](./discussions) 中讨论
- 📧 **联系我们**: 创建 issue 或 discussion

### 常见问题

<details>
<summary>如何配置 OpenList 服务器？</summary>

1. 确保你的 OpenList 服务器版本 >= 3.0
2. 在应用中输入服务器完整地址 (如 `https://your-domain.com`)
3. 输入你的用户名和密码
4. 可选择"记住密码"以便下次自动登录

</details>

<details>
<summary>应用支持哪些媒体格式？</summary>

应用支持大多数常见的音视频格式：
- **视频**: MP4, MKV, AVI, MOV, WMV, FLV, WebM, M4V
- **音频**: MP3, WAV, FLAC, AAC, OGG, M4A, WMA

</details>

<details>
<summary>如何启用画中画功能？</summary>

1. 确保你的设备运行 Android 8.0+
2. 在播放视频时，按 Home 键或应用切换按钮
3. 视频会自动进入画中画模式
4. 可以拖动调整窗口位置和大小

</details>

---

## 🙏 致谢

- [OpenList](https://alist.nn.ci) - 优秀的文件列表程序
- [Android Developers](https://developer.android.com) - Android 开发资源
- [Jetpack Compose](https://developer.android.com/jetpack/compose) - 现代 UI 工具包
- [ExoPlayer](https://exoplayer.dev) - 强大的媒体播放器

---

## 📊 项目统计

![GitHub stars](https://img.shields.io/github/stars/lqc2001/OpenList-Android?style=social)
![GitHub forks](https://img.shields.io/github/forks/lqc2001/OpenList-Android?style=social)
![GitHub issues](https://img.shields.io/github/issues/lqc2001/OpenList-Android)
![GitHub license](https://img.shields.io/github/license/lqc2001/OpenList-Android)

---

<div align="center">

**如果这个项目对你有帮助，请给我们一个 Star ⭐**

[🔝 返回顶部](#openlist-android-管理工具)

</div>