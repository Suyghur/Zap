package cn.flyfun.zap.appender

import android.util.Log
import cn.flyfun.zap.Interceptor.IInterceptor
import cn.flyfun.zap.Level

/**
 * @author #Suyghur,
 * Created on 2021/1/15
 */
class AndroidAppender(builder: Builder) : AbsAppender() {

    init {
        setLevel(builder.level)
        addInterceptor(builder.interceptors)
    }

    override fun doAppend(level: Int, tag: String, msg: String) {
        Log.println(level, tag, msg)
    }

    class Builder {
        internal var level = Level.VERBOSE
        internal var interceptors: MutableList<IInterceptor> = mutableListOf()

        fun setLevel(level: Int): Builder {
            this.level = level
            return this
        }

        fun addInterceptor(interceptor: IInterceptor): Builder {
            interceptors.add(interceptor)
            return this
        }

        fun create(): AndroidAppender {
            return AndroidAppender(this)
        }

    }
}