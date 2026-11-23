# YIClass - 智能课表管理应用

一个用 Kotlin 和 Jetpack Compose 开发的 Android 原生课表应用，支持多种视图、AI 智能导入、教务导入和文件备份。

**当前版本** v1.0.0

## 核心功能

### 视图模式

- **周视图**展示整周课程，合并连续节次，标出同一时段的课程冲突。
- **日视图**专注单日课程，以彩色边框区分课程。
- **列表视图**按周次分组，以卡片展示课程。

### 课程管理

- 添加、编辑和删除课程，可设置课程颜色、地点、教师和上课时间。
- 创建、重命名、切换和管理多个课表。
- 设置学期开始日期、总周数、各节次的起止时间和周末显示。

### 智能导入

- **AI 图片识别**支持从课表图片提取课程信息。
- **AI 表格解析**支持粘贴表格或 CSV 格式的课程信息。
- **AI 文字识别**支持从自然语言描述中提取课程安排。
- **AI 教务导入**支持分析在应用内打开的教务网页。
- **教务脚本导入**支持配置脚本仓库，从学校教务系统获取课表。
- **文件导入和导出**支持用 JSON 备份及恢复课表。

### 其他功能

- 首次启动自动创建默认课表。
- 支持停课和补课，可导入 2025–2027 年中国大陆官方放假和调休日期。调休日按学校安排选择补课星期。
- 支持课前提醒、深色模式和本地数据存储。

## 快速开始

### 运行要求

- Android 10（API 29）或更新版本。
- Android SDK Platform 37.0、Build Tools 36.0.0 和 JDK 17 或更新版本用于本地构建。

### 安装步骤

在 Android Studio 中打开 Kotlin 项目，完成 Gradle 同步后，选择模拟器或真机运行。命令行可通过 `ANDROID_HOME` 指定 SDK 目录，并在项目根目录运行下面的命令。

```bash
./gradlew :app:assembleDebug
```

调试 APK 位于 `app/build/outputs/apk/debug/`。Windows 用户可使用 `gradlew.bat`。

如需构建签名版，在项目根目录配置 `key.properties`，填入 `storeFile`、`storePassword`、`keyAlias` 和 `keyPassword`。`storeFile` 的相对路径以 `app/` 为基准。

```bash
./gradlew :app:assembleRelease
```

正式 APK 位于 `app/build/outputs/apk/release/`。

### AI 功能配置（可选）

1. 在设置中启用「高级功能」，打开「AI 导入配置」。
2. 选择 OpenAI、Claude、Gemini 或 DeepSeek 预设，或填写自己的 OpenAI 兼容接口。
3. 输入 API Key，选择文本模型。启用图片导入时需要填写视觉模型。
4. 打开要使用的导入方式并保存配置，再回到「课表管理」打开「AI 导入」。

## 数据结构说明

### 课程模型

每门课程包含名称、地点、教师、ARGB 颜色和一组上课时间。上课时间用三个字段表示。

| 字段 | 说明 |
| --- | --- |
| `weekday` | 星期几，`1` 为周一，`7` 为周日 |
| `periods` | 节次列表，`[1, 2]` 表示第 1–2 节 |
| `weekPattern` | 上课周次列表，`[1, 3, 5]` 表示第 1、3、5 周 |

### 课表设置

课表保存学期开始日期、总周数、最大节次、周末显示、节次时间、课前提醒和停课补课安排。总周数支持 1–20 周，最大节次支持 1–16 节。

### JSON 导入导出格式

Kotlin 版使用 `yiclass-timetable` 版本 `1` 作为备份格式。导入备份会在设备上创建一份新课表。

```json
{
  "format": "yiclass-timetable",
  "version": 1,
  "timetable": {
    "name": "秋季课表",
    "settings": {
      "startDate": "2026-09-07",
      "totalWeeks": 18,
      "showWeekend": false,
      "maxPeriods": 12,
      "reminderMinutes": 15,
      "classTimes": [
        { "period": 1, "startTime": "08:00", "endTime": "08:45" }
      ],
      "calendar": [
        { "date": "2026-10-01" }
      ]
    },
    "courses": [
      {
        "name": "高等数学",
        "location": "教学楼 A101",
        "teacher": "张老师",
        "color": 4290502395,
        "schedules": [
          { "weekday": 1, "periods": [1, 2], "weekPattern": [1, 2, 3] }
        ]
      }
    ]
  }
}
```

## 技术架构

### 核心组件

- **界面**使用 Jetpack Compose、Material 3 和类型安全的 Compose Navigation。
- **状态管理**使用 ViewModel、StateFlow 和 SavedStateHandle。
- **数据存储**使用 Room 3 存储课表，DataStore 保存应用设置。
- **课程提醒**使用 WorkManager 和 AlarmManager。
- **导入和备份**使用 OkHttp、Kotlin 序列化以及 Android 系统文件选择器。

依赖版本统一配置在 [Gradle Version Catalog](gradle/libs.versions.toml) 中。

### 项目结构

```text
app/src/
├── main/
│   ├── java/wykwey/yiclass/
│   │   ├── data/           # 数据库、仓库、AI 服务和提醒调度
│   │   ├── di/             # Hilt 依赖注入
│   │   ├── domain/         # 课表模型、备份和课程查询
│   │   └── presentation/   # 页面、组件、导航和 ViewModel
│   └── res/                # 图标、主题和其他 Android 资源
└── test/                   # 单元测试
```

## 贡献指南

欢迎反馈问题和提交改动。提交前可运行单元测试和 Lint。

```bash
./gradlew :app:testDebugUnitTest :app:lintDebug
```

## 常见问题

### 如何添加或导入课表？

点周视图中的空白节次可以添加课程。在设置中的「课表管理」里，可以新建课表或导入 JSON。启用高级功能后，也可以使用教务脚本或已配置的 AI 导入方式。

### 如何导出课表？

先切换到要备份的课表，再在「设置 → 课表管理」中点「导出课表」，选择 JSON 文件的保存位置。

### 课程时间冲突如何处理？

周视图会标出发生冲突的课程。点击冲突单元格可以选择课程，再进入编辑页调整时间。

### 数据存储在哪里？

课程和设置保存在设备上。使用 AI 导入时，选中的图片、文本或网页内容会发送到你配置的 AI 服务。

## 原项目

[Flutter 版 YIClass](https://github.com/wykwey/YIClass)
