package cn.flyfun.zap.logger

import cn.flyfun.zap.appender.IAppender

/**
 * @author #Suyghur,
 * Created on 2021/1/19
 */
class AppenderLogger private constructor() : ILogger {

    private val appenderList: MutableList<IAppender> = mutableListOf()

    fun getAppenderList(): MutableList<IAppender> {
        return appenderList
    }

    fun addAppender(appender: IAppender) {
        appenderList.add(appender)
    }

    override fun println(priority: Int, tag: String, msg: String) {
        for (appender in appenderList) {
            appender.append(priority, tag, msg)
        }
    }

    override fun flush() {
        for (appender in appenderList) {
            appender.flush()
        }
    }

    override fun release() {
        for (appender in appenderList) {
            appender.release()
        }
        appenderList.clear()
    }

    class Builder {
        private val logger: AppenderLogger = AppenderLogger()
        fun addAppender(appender: IAppender): Builder {
            logger.addAppender(appender)
            return this
        }

        fun create(): AppenderLogger {
            return logger
        }
    }
}