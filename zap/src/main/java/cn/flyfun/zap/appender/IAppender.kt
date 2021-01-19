package cn.flyfun.zap.appender

/**
 * @author #Suyghur,
 * Created on 2021/1/15
 */
interface IAppender {

    fun append(level: Int, tag: String, msg: String)
    fun flush()
    fun release()
}