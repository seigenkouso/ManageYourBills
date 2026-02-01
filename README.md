# Manage Your Bills 记账 v1.2.1

![Kotlin](https://img.shields.io/badge/Kotlin-1.9.0-purple?style=flat&logo=kotlin)
![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-green?style=flat&logo=android)
![Material 3](https://img.shields.io/badge/Design-Material%203-blue?style=flat&logo=materialdesign)
![Android](https://img.shields.io/badge/Platform-Android-orange?style=flat&logo=android)

> **ManageYourBills** 是一款基于 **Jetpack Compose** 开发的现代化 Android 记账应用。它坚持**纯本地存储**（保障隐私安全），同时提供 **WebDAV 云备份**（数据防丢）和 **AI 智能识别**功能，致力于为用户提供极致流畅、无广告、纯净的记账体验。

---

## ✨ 核心特性 (Features)

### 🎨 现代化设计
* **Material Design 3**: 完全遵循谷歌最新的设计规范，界面简洁优雅。
* **深色模式完美适配**: 
    * ☀️ **浅色模式**: 采用沉稳的 **ThemeGreen (深绿)** 主题，清新自然。
    * 🌙 **深色模式**: 自动切换为护眼的 **ThemeLightGreen (浅绿)** 主题，夜间使用更舒适。
* **流畅交互**: 基于 Compose 构建的丝滑转场与跟手动画。

### ☁️ 数据安全与同步
* **纯本地存储**: 核心数据基于 **Room Database** 存储在手机本地，拒绝隐私上传，安全无忧。
* **WebDAV 云备份**: 
    * 支持配置 **坚果云** (或其他标准 WebDAV 服务)。
    * 一键**备份**数据到云端 / 从云端**恢复**数据。
    * 支持换机数据迁移，不再担心数据丢失。

### 🤖 AI 智能记账
* **截图识别**: 集成 **Google ML Kit**，支持从相册导入账单截图或小票照片。
* **自动解析**: 本地 AI 算法自动识别**金额、商户、日期**，并根据关键词智能匹配**消费分类**，极大提升记账效率。

### 🚀 便捷更新
* **应用内更新**: 自动检测 GitHub Release 最新版本。
* **高速下载**: 内置加速节点，支持 App 内直接**后台下载**并自动唤起安装 (适配 Android FileProvider 机制)，无需跳转浏览器。

## 🛠️ 技术栈 (Tech Stack)

* **语言**: Kotlin
* **UI 框架**: Jetpack Compose (Material3)
* **架构**: MVVM (Model-View-ViewModel)
* **数据库**: Room (SQLite)
* **网络**: HttpURLConnection (GitHub API) / Sardine-Android (WebDAV)
* **AI/ML**: Google ML Kit (Text Recognition v2)
* **异步处理**: Kotlin Coroutines & Flow
* **依赖注入**: Manual Dependency Injection (AppViewModelProvider)

## 📥 下载与安装 (Download)

您可以前往 [Releases 页面](https://github.com/seigenkouso/ManageYourBills/releases) 下载最新版本的 APK 安装包。

或者直接点击下方链接下载最新版：
[👉 **下载最新版 (Latest APK)**](https://ghfast.top/https://github.com/seigenkouso/ManageYourBills/releases/latest/download/ManageYourBills.apk)

同时，欢迎访问 [**Manage Your Bills 记账**](https://seigenkouso.github.io/manage-your-bills-web/) 官方网站。

## 📖 使用指南 (User Guide)

### 1. 配置云备份 (推荐)
为了防止数据丢失，建议首次安装后进行配置：
1.  注册一个 [坚果云](https://www.jianguoyun.com/) 账号。
2.  进入坚果云后台 -> 账户信息 -> 安全选项 -> **添加应用密码**。
3.  打开 App -> 侧边栏 -> **设置 & 云备份**。
4.  点击 **配置坚果云账号**，输入您的坚果云账号(邮箱)和**应用密码**。
5.  点击 **立即备份到云端** 即可。

### 2. 使用 AI 记账
1.  点击主页右下角的 `+` 号进入记账页。
2.  点击输入框下方的 **"AI 截图识别"** 按钮。
3.  选择一张包含金额和日期的图片（如微信/支付宝账单截图）。
4.  App 会自动填充金额、备注、分类和日期，确认无误后点击保存。

## 🤝 贡献 (Contributing)

欢迎提交 Issue 或 Pull Request！如果您有好的建议或发现了 Bug，请随时告知。

1.  Fork 本仓库
2.  新建分支 (`git checkout -b feature/AmazingFeature`)
3.  提交更改 (`git commit -m 'Add some AmazingFeature'`)
4.  推送到分支 (`git push origin feature/AmazingFeature`)
5.  提交 Pull Request

## 📄 开源协议 (License)

本项目采用 [MIT License](LICENSE) 开源协议。

---

**Developed with ❤️ by Seigenkouso Lun**
