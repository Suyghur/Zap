package cn.flyfun.zap.appender

import android.content.Context
import android.os.Environment
import android.text.TextUtils
import cn.flyfun.zap.Level
import cn.flyfun.zap.ZapBuffer
import cn.flyfun.zap.formatter.IFormatter
import cn.flyfun.zap.interceptor.IInterceptor
import java.io.File

/**
 * @author #Suyghur,
 * Created on 2021/1/19
 */
class FileAppender(builder: Builder) : AbsAppender() {

    private var logBuffer: ZapBuffer
    private lateinit var formatter: IFormatter

    init {
        logBuffer = ZapBuffer(builder.bufferFilePath, builder.bufferSize, builder.logFilePath, builder.compress)
        maxSingleLength = builder.bufferSize
        setLevel(builder.level)
        addInterceptor(builder.interceptors)
        builder.formatter?.apply {
            formatter = this
        }
    }

    override fun doAppend(level: Int, tag: String, msg: String) {
        logBuffer.write(formatter.format(level, tag, msg))
    }

    override fun flush() {
        super.flush()
        logBuffer.flushAsync()
    }

    override fun release() {
        super.release()
        logBuffer.release()
    }

    class Builder(private val context: Context) {

        internal var bufferFilePath = ""
        internal var logFilePath = ""
        internal var bufferSize = 4096
        internal var level = Level.VERBOSE
        internal var interceptors: MutableList<IInterceptor> = mutableListOf()
        internal var formatter: IFormatter? = null
        internal var compress = false

        fun setBufferSize(bufferSize: Int): Builder {
            this.bufferSize = bufferSize
            return this
        }

        fun setBufferFilePath(bufferFilePath: String): Builder {
            this.bufferFilePath = bufferFilePath
            return this
        }

        fun setLogFilePath(logFilePath: String): Builder {
            this.logFilePath = logFilePath
            return this
        }

        fun setLevel(level: Int): Builder {
            this.level = level
            return this
        }

        fun addInterceptor(interceptor: IInterceptor): Builder {
            interceptors.add(interceptor)
            return this
        }

        fun setFormatter(formatter: IFormatter): Builder {
            this.formatter = formatter
            return this
        }

        fun setCompress(compress: Boolean): Builder {
            this.compress = compress
            return this
        }

        fun create(): FileAppender {
            if (TextUtils.isEmpty(logFilePath)) {
                throw IllegalArgumentException("log file path cannot be null")
            }

            if (TextUtils.isEmpty(bufferFilePath)) {
                bufferFilePath = getDefaultBufferPath(context)
            }
            if (formatter == null) {
                formatter = object : IFormatter {
                    override fun format(level: Int, tag: String, msg: String): String {
                        return String.format("%s/%s: %s\n", Level.getShortLevelName(level), tag, msg)
                    }
                }
            }
            return FileAppender(this)
        }

        private fun getDefaultBufferPath(context: Context): String {
            val bufferFile: File = if ((Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) && context.getExternalFilesDir("zap") != null) {
                context.getExternalFilesDir("zap")!!
            } else {
                File(context.filesDir, "zap")
            }
            if (!bufferFile.exists()) {
                bufferFile.mkdirs()
            }
            return File(bufferFile, ".zapCache").absolutePath
        }

    }
}