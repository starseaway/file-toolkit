# File Toolkit 文件工具包

<div align="center">
  <img src="file-toolkit-logo.svg" width="500" alt="file-toolkit-logo">
</div>

![Version](https://img.shields.io/badge/version-1.1.0-blue)
![License](https://img.shields.io/badge/license-Apache%202.0-green)
![API](https://img.shields.io/badge/API-19%2B-brightgreen)

## 一、模块简介

File Toolkit 是一个文件和目录操作的工具包，帮你轻松完成创建、读取、写入、复制、移动、重命名、删除，以及压缩和解压等常用操作。

除了这些，它还提供路径管理、文件大小计算、目录列表读取等辅助功能，让日常文件处理更方便，也为未来功能扩展预留了空间。

## 特点：
- 创建、读取、写入、重命名、删除文件和目录
- 复制与移动文件和目录
- 计算和格式化文件/目录大小
- 获取目录下文件列表、按行读取文件内容
- 获取应用内部和外部文件路径
- 压缩与解压（标准与加密支持，带进度回调）

## 二、使用说明

### 1. 根据 Gradle 版本或项目配置，自行选择在合适的位置添加仓库地址

```groovy
maven {
    // jitpack仓库
    url 'https://jitpack.io'
}
```

### 2.在 `build.gradle` (Module 级) 中添加依赖：

```groovy
dependencies {
    implementation 'com.github.starseaway:file-toolkit:1.1.0'
}
```

```kotlin
dependencies {
    implementation("com.github.starseaway:file-toolkit:1.1.0")
}
```

## 三、快速开始

下面的示例展示了如何使用 File Toolkit 快速完成文件创建、写入/读取、压缩和解压（普通及加密），并带注释说明

```kotlin
fun main() {
    try {
        // 创建基础目录（示例目录）
        val baseDir = "test_files"
        FileOperation.createOrExistsDir(baseDir)

        // 创建日期文件，文件名唯一
        val txtFile = FileOperation.createDateNewFile(baseDir, "sample", ".txt")
        Log.d("File", "创建文件: ${txtFile.absolutePath}")

        // 写入字符串到文件（覆盖模式）
        val content = "Hello, File Toolkit!"
        FileIOUtil.writeFileFromString(txtFile, content, isCover = true)
        Log.d("File", "写入内容成功")

        // 读取文件内容为字符串
        val readContent = FileIOUtil.readFileToString(txtFile)
        Log.d("File", "读取文件内容:\n$readContent")

        // 写入多行内容
        val lines = listOf("第一行", "第二行", "第三行")
        FileIOUtil.writeLinesToFile(txtFile, lines, isAppend = false)
        Log.d("File", "写入多行成功")

        // 按行读取文件
        val readLines = FileIOUtil.readFileToLines(txtFile, charset = "UTF-8")
        Log.d("File", "按行读取内容: $readLines")

        // 普通 ZIP 压缩
        val zipFile = File(baseDir, "archive.zip")
        val zipSuccess = StandardZipUtil.zipFile(txtFile, zipFile)
        Log.d("File", "普通压缩成功: $zipSuccess")

        // 解压 ZIP
        val unzipDir = File(baseDir, "unzip")
        StandardZipUtil.unzipFile(zipFile, unzipDir)
        Log.d("File", "解压完成到目录: ${unzipDir.absolutePath}")

        // AES 加密压缩
        val encryptedZipPath = EncryptedZipUtil.zipEncrypt(baseDir, "$baseDir/", isCreateDir = true, passwd = "123456")
        Log.d("File", "加密压缩文件路径: $encryptedZipPath")

        // Handler 处理解压进度回调
        val handler = Handler { msg: Message ->
            when (msg.what) {
                EncryptedZipUtil.CompressStatus.START -> Log.d("File", "解压开始")
                EncryptedZipUtil.CompressStatus.HANDLING -> {
                    val percent = msg.data.getInt(EncryptedZipUtil.CompressKeys.PERCENT)
                    Log.d("File", "解压中: $percent%")
                }
                EncryptedZipUtil.CompressStatus.COMPLETED -> Log.d("File", "解压完成")
                EncryptedZipUtil.CompressStatus.ERROR -> {
                    val error = msg.data.getString(EncryptedZipUtil.CompressKeys.ERROR)
                    Log.e("File", "解压错误: $error")
                }
            }
            true
        }
        // 带进度回调的加密 ZIP 解压
        EncryptedZipUtil.unzip(File(encryptedZipPath),
            dest = "$baseDir/encrypted_unzip",
            passwd = "123456",
            charset = "UTF-8",
            handler = handler,
            isDeleteZipFile = false  // 解压完成后是否删除源 ZIP 文件
        )
    } catch (ex: Exception) {
        // 生产环境建议针对具体异常处理
        Log.e("File", "文件操作异常: ", ex)
    }
}
```

## 四、目录结构

```text
com/xinyi/file/
│
├── entity/
│   └── FileEntity.java           # 文件信息实体类，封装一个文件的基本信息
│
├── io/
│   ├── FileBasicUtil.java        # 基础文件工具类（获取 File 对象、判断存在性、获取扩展名等）
│   ├── FileOperation.java        # 文件操作类（创建、删除、重命名文件等）
│   ├── FileCopyMove.java         # 文件/目录复制与移动操作
│   ├── FileSizeUtil.java         # 文件/目录大小计算及格式化
│   ├── FileListUtil.java         # 目录文件列表读取及行读取操作
│   └── FileIOUtil.java           # 文件流操作工具，封装常用的输入/输出操作
│
├── path/
│   └── AppFilePathUtil.java      # 获取应用内部和外部文件路径的工具类
│
└── zip/
    ├── StandardZipUtil.java      # 基于 java.util.zip 的标准压缩和解压功能（不支持加密）
    └── EncryptedZipUtil.java     # 基于 Zip4j 封装的压缩/解压工具，支持加密、分卷压缩和进度回调
```

## 五、版本变更记录

### V1.1.0 (2026-03-30)
- 更改包名，修改目录结构
- 提升工具类方法调用的一致性

### V1.0.1 (2025-04-29)
- 新增根据后缀获取指定目录下的所有文件信息列表的方法（不包含隐藏文件）

### V1.0.0 (2025-03-31)
- 初始版本，提供基础文件管理、路径管理、压缩解压等工具类