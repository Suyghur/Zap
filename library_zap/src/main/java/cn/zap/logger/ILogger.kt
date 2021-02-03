package cn.zap.logger

/**
 * @author #Suyghur,
 * Created on 2021/1/19
 */
interface ILogger {

    fun println(priority: Int, tag: String, msg: String)

    fun flush()

    fun release()
}