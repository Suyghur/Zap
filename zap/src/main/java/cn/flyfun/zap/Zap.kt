package cn.flyfun.zap

import android.app.Application
import android.content.Context
import android.os.Process
import cn.flyfun.zap.appender.AndroidAppender
import cn.flyfun.zap.appender.FileAppender
import cn.flyfun.zap.formatter.DateFileFormatter
import cn.flyfun.zap.interceptor.IInterceptor
import cn.flyfun.zap.logger.AndroidLogger
import cn.flyfun.zap.logger.AppenderLogger
import cn.flyfun.zap.logger.ILogger
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.net.UnknownHostException
import java.text.SimpleDateFormat
import java.util.*

/**
 * @author #Suyghur,
 * Created on 2021/1/19
 */
object Zap {

    private var iLoggerDelegate: ILogger = AndroidLogger()
    private var mUncaughtExceptionHandler: Thread.UncaughtExceptionHandler? = null

    private const val DEFAULT_BUFFER_SIZE = 1024 * 400

    @JvmStatic
    fun v(tag: String, msg: String) {
        println(Level.VERBOSE, tag, msg)
    }

    @JvmStatic
    fun d(tag: String, msg: String) {
        println(Level.DEBUG, tag, msg)
    }

    @JvmStatic
    fun i(tag: String, msg: String) {
        println(Level.INFO, tag, msg)
    }

    @JvmStatic
    fun w(tag: String, msg: String) {
        println(Level.WARN, tag, msg)
    }

    @JvmStatic
    fun w(tag: String, msg: String, throwable: Throwable) {
        println(Level.WARN, tag, msg + "\n${getStackTraceString(throwable)}")
    }

    @JvmStatic
    fun w(tag: String, throwable: Throwable) {
        println(Level.WARN, tag, getStackTraceString(throwable))
    }

    @JvmStatic
    fun e(tag: String, msg: String) {
        println(Level.ERROR, tag, msg)
    }

    @JvmStatic
    fun e(tag: String, msg: String, throwable: Throwable) {
        println(Level.ERROR, tag, msg + "\n${getStackTraceString(throwable)}")
    }

    @JvmStatic
    fun e(tag: String, throwable: Throwable) {
        println(Level.ERROR, tag, getStackTraceString(throwable))
    }

    @JvmStatic
    fun println(priority: Int, tag: String, msg: String) {
        iLoggerDelegate.println(priority, tag, msg)
    }

    @JvmStatic
    fun flush() {
        iLoggerDelegate.flush()
    }

    @JvmStatic
    fun release() {
        iLoggerDelegate.release()
    }

    @JvmStatic
    fun getStackTraceString(tr: Throwable?): String {
        if (tr == null) {
            return ""
        }
        var t = tr
        while (t != null) {
            if (t is UnknownHostException) {
                return ""
            }
            t = t.cause
        }
        val sw = StringWriter()
        val pw = PrintWriter(sw)
        tr.printStackTrace(pw)
        pw.flush()
        return sw.toString()
    }

    @JvmStatic
    fun default(application: Application) {
        val wrapInterceptor = object : IInterceptor {
            override fun intercept(data: ZapData): Boolean {
                return true
            }
        }

        val logAppender = AndroidAppender.Builder()
                .setLevel(Level.VERBOSE)
                .addInterceptor(wrapInterceptor)
                .create()

        val zapFolder = getLogDir(application)
        val bufferPath = zapFolder.absolutePath + File.separator + ".cache"
        val time = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val logPath = zapFolder.absolutePath + File.separator + time + ".txt"
        val fileAppender = FileAppender.Builder(application)
                .setLogFilePath(logPath)
                .setLevel(Level.VERBOSE)
                .addInterceptor(wrapInterceptor)
                .setBufferFilePath(bufferPath)
                .setFormatter(DateFileFormatter())
                .setCompress(false)
                .setBufferSize(DEFAULT_BUFFER_SIZE)
                .create()
        initCrashHandler()
        iLoggerDelegate = AppenderLogger.Builder()
                .addAppender(logAppender)
                .addAppender(fileAppender)
                .create()

    }

    private fun initCrashHandler() {
        mUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { t, e ->
            e("flyfun_zap", e)
            if (mUncaughtExceptionHandler != null) {
                mUncaughtExceptionHandler!!.uncaughtException(t, e)
            } else {
                Process.killProcess(Process.myPid())
            }
        }
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