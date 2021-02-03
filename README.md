# Zap

> 基于 mmap, 高性能、高可用的 Android 日志收集框架

## Android 日志库 Zap

使用 mmap 文件映射内存作为缓存，可以在不牺牲性能的前提下最大化的保证日志的完整性。
日志首先会写入到 mmap 文件映射内存中，基于 mmap 的特性，即使用户强杀了进程，日志文件也不会丢失，并且会在下次初始化 Zap 的时候回写到日志文件中。

针对一些用户反馈难以复现的线上问题，分析日志有时候是解决问题的必要手段。
但是日志的收集一直有个痛点，就是性能与日志完整性无法兼得。
要实现高性能的日志收集，势必要使用大量内存，先将日志写入内存中，然后在合适的时机将内存里的日志写入到文件系统中（flush），
如果在 flush 之前用户强杀了进程，那么内存里的内容会因此而丢失。
日志实时写入文件可以保证日志的完整性，但是写文件是 IO 操作，涉及到用户态与内核态的切换，相比较直接写内存会更耗时，UI 线程中频繁的写文件会造成卡顿，影响用户体验。

## 使用

### 在Application中初始化

sdk提供默认的初始化方法Zap.default()，自定义初始化方法请参考Zap.kt#default()实现

```kotlin
	/**
	 * 默认的初始化方法
	 *
	 * @param application   上下文对象
	 * @param past  保存时间（天）
	 * @param debug debug模式
	 * @return
	 */
	@JvmStatic
	@JvmOverloads
	fun default(application: Application, past: Int = DEFAULT_PAST_TIME, debug: Boolean = false): Boolean
```

### 打印日志

和android.util.Log使用方法一致，默认提供DEBUG、INFO、ERROR三种级别日志，更详细的使用方法请参考demo

```kotlin
	Zap.d("DEBUG级别日志测试")
	Zap.i("INFO级别日志测试")
	Zap.e("ERROR级别日志测试")
```

### 写日志

```kotlin
	override fun onPause() {
		super.onPause()
		Zap.flush()
		}
```

### 释放Zap实例

```kotlin
	override fun onDestroy() {
		super.onDestroy()
		Zap.release()
		}
```

## 参考

- [Tencent/mars](https://github.com/Tencent/mars)
- [XLog](https://github.com/elvishew/xLog)