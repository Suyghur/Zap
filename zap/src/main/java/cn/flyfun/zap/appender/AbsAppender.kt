package cn.flyfun.zap.appender

import cn.flyfun.zap.Interceptor.IInterceptor
import cn.flyfun.zap.Interceptor.LevelInterceptor
import cn.flyfun.zap.ZapData

/**
 * @author #Suyghur,
 * Created on 2021/1/15
 */
abstract class AbsAppender : IAppender {

    var maxSingleLength = MAX_LENGTH_OF_SINGLE_MESSAGE
    private val interceptors: MutableList<IInterceptor> = mutableListOf()
    private var levelInterceptor = LevelInterceptor()

    init {
        addInterceptor(levelInterceptor)
    }

    fun setLevel(level: Int) {
        levelInterceptor.setLevel(level)
    }

    fun addInterceptor(interceptors: MutableList<IInterceptor>) {
        if (interceptors.isNotEmpty()) {
            this.interceptors.addAll(interceptors)
        }
    }

    fun addInterceptor(interceptor: IInterceptor) {
        interceptors.add(interceptor)
    }

    private fun appendInner(level: Int, tag: String, msg: String) {
        if (msg.length <= maxSingleLength) {
            doAppend(level, tag, msg)
            return
        }
        val msgLength = msg.length
        var start = 0
        var end = start + maxSingleLength
        while (start < msgLength) {
            doAppend(level, tag, msg.substring(start, end))
            start = end
            end = (start + maxSingleLength).coerceAtMost(msgLength)
        }
    }

    protected abstract fun doAppend(level: Int, tag: String, msg: String)

    override fun append(level: Int, tag: String, msg: String) {
        val data = ZapData.obtain(level, tag, msg)
        var intercepted = false
        for (interceptor in interceptors) {
            if (!interceptor.intercept(data)) {
                intercepted = true
            }
        }
        if (!intercepted) {
            appendInner(data.level, data.tag, data.msg)
        }
        data.recycle()
    }

    override fun flush() {
    }

    override fun release() {
    }

    companion object {
        const val MAX_LENGTH_OF_SINGLE_MESSAGE = 4063
    }
}