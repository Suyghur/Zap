package cn.flyfun.zap.logger

/**
 * @author #Suyghur,
 * Created on 2021/1/15
 */
interface ILogger {

    fun println(priority: Int, tag: String, msg: String)

    fun flush()

    fun release()
}