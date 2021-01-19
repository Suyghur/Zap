package cn.flyfun.zap

import android.app.Application
import android.content.Context
import android.util.Log
import cn.flyfun.zap.Interceptor.IInterceptor
import cn.flyfun.zap.appender.AndroidAppender
import cn.flyfun.zap.appender.FileAppender
import cn.flyfun.zap.crash.ZapCrash
import cn.flyfun.zap.formatter.DateFileFormatter
import cn.flyfun.zap.logger.ILogger
import cn.flyfun.zap.logger.AndroidLogger
import cn.flyfun.zap.logger.AppenderLogger
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.net.URLDecoder
import java.net.UnknownHostException
import java.text.SimpleDateFormat
import java.util.*


/**
 * @author #Suyghur,
 * Created on 2021/1/15
 */
object Zap {

    var iLoggerDelegate: ILogger = AndroidLogger()

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
        Log.e("flyfun_zap", getStackTraceString(throwable))
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
//        return URLDecoder.decode(sw.toString(), "UTF-8")
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
        val bufferPath = zapFolder.absolutePath + File.separator + ".zapCache"
        val time = SimpleDateFormat("yyyy_MM_dd", Locale.getDefault()).format(Date())
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

        iLoggerDelegate = AppenderLogger.Builder()
                .addAppender(logAppender)
                .addAppender(fileAppender)
                .create()

        ZapCrash.getInstance().initialize(application)
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