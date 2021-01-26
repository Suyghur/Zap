package cn.flyfun.zap

import android.app.Application
import android.os.Process
import cn.flyfun.zap.appender.FileAppender
import cn.flyfun.zap.appender.ZapAppender
import cn.flyfun.zap.formatter.DateFileFormatter
import cn.flyfun.zap.interceptor.IInterceptor
import cn.flyfun.zap.logger.AppenderLogger
import cn.flyfun.zap.logger.ILogger
import cn.flyfun.zap.logger.ZapLogger
import cn.flyfun.zap.toolkit.FileUtils
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.lang.reflect.Array
import java.net.UnknownHostException
import java.text.SimpleDateFormat
import java.util.*

/**
 * @author #Suyghur,
 * Created on 2021/1/19
 */
object Zap {

    private var iLoggerDelegate: ILogger = ZapLogger()
    private var mUncaughtExceptionHandler: Thread.UncaughtExceptionHandler? = null

    private const val DEFAULT_BUFFER_SIZE = 1024 * 400
    private const val DEFAULT_PAST_TIME = -3

    private const val TAG = "flyfun_zap"


    @JvmStatic
    fun d(any: Any) {
        d(TAG, any)
    }

    @JvmStatic
    fun d(tag: String, any: Any) {
        println(Level.DEBUG, tag, any)
    }

    @JvmStatic
    fun i(any: Any) {
        i(TAG, any)
    }

    @JvmStatic
    fun i(tag: String, any: Any) {
        println(Level.INFO, tag, any)
    }

    @JvmStatic
    fun e(any: Any) {
        e(TAG, any)
    }

    @JvmStatic
    fun e(throwable: Throwable) {
        e(TAG, throwable)
    }

    @JvmStatic
    fun e(tag: String, any: Any) {
        println(Level.ERROR, tag, any)
    }

    @JvmStatic
    fun e(tag: String, throwable: Throwable) {
        println(Level.ERROR, tag, getStackTraceString(throwable))
    }

    @JvmStatic
    fun e(tag: String, msg: String, throwable: Throwable) {
        println(Level.ERROR, tag, msg + "\n${getStackTraceString(throwable)}")
    }

    @JvmStatic
    fun println(priority: Int, tag: String, msg: String) {
        iLoggerDelegate.println(priority, tag, msg)
    }

    @JvmStatic
    fun println(priority: Int, tag: String, any: Any?) {
        iLoggerDelegate.println(priority, tag, obj2str(any))
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

    private fun obj2str(any: Any?): String {
        any?.let {
            val clz: Class<out Any?> = it.javaClass
            if (clz.isArray) {
                val sb = StringBuilder(clz.simpleName)
                sb.append("[ ")
                val len = Array.getLength(any)
                for (i in 0 until len) {
                    if (i != 0) {
                        sb.append(", ")
                    }
                    val tmp = Array.get(any, i)
                    sb.append(tmp)
                }
                sb.append(" ]")
                return sb.toString()
            } else {
                return "" + it
            }
        }
        return "null"
    }


    @JvmStatic
    @JvmOverloads
    fun default(application: Application, past: Int = DEFAULT_PAST_TIME, debug: Boolean = false) {
        val wrapInterceptor = object : IInterceptor {
            override fun intercept(data: ZapData): Boolean {
                return true
            }
        }
        var level = Level.INFO
        if (debug) {
            level = Level.DEBUG
        }
        val logAppender = ZapAppender.Builder()
                .setLevel(level)
                .addInterceptor(wrapInterceptor)
                .create()

        val zapFolder = FileUtils.getLogDir(application)
        val bufferPath = zapFolder.absolutePath + File.separator + ".cache"
        val time = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val logPath = zapFolder.absolutePath + File.separator + time + ".txt"
        val fileAppender = FileAppender.Builder(application)
                .setLogFilePath(logPath)
                .setLevel(Level.DEBUG)
                .addInterceptor(wrapInterceptor)
                .setBufferFilePath(bufferPath)
                .setFormatter(DateFileFormatter())
                .setCompress(false)
                .setBufferSize(DEFAULT_BUFFER_SIZE)
                .create()
        initCrashHandler()
        FileUtils.deletePastLog(zapFolder.absolutePath, past)
        iLoggerDelegate = AppenderLogger.Builder()
                .addAppender(logAppender)
                .addAppender(fileAppender)
                .create()

//        callback.onResult(0, "zap framework init success")
    }


    private fun initCrashHandler() {
        mUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { t, e ->
            e(e)
            if (mUncaughtExceptionHandler != null) {
                mUncaughtExceptionHandler!!.uncaughtException(t, e)
            } else {
                Process.killProcess(Process.myPid())
            }
        }
    }

}