# 更新日志

本文档记录了 OpenList Android 应用的所有重要变更。

## [2.0.0] - 2025-01-07

### 重构
- **完整品牌重命名** - 将应用从 AList 重命名为 OpenList
- **包结构优化** - 合并 com.example.alist 包到 com.openlist.android
- **数据存储重命名** - 统一数据存储标识符：
  - DataStore: `alist_preferences` → `openlist_preferences`
  - 数据库: `alist_database` → `openlist_database`
  - 加密存储: `alist_secure_prefs` → `openlist_secure_prefs`

### 核心类重命名
- `AListApplication` → `OpenListApplication`
- `AListApiService` → `OpenListApiService`
- `AListException` → `OpenListException`
- `AListTheme` → `OpenListTheme`

### 用户界面更新
- 应用显示名称: "AList 管理器" → "OpenList 管理器"
- 主题样式: `Theme.AList` → `Theme.OpenList`
- 登录界面标题和品牌标识更新
- 所有用户可见文本已更新为 OpenList

### 文档更新
- 完全重写 README.md，包含专业格式化、功能徽章和详细文档
- 更新 CLAUDE.md 技术文档，反映 OpenList 架构
- 创建详细的重命名完成报告
- 更新所有开发指南和部署说明

### 破坏性变更
- **数据重置** - 所有应用数据将被清除（用户设置、登录凭据等）
- **重新配置** - 用户需要重新设置服务器连接和账户信息
- **不兼容性** - 不保持向后兼容性，需要全新安装

## [1.0.0] - 2025-01-06

### 新功能
- **初始版本发布**
- **Hilt 依赖注入** - 完整的依赖注入系统集成
- **MVVM 架构** - 清洁的架构模式实现
- **Jetpack Compose** - 现代 UI 框架集成
- **Material 3 设计** - 最新设计系统应用

### 核心功能
- **用户认证** - OpenList 服务器认证系统
- **文件管理** - 远程文件浏览和操作
- **媒体播放** - 基于 ExoPlayer 的音视频播放
- **网络优化** - 智能连接和错误处理
- **安全存储** - Android Keystore 数据加密

### 技术特性
- **网络监控** - 实时连接状态监控
- **错误处理** - 全局错误处理系统
- **URL 处理** - 智能格式化和验证
- **数据持久化** - Room 数据库和 DataStore
- **无障碍支持** - 完整的辅助功能支持

### 开发工具
- **构建系统** - Gradle 8.6+ 支持
- **Kotlin 2.0** - 最新语言特性支持
- **测试框架** - 单元测试和集成测试
- **代码质量** - Lint 检查和代码规范

---

## 版本说明

### 版本号规则
- **主版本号** - 不兼容的 API 变更
- **次版本号** - 向后兼容的功能新增
- **修订号** - 向后兼容的问题修复

### 更新类型
- **功能新增** - 新功能的实现
- **问题修复** - 错误修复和性能优化
- **文档更新** - 文档和完善
- **重构** - 代码结构优化

### 贡献
欢迎通过 GitHub Issues 和 Pull Requests 贡献代码和建议。

---

**OpenList Android** - 基于 OpenList 协议的现代化文件管理工具