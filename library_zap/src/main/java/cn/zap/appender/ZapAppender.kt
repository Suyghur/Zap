package cn.zap.appender

import android.util.Log
import cn.zap.Level
import cn.zap.interceptor.IInterceptor

/**
 * @author #Suyghur,
 * Created on 2021/1/19
 */
class ZapAppender(builder: Builder) : AbsAppender() {

    init {
        setLevel(builder.level)
        addInterceptor(builder.interceptors)
    }

    override fun doAppend(level: Int, tag: String, msg: String) {
        Log.println(level, tag, msg)
    }

    class Builder {
        internal var level = Level.DEBUG
        internal var interceptors: MutableList<IInterceptor> = mutableListOf()

        fun setLevel(level: Int): Builder {
            this.level = level
            return this
        }

        fun addInterceptor(interceptor: IInterceptor): Builder {
            interceptors.add(interceptor)
            return this
        }

        fun create(): ZapAppender {
            return ZapAppender(this)
        }

    }
}