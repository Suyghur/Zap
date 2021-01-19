package cn.flyfun.zap.formatter

/**
 * @author #Suyghur,
 * Created on 2021/1/19
 */
interface IFormatter {

    fun format(level: Int, tag: String, msg: String): String
}