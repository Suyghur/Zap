package cn.flyfun.zap.crash

import android.app.Application
import android.os.Process
import cn.flyfun.zap.Zap

/**
 * @author #Suyghur,
 * Created on 2021/1/19
 */
class ZapCrash private constructor() : Thread.UncaughtExceptionHandler {

    private var mUncaughtExceptionHandler: Thread.UncaughtExceptionHandler? = null


    fun initialize(application: Application) {
        mUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler(this)
    }

    override fun uncaughtException(thread: Thread, throwable: Throwable) {
        Zap.e("flyfun_zap", "has some crash", throwable)
        if (mUncaughtExceptionHandler != null) {
            mUncaughtExceptionHandler!!.uncaughtException(thread, throwable)
        } else {
            Process.killProcess(Process.myPid())
        }
    }

    companion object {
        @JvmStatic
        fun getInstance(): ZapCrash {
            return ZapCrashHolder.INSTANCE
        }

        private object ZapCrashHolder {
            val INSTANCE: ZapCrash = ZapCrash()
        }
    }
}