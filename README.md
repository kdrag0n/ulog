# µlog

Simple, fast, and efficient logging facade for Android apps.

Inspired by Timber and Logcat.

## Features

- Lazy message evaluation
- Pluggable backends (like Timber)
- Fast automatic class name tags for debug
- Easy to specify explicit tags when necessary (optional Kotlin named argument)
- Debug and verbose logs completely optimized out from release builds

These features improve performance (no more expensive toString calls accidentally left in release builds), reduce code size, and help make obfuscation more effective for sensitive code.

## Usage

µlog isn’t available as a library because one of its key features, stripping debug and verbose logs in release builds, depends on BuildConfig. Instead, simply add [Ulog.kt](Ulog.kt) to your project and adjust the BuildConfig import.

Install a backend at early init, e.g. in Application#onCreate:

```kotlin
Ulog.installBackend(SystemLogBackend())
```

Then log away:

```kotlin
logV { "Call func" }
logD { "Debug: $data" }
logI(TAG) { "Starting service" }
logW(TAG, e) { "Unexpected error" }
logE(e) { "Failed to fetch data" }
```
