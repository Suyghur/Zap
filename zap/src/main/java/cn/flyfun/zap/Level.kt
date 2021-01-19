package cn.flyfun.zap

/**
 * @author #Suyghur,
 * Created on 2021/1/15
 */
object Level {

    const val VERBOSE = 2
    const val DEBUG = 3
    const val INFO = 4
    const val WARN = 5
    const val ERROR = 6

    fun getLevelName(level: Int): String {
        return when (level) {
            VERBOSE -> "VERBOSE"
            DEBUG -> "DEBUG"
            INFO -> "INFO"
            WARN -> "WARN"
            ERROR -> "ERROR"
            else -> {
                if (level < VERBOSE) {
                    "VERBOSE-${VERBOSE - level}"
                } else {
                    "ERROR+${level - ERROR}"
                }
            }
        }
    }

    @JvmStatic
    fun getShortLevelName(level: Int): String {
        return when (level) {
            VERBOSE -> "V"
            DEBUG -> "D"
            INFO -> "I"
            WARN -> "W"
            ERROR -> "E"
            else -> {
                if (level < VERBOSE) {
                    "V-${VERBOSE - level}"
                } else {
                    "E+${level - ERROR}"
                }
            }
        }
    }
}