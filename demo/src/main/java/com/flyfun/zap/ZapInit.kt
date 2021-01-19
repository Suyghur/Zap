package com.flyfun.zap

import android.content.Context
import cn.flyfun.zap.Interceptor.IInterceptor
import cn.flyfun.zap.Level
import cn.flyfun.zap.ZapData
import cn.flyfun.zap.Zap
import cn.flyfun.zap.appender.AndroidAppender
import cn.flyfun.zap.appender.FileAppender
import cn.flyfun.zap.formatter.DateFileFormatter
import cn.flyfun.zap.logger.AppenderLogger
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * @author #Suyghur,
 * Created on 2021/1/18
 */
object ZapInit {

    private const val BUFFER_SIZE = 1024 * 400

    @JvmStatic
    fun install(context: Context) {
        val level = Level.VERBOSE
        val wrapInterceptor = object : IInterceptor {
            override fun intercept(data: ZapData): Boolean {
//                data.tag = "Zap-${data.tag}"
                return true
            }
        }
        val appender = AndroidAppender.Builder().setLevel(level).addInterceptor(wrapInterceptor).create()

        val log = getLogDir(context)
        val bufferPath = log.absolutePath + File.separator + ".zapCache"
        val time = SimpleDateFormat("yyyy_MM_dd", Locale.getDefault()).format(Date())
        val logPath = log.absolutePath + File.separator + time + ".txt"
        val fileAppender = FileAppender.Builder(context)
                .setLogFilePath(logPath)
                .setLevel(level)
                .addInterceptor(wrapInterceptor)
                .setBufferFilePath(bufferPath)
                .setFormatter(DateFileFormatter())
                .setCompress(false)
                .setBufferSize(BUFFER_SIZE)
                .create()

        Zap.iLoggerDelegate = AppenderLogger.Builder()
                .addAppender(appender)
                .addAppender(fileAppender)
                .create()
    }

    private fun getLogDir(context: Context): File {
        var path = context.getExternalFilesDir("zap")
        if (path == null) {
            path = File(context.filesDir, "zap")
        }
        if (!path.exists()) {
            path.mkdirs()
        }
        return path
    }
}