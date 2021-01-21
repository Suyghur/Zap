package cn.flyfun.zap

import android.util.Log
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

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

        @JvmStatic
        external fun showAllFiles(path: String)

//        @JvmStatic
//        fun showFiles(path: String) {
//            val calendar = Calendar.getInstance()
//            calendar.add(Calendar.DATE, -30)
//            val time = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
//            Zap.d(TAG, time)
////            Zap.d(TAG,calendar.time.toString())
//            val fileTree = File(path).walk()
//            fileTree.maxDepth(1)
//                    .filter { it.isFile }
//                    .filter { it.extension == "txt" }
//                    .forEach {
//                        if (it.name == "$time.txt") {
//                            Zap.d(TAG, it.name)
//                        }
//                    }
//
//
//        }
    }
}