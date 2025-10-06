# 贡献指南

感谢您对 OpenList Android 项目的关注！我们欢迎所有形式的贡献。

## 📋 贡献方式

### 报告问题
- 使用 [GitHub Issues](https://github.com/lqc2001/OpenList-Android/issues) 报告错误
- 提供详细的重现步骤和环境信息
- 使用错误报告模板以便我们更好地理解问题

### 功能建议
- 在 [GitHub Discussions](https://github.com/lqc2001/OpenList-Android/discussions) 中讨论新功能
- 详细描述需求和使用场景
- 提供可能的实现方案

### 代码贡献
- Fork 仓库并创建功能分支
- 遵循项目的代码规范和架构模式
- 提交 Pull Request 并描述变更内容

## 🛠️ 开发环境设置

### 系统要求
- **操作系统**: Windows 10+, macOS 10.14+, 或 Linux
- **Java 版本**: JDK 11 或更高版本
- **Android Studio**: Arctic Fox 或更高版本
- **Android SDK**: API 23-35
- **Git**: 最新版本

### 环境配置

1. **克隆项目**
   ```bash
   git clone https://github.com/lqc2001/OpenList-Android.git
   cd OpenList-Android
   ```

2. **配置 Android Studio**
   - 打开 Android Studio
   - 选择 "Open an existing project"
   - 选择项目目录
   - 等待 Gradle 同步完成

3. **验证构建**
   ```bash
   # 清理项目
   ./gradlew clean

   # 构建调试版本
   ./gradlew assembleDebug

   # 运行测试
   ./gradlew test
   ```

## 📝 代码规范

### Kotlin 代码风格
- 使用 Kotlin 官方代码风格
- 函数和属性使用有意义的命名
- 适当的注释和文档

### 命名约定
- **类名**: PascalCase (例如: `OpenListApplication`)
- **函数名**: camelCase (例如: `loadFiles()`)
- **变量名**: camelCase (例如: `currentPath`)
- **常量名**: UPPER_SNAKE_CASE (例如: `MAX_TIMEOUT`)

### 架构原则
- 遵循 MVVM + Clean Architecture
- 使用 Hilt 进行依赖注入
- 保持单一职责原则
- 实现依赖反转

### 错误处理
- 使用全局错误处理系统
- 提供用户友好的错误消息
- 正确处理网络异常
- 记录详细的错误日志

## 🧪 测试规范

### 单元测试
- 为所有核心功能编写单元测试
- 使用 JUnit 5 和 MockK
- 测试边界条件和异常情况

```kotlin
@Test
fun `test url formatting with valid input`() {
    val result = UrlUtils.formatUrl("example.com")
    assertEquals("https://example.com", result)
}
```

### 集成测试
- 测试组件间的交互
- 使用 Hilt 测试框架
- 测试网络和数据库操作

```bash
# 运行集成测试
./gradlew connectedAndroidTest

# 生成测试覆盖率报告
./gradlew createDebugCoverageReport
```

## 🔄 Git 工作流

### 分支策略
- **main**: 主分支，始终保持可发布状态
- **feature/**: 功能开发分支
- **bugfix/**: 错误修复分支
- **hotfix/**: 紧急修复分支

### 提交规范
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

### Pull Request 流程
1. 创建功能分支：`git checkout -b feature/feature-name`
2. 开发并提交更改
3. 推送到分支：`git push origin feature/feature-name`
4. 创建 Pull Request
5. 等待代码审查
6. 合并到主分支

## 📋 审查标准

### 代码质量
- 代码必须通过所有测试
- 代码复杂度控制在合理范围
- 遵循项目编码规范
- 添加适当的注释和文档

### 功能要求
- 新功能必须包含相应测试
- 错误修复必须包含回归测试
- 性能影响必须在可接受范围内
- 向后兼容性必须得到保证

### 文档要求
- 新功能需要更新用户文档
- API 变更需要更新技术文档
- 配置变更需要更新构建文档
- 重大变更需要更新更新日志

## 🐛 问题报告指南

### 错误报告模板
```markdown
## 描述
简要描述遇到的问题

## 重现步骤
1. 执行操作 A
2. 点击按钮 B
3. 观察结果 C

## 期望行为
描述期望的正确行为

## 实际行为
描述观察到的实际行为

## 环境信息
- 设备型号:
- Android 版本:
- 应用版本:
- OpenList 服务器版本:

## 日志信息
```
相关的错误日志
```
```

### 功能建议模板
```markdown
## 功能描述
详细描述建议的功能

## 使用场景
描述在什么情况下需要这个功能

## 实现建议
如果有的话，提供可能的实现方案

## 替代方案
描述现有的替代解决方案
```

## 📞 联系方式

- **GitHub Issues**: [https://github.com/lqc2001/OpenList-Android/issues](https://github.com/lqc2001/OpenList-Android/issues)
- **GitHub Discussions**: [https://github.com/lqc2001/OpenList-Android/discussions](https://github.com/lqc2001/OpenList-Android/discussions)
- **邮件**: 通过 GitHub Issues 联系

## 📄 许可证

通过贡献代码，您同意您的贡献将在 [MIT License](LICENSE) 下发布。

---

## 🎯 贡献者指南

### 第一次贡献
1. 阅读 [README.md](README.md) 了解项目概况
2. 熟悉 [CLAUDE.md](CLAUDE.md) 技术架构
3. 选择一个 good first issue
4. 按照本指南进行开发

### 重大功能开发
- 先在 Discussions 中讨论设计方案
- 创建详细的实现计划
- 分阶段实现和测试
- 确保向后兼容性

### 文档贡献
- 修正错误和拼写
- 改进示例代码
- 翻译文档
- 添加使用教程

## 🏆 贡献者认可

所有贡献者都会在以下地方得到认可：
- [README.md](README.md) 贡献者列表
- 发布说明中的致谢
- 代码提交历史中的署名

---

感谢您对 OpenList Android 项目的贡献！🎉