package cn.flyfun.zap

import android.util.Log

/**
 * @author #Suyghur,
 * Created on 2021/1/19
 */
class ZapBuffer(var bufferPath: String,
                var capacity: Int,
                var logPath: String,
                var compress: Boolean) {

    private var ptr = 0L

    init {
        System.loadLibrary("zap")
        try {
            ptr = initNative(bufferPath, capacity, logPath, compress)
        } catch (e: Exception) {
            Log.e(TAG, Zap.getStackTraceString(e))
        }
    }

    fun changeLogPath(logPath: String) {
        if (ptr != 0L) {
            try {
                changeLogPathNative(ptr, logPath)
            } catch (e: Exception) {
                Log.e(TAG, Zap.getStackTraceString(e))
            }
        }
    }

    fun write(log: String) {
        if (ptr != 0L) {
            try {
                writeNative(ptr, log)
            } catch (e: Exception) {
                Log.e(TAG, Zap.getStackTraceString(e))
            }
        }
    }

    fun flushAsync() {
        if (ptr != 0L) {
            try {
                flushAsyncNative(ptr)
            } catch (e: Exception) {
                Log.e(TAG, Zap.getStackTraceString(e))
            }
        }
    }

    fun release() {
        if (ptr != 0L) {
            try {
                releaseNative(ptr)
            } catch (e: Exception) {
                Log.e(TAG, Zap.getStackTraceString(e))
            }
        }
    }

    private external fun writeNative(ptr: Long, log: String)

    private external fun flushAsyncNative(ptr: Long)

    private external fun releaseNative(ptr: Long)

    private external fun changeLogPathNative(ptr: Long, logPath: String)

    companion object {
        const val TAG = "flyfun_zap"

        @JvmStatic
        private external fun initNative(bufferPath: String, capacity: Int, logPath: String, compress: Boolean): Long
    }
}