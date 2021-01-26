package cn.flyfun.zap

/**
 * @author #Suyghur,
 * Created on 2021/1/19
 */
object Level {

    const val DEBUG = 3
    const val INFO = 4
    const val ERROR = 6

    @JvmStatic
    fun getLevelName(level: Int): String {
        return when (level) {
            DEBUG -> "DEBUG"
            INFO -> "INFO"
            ERROR -> "ERROR"
            else ->
                if (level < DEBUG) {
                    "DEBUG-${DEBUG - level}"
                } else {
                    "ERROR-${level - ERROR}"
                }
        }
    }

    @JvmStatic
    fun getShortLevelName(level: Int): String {
        return when (level) {
            DEBUG -> "D"
            INFO -> "I"
            ERROR -> "E"
            else ->
                if (level < DEBUG) {
                    "V-${DEBUG - level}"
                } else {
                    "E-${level - ERROR}"
                }
        }
    }
}