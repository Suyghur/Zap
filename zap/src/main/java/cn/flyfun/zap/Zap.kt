package cn.flyfun.zap

import android.util.Log
import cn.flyfun.zap.logger.ILogger
import cn.flyfun.zap.logger.AndroidLogger
import java.io.PrintWriter
import java.io.StringWriter
import java.net.UnknownHostException


/**
 * @author #Suyghur,
 * Created on 2021/1/15
 */
object Zap {

    var iLoggerDelegate: ILogger = AndroidLogger()

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

}