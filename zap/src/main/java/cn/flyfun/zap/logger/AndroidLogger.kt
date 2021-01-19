package cn.flyfun.zap.logger

import android.util.Log

/**
 * @author #Suyghur,
 * Created on 2021/1/15
 */
class AndroidLogger : ILogger {

    override fun println(priority: Int, tag: String, msg: String) {
        Log.println(priority, tag, msg)
    }

    override fun flush() {
    }

    override fun release() {
    }
}