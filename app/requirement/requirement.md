# Android 音乐播放器 Demo 需求文档

## 1. 项目概述

### 1.1 项目名称

Android 音乐播放器 Demo

### 1.2 项目定位

本项目是一个用于学习 Android 开发基础知识和核心技能的音乐播放器 Demo。项目不追求商业级完整度，而是通过一个相对完整、可运行、可扩展的播放器场景，系统练习 Android 的 UI、生命周期、四大组件、事件处理、后台服务、通知栏、线程通信、列表展示和简单数据管理能力。

### 1.3 项目目标

通过开发本 Demo，学习并掌握以下 Android 开发能力：

- 使用 Android Studio 创建、运行、调试项目。
- 使用 XML 布局和常用 UI 控件构建页面。
- 使用 RecyclerView 展示音乐列表。
- 使用 Activity、Fragment、Service、BroadcastReceiver 等 Android 核心组件。
- 使用 Intent 完成页面跳转和组件通信。
- 使用 Handler 或主线程切换机制处理播放进度刷新。
- 使用 MediaPlayer 完成基础音频播放。
- 使用 Notification 展示前台播放通知。
- 理解前台、后台、横竖屏、页面销毁等生命周期场景。
- 实践简单的状态管理、数据模型设计和事件回调。

## 2. 目标用户

### 2.1 主要用户

Android 初学者、具备 Java 基础和一定前端基础的开发者。

### 2.2 使用场景

- 学习 Android 基础开发流程。
- 练习音乐播放相关 API。
- 练习 App 页面结构和组件通信。
- 作为后续扩展网络音乐、歌词、数据库、Jetpack 架构的基础项目。

## 3. 技术范围

### 3.1 推荐技术栈

- 开发语言：Java
- UI 方式：XML 布局
- 最低 Android 版本：Android 8.0，API 26 或以上
- 播放能力：MediaPlayer
- 列表组件：RecyclerView
- 页面结构：Activity + Fragment
- 后台播放：Service
- 播放通知：Notification
- 数据来源：本地内置音频资源，后续可扩展为扫描本地音乐

### 3.2 暂不纳入 MVP 的技术

- 网络音乐播放
- 在线搜索
- 用户登录
- 云端歌单
- 复杂数据库
- 复杂歌词滚动
- 商业级音频焦点处理
- 蓝牙耳机控制
- Android Auto

这些能力可以作为后续进阶版本扩展。

## 4. 项目版本规划

### 4.1 MVP 版本

MVP 版本要求完成一个可正常运行的本地音乐播放器，至少包含：

- 音乐列表页
- 播放详情页
- 播放、暂停、上一首、下一首
- 播放进度展示
- 后台播放 Service
- 通知栏播放状态
- 基础生命周期处理

### 4.2 进阶版本

在 MVP 完成后，可以继续扩展：

- 收藏歌曲
- 最近播放
- 播放模式切换
- 本地音乐扫描
- 简易歌词展示
- 自定义播放进度条
- 夜间模式
- 使用 Room 保存播放历史
- 使用 MVVM 重构项目结构

## 5. 功能需求

## 5.1 音乐列表页

### 5.1.1 功能说明

音乐列表页用于展示当前可播放的音乐列表。MVP 阶段可以使用内置在 res/raw 或 assets 中的测试音频，也可以使用模拟数据展示歌曲名称、歌手、时长和封面。

### 5.1.2 页面元素

- 页面标题：音乐播放器
- 音乐列表 RecyclerView
- 每个音乐条目包含：
    - 歌曲名称
    - 歌手名称
    - 歌曲时长
    - 专辑封面或默认图标
    - 当前播放标识
- 底部迷你播放器：
    - 当前歌曲名称
    - 播放/暂停按钮
    - 下一首按钮

### 5.1.3 交互规则

- 点击某首歌曲后，开始播放该歌曲。
- 点击歌曲条目后，进入播放详情页。
- 当前正在播放的歌曲需要在列表中有明显状态标识。
- 点击底部迷你播放器区域，进入播放详情页。
- 点击底部播放/暂停按钮，可以控制当前歌曲播放状态。

### 5.1.4 学习重点

- RecyclerView
- Adapter
- ViewHolder
- item 点击事件
- Activity/Fragment 页面组织
- 列表状态刷新

## 5.2 播放详情页

### 5.2.1 功能说明

播放详情页展示当前歌曲的详细信息，并提供完整播放控制。

### 5.2.2 页面元素

- 返回按钮
- 歌曲封面
- 歌曲名称
- 歌手名称
- 当前播放时间
- 总时长
- 播放进度条
- 播放/暂停按钮
- 上一首按钮
- 下一首按钮
- 播放模式按钮
- 收藏按钮，进阶功能

### 5.2.3 交互规则

- 点击播放/暂停按钮，切换播放状态。
- 点击上一首，播放列表中的上一首歌曲。
- 点击下一首，播放列表中的下一首歌曲。
- 拖动进度条，跳转到指定播放位置。
- 当前播放时间需要随播放进度实时更新。
- 歌曲播放完成后，根据播放模式决定下一步行为。

### 5.2.4 学习重点

- SeekBar
- Handler 定时刷新 UI
- MediaPlayer 进度控制
- Activity 生命周期
- 页面状态同步

## 5.3 音乐播放控制

### 5.3.1 功能说明

系统需要支持基础音乐播放控制。

### 5.3.2 功能列表

- 播放指定歌曲
- 暂停当前歌曲
- 继续播放当前歌曲
- 停止播放
- 播放上一首
- 播放下一首
- 跳转到指定进度
- 监听播放完成事件
- 获取当前播放状态
- 获取当前播放进度

### 5.3.3 播放状态

播放器至少需要维护以下状态：

- IDLE：未初始化
- PREPARING：准备中
- PLAYING：播放中
- PAUSED：已暂停
- COMPLETED：播放完成
- ERROR：播放异常

### 5.3.4 学习重点

- MediaPlayer 生命周期
- 状态机思维
- 异常处理
- 资源释放

## 5.4 后台播放 Service

### 5.4.1 功能说明

音乐播放逻辑应放在 Service 中，页面只负责展示状态和发送控制指令。

### 5.4.2 功能要求

- App 进入后台后，音乐继续播放。
- 页面销毁后，Service 不应因为 Activity 销毁而立即中断播放。
- Service 需要向页面提供当前播放状态。
- Activity 或 Fragment 可以绑定 Service 获取控制能力。
- 当用户主动退出播放时，Service 需要释放 MediaPlayer 资源。

### 5.4.3 学习重点

- Service
- bindService
- startService
- Service 生命周期
- 前台服务
- Activity 与 Service 通信

## 5.5 通知栏控制

### 5.5.1 功能说明

播放音乐时，系统通知栏需要显示当前播放信息，并提供基础控制按钮。

### 5.5.2 通知内容

- 歌曲名称
- 歌手名称
- 播放状态
- 播放/暂停按钮
- 上一首按钮
- 下一首按钮
- 点击通知回到播放详情页

### 5.5.3 交互规则

- 播放音乐时显示通知。
- 暂停音乐时通知仍可保留。
- 停止播放或退出播放器时取消通知。
- 点击通知中的按钮后，播放器状态需要同步更新。

### 5.5.4 学习重点

- Notification
- NotificationChannel
- PendingIntent
- BroadcastReceiver
- 前台服务通知

## 5.6 播放模式

### 5.6.1 MVP 播放模式

MVP 阶段至少支持顺序播放。

### 5.6.2 进阶播放模式

后续可以扩展：

- 顺序播放
- 单曲循环
- 列表循环
- 随机播放

### 5.6.3 交互规则

- 点击播放模式按钮切换模式。
- 切换后需要有 Toast 或图标变化提示。
- 歌曲播放完成后，根据当前播放模式执行下一步。

## 5.7 收藏功能，进阶

### 5.7.1 功能说明

用户可以收藏或取消收藏歌曲。

### 5.7.2 功能要求

- 播放详情页可以点击收藏按钮。
- 列表页可以展示收藏状态。
- 收藏数据需要在 App 重启后保留。

### 5.7.3 实现建议

MVP 后可以先用 SharedPreferences 保存收藏歌曲 ID，再进阶使用 Room 数据库。

### 5.7.4 学习重点

- SharedPreferences
- 数据持久化
- UI 状态恢复

## 5.8 最近播放，进阶

### 5.8.1 功能说明

记录用户最近播放过的歌曲。

### 5.8.2 功能要求

- 播放歌曲时记录播放时间。
- 最近播放列表按时间倒序展示。
- 同一首歌曲重复播放时，只保留最新记录。

### 5.8.3 学习重点

- 数据结构设计
- 本地持久化
- 列表排序

## 6. 非功能需求

## 6.1 性能需求

- 列表滚动应保持流畅。
- 播放控制响应时间应小于 300ms。
- 播放进度刷新间隔建议为 500ms 或 1000ms。
- 页面切换不应造成音乐卡顿。

## 6.2 稳定性需求

- 页面切换、旋转屏幕、App 切后台时不应崩溃。
- MediaPlayer 资源需要正确释放。
- 播放异常时需要给出 Toast 提示。
- 当前歌曲不存在或加载失败时，需要跳过或进入错误状态。

## 6.3 兼容性需求

- 需要适配 Android 8.0 及以上版本。
- Android 8.0 及以上必须创建 NotificationChannel。
- 如果后续加入本地音乐扫描，需要单独处理 Android 10 及以上的存储权限变化。

## 6.4 可维护性需求

- 播放逻辑、页面逻辑、数据模型应分层。
- 不建议把所有逻辑写在 MainActivity 中。
- 常量、状态、事件 Action 应统一管理。
- 每个核心类职责清晰，便于后续重构为 MVVM。

## 7. 页面设计

## 7.1 页面结构

建议 MVP 使用两个主要页面：

- MainActivity：承载音乐列表页和底部迷你播放器。
- PlayerActivity 或 PlayerFragment：播放详情页。

也可以采用单 Activity 多 Fragment：

- MainActivity
    - MusicListFragment
    - PlayerFragment

初学阶段建议先使用 Activity + Fragment 混合方式，方便理解页面跳转和 Fragment 生命周期。

## 7.2 音乐列表页布局建议

推荐控件：

- ConstraintLayout：根布局。
- RecyclerView：音乐列表。
- LinearLayout 或 ConstraintLayout：底部迷你播放器。
- TextView：歌曲名、歌手名、时长。
- ImageView：封面。
- Button 或 ImageButton：播放/暂停、下一首。

## 7.3 播放详情页布局建议

推荐控件：

- ConstraintLayout：根布局。
- ImageView：歌曲封面。
- TextView：歌曲名、歌手、当前时间、总时长。
- SeekBar：播放进度。
- ImageButton：上一首、播放/暂停、下一首、播放模式。

## 8. 数据模型

## 8.1 Music 数据模型

建议字段：

- id：歌曲唯一 ID
- title：歌曲名称
- artist：歌手名称
- duration：歌曲时长
- coverResId：封面资源 ID
- audioResId：音频资源 ID
- isFavorite：是否收藏

## 8.2 PlayerState 数据模型

建议字段：

- currentMusic：当前歌曲
- currentIndex：当前歌曲索引
- playbackState：播放状态
- currentPosition：当前播放进度
- duration：当前歌曲总时长
- playMode：播放模式

## 8.3 PlayMode 播放模式

建议枚举：

- SEQUENCE：顺序播放
- LOOP_LIST：列表循环
- LOOP_ONE：单曲循环
- SHUFFLE：随机播放

MVP 可以先只实现 SEQUENCE，其他模式保留接口。

## 9. 组件设计

## 9.1 MainActivity

职责：

- 初始化主页面。
- 展示音乐列表。
- 绑定 MusicPlayerService。
- 接收播放状态变化并刷新 UI。
- 处理底部迷你播放器交互。

## 9.2 PlayerActivity 或 PlayerFragment

职责：

- 展示当前歌曲详情。
- 控制播放、暂停、上一首、下一首。
- 展示和拖动播放进度。
- 监听播放状态并刷新 UI。

## 9.3 MusicPlayerService

职责：

- 持有 MediaPlayer。
- 管理播放器状态。
- 提供播放控制方法。
- 更新通知栏。
- 向页面回调当前状态。
- 释放播放资源。

## 9.4 MusicRepository

职责：

- 提供音乐列表数据。
- MVP 阶段返回本地模拟数据。
- 后续可扩展为读取本地音乐或数据库。

## 9.5 NotificationReceiver

职责：

- 接收通知栏按钮事件。
- 将播放、暂停、上一首、下一首等动作转发给 Service。

## 10. 组件通信设计

## 10.1 页面到 Service

页面通过绑定 Service 获取播放器控制对象，调用以下方法：

- play(musicId)
- pause()
- resume()
- playNext()
- playPrevious()
- seekTo(position)
- getPlayerState()

## 10.2 Service 到页面

Service 可以通过回调接口或广播通知页面状态变化。

初学阶段推荐先使用回调接口，便于理解对象通信；进阶阶段可以尝试使用 LiveData、StateFlow 或广播。

## 10.3 通知栏到 Service

通知栏按钮通过 PendingIntent 发送 Action，由 BroadcastReceiver 或 Service 处理：

- ACTION_PLAY_PAUSE
- ACTION_NEXT
- ACTION_PREVIOUS
- ACTION_STOP

## 11. 权限需求

## 11.1 MVP 阶段

如果只播放应用内置音频资源，不需要读取外部存储权限。

## 11.2 本地音乐扫描阶段

如果扫描手机本地音乐，需要根据 Android 版本申请权限：

- Android 12 及以下：READ_EXTERNAL_STORAGE
- Android 13 及以上：READ_MEDIA_AUDIO

需要在运行时申请权限，并处理用户拒绝权限的情况。

## 12. 异常场景

系统至少需要考虑以下异常：

- 音频文件加载失败。
- 播放过程中 MediaPlayer 抛出错误。
- 用户快速连续点击播放按钮。
- 页面销毁后回调仍然尝试刷新 UI。
- App 切后台后通知栏状态不同步。
- 歌曲播放完成后列表越界。
- 横竖屏切换导致状态丢失。

## 13. 验收标准

## 13.1 MVP 验收标准

- App 可以正常启动。
- 音乐列表可以正常展示。
- 点击歌曲后可以播放音乐。
- 播放详情页可以显示当前歌曲信息。
- 播放、暂停、上一首、下一首功能可用。
- 播放进度可以随音乐播放更新。
- 拖动进度条可以跳转播放位置。
- App 进入后台后音乐仍可继续播放。
- 通知栏可以显示当前歌曲和播放状态。
- 通知栏按钮可以控制播放。
- 返回列表页后，播放状态仍能正确显示。
- 退出播放时 MediaPlayer 资源能释放。
- 常见页面切换和后台切换不会崩溃。

## 13.2 进阶验收标准

- 支持多种播放模式。
- 支持收藏歌曲。
- 支持最近播放。
- 支持扫描本地音乐。
- 支持自定义播放进度条或播放动画。
- App 重启后能恢复部分用户状态。

## 14. 学习里程碑

## 14.1 第 1 阶段：项目搭建

目标：

- 创建 Android 项目。
- 熟悉目录结构。
- 准备测试音频和图片资源。
- 完成 Git 初始化，建议每个阶段提交一次代码。

产出：

- App 能启动。
- 首页能显示静态标题。

## 14.2 第 2 阶段：音乐列表

目标：

- 创建 Music 数据模型。
- 创建 MusicRepository。
- 使用 RecyclerView 展示歌曲列表。
- 实现 item 点击事件。

产出：

- 首页可以展示模拟音乐列表。
- 点击条目可以拿到对应 Music 对象。

## 14.3 第 3 阶段：基础播放

目标：

- 引入 MediaPlayer。
- 实现播放、暂停、继续播放。
- 实现播放完成监听。

产出：

- 点击列表歌曲后可以播放音乐。
- 播放按钮状态可以变化。

## 14.4 第 4 阶段：播放详情页

目标：

- 创建播放详情页。
- 展示歌曲名称、歌手、封面、进度。
- 实现上一首、下一首、拖动进度。

产出：

- 完整播放详情页可用。
- 进度条可以正常更新。

## 14.5 第 5 阶段：Service 与后台播放

目标：

- 将播放逻辑移动到 MusicPlayerService。
- Activity 绑定 Service。
- 页面切后台后音乐继续播放。

产出：

- 播放状态不依赖单个 Activity 生命周期。
- App 后台播放稳定。

## 14.6 第 6 阶段：通知栏控制

目标：

- 创建 NotificationChannel。
- 创建播放通知。
- 实现通知栏播放控制。

产出：

- 播放时通知栏显示歌曲信息。
- 通知栏按钮可控制播放。

## 14.7 第 7 阶段：进阶能力

目标：

- 增加播放模式。
- 增加收藏或最近播放。
- 尝试自定义 View 或动画。

产出：

- Demo 具备可展示性。
- 项目结构便于继续扩展。

## 15. 建议目录结构

```text
app/src/main/java/你的包名/
  data/
    Music.java
    MusicRepository.java
  player/
    MusicPlayerService.java
    PlayerState.java
    PlayMode.java
  ui/
    MainActivity.java
    PlayerActivity.java
    MusicListAdapter.java
  receiver/
    NotificationReceiver.java
  util/
    TimeFormatUtil.java
    Constants.java
```

## 16. 开发顺序建议

建议不要一开始就写 Service 和通知栏。推荐顺序如下：

1. 先完成静态页面。
2. 再完成 RecyclerView 列表。
3. 再用 Activity 内部的 MediaPlayer 跑通最小播放链路。
4. 播放链路稳定后，再把播放逻辑迁移到 Service。
5. 最后增加通知栏、播放模式和持久化能力。

这样可以降低初学阶段的调试难度，也能更清楚地理解每一次重构的原因。

## 17. 关键学习问题

开发过程中需要主动思考以下问题：

- 为什么播放逻辑不应该长期放在 Activity 中？
- Activity 销毁后，MediaPlayer 会发生什么？
- Service 和 Activity 如何通信？
- 为什么不能在子线程直接更新 UI？
- Handler 为什么可以刷新播放进度？
- Notification 的 PendingIntent 是如何回到 App 或控制播放的？
- RecyclerView 为什么需要 Adapter 和 ViewHolder？
- 播放状态如何同步到多个页面？
- 横竖屏切换时，当前播放状态如何保留？

## 18. 风险与注意事项

- 音频文件需要使用可合法测试的资源，不要随意打包版权音乐。
- MediaPlayer 使用后必须 release，避免资源泄漏。
- Handler 刷新进度时，需要在页面销毁时停止回调，避免内存泄漏。
- Android 8.0 以上通知必须配置 NotificationChannel。
- 如果使用前台服务，需要注意 Android 版本对前台服务的限制。
- 本地音乐扫描涉及权限和系统版本差异，不建议放入第一版。

## 19. 最终交付物

MVP 阶段最终应交付：

- 可运行的 Android 项目源码。
- 至少 3 首可播放的测试音乐。
- 音乐列表页。
- 播放详情页。
- 后台播放 Service。
- 通知栏播放控制。
- 简单 README，说明项目功能、运行方式和学习到的知识点。

## 20. 后续扩展方向

完成 MVP 后，可以按以下方向继续深入：

- 使用 Room 保存收藏和最近播放。
- 使用 ViewModel + LiveData 重构状态管理。
- 使用 ExoPlayer 替代 MediaPlayer。
- 使用 Material Design 优化 UI。
- 使用自定义 View 绘制圆形进度条。
- 使用 ContentResolver 扫描本地音乐。
- 使用前台服务优化长期播放稳定性。
- 增加歌词解析和滚动歌词。

