package cn.flyfun.zap.formatter

import android.text.TextUtils
import cn.flyfun.zap.Level
import java.text.SimpleDateFormat
import java.util.*

/**
 * @author #Suyghur,
 * Created on 2021/1/19
 */
class DateFileFormatter @JvmOverloads constructor(pattern: String? = "yyy:MM:dd HH:mm:ss") : IFormatter {

    private var simpleDateFormat: SimpleDateFormat = SimpleDateFormat(pattern, Locale.getDefault())
    private var mDate: Date
    private var lastDateFormatted = ""
    private var mStringBuffer: StringBuffer = StringBuffer()
    private var mTimeLength = 0

    init {
        Calendar.getInstance()[Calendar.SECOND] = 0
        mDate = Calendar.getInstance().time
    }

    private fun resetTimePrefix() {
        if (mStringBuffer.isNotEmpty()) {
            mStringBuffer.delete(0, mStringBuffer.length)
        }
        mTimeLength = mStringBuffer.append(lastDateFormatted).append(" ").length
    }

    private fun formatString(level: Int, tag: String, msg: String): String {
        if (mStringBuffer.length > mTimeLength) {
            mStringBuffer.delete(mTimeLength, mStringBuffer.length)
        }
        return mStringBuffer.append("[${Level.getShortLevelName(level)}]")
                .append(tag).append(": ").append(msg).append("\n").toString()
    }

    override fun format(level: Int, tag: String, msg: String): String {
        if ((System.currentTimeMillis() - mDate.time) > 1000 || TextUtils.isEmpty(lastDateFormatted)) {
            mDate.time = System.currentTimeMillis()
            lastDateFormatted = simpleDateFormat.format(mDate)
            resetTimePrefix()
            return formatString(level, tag, msg)
        }
        return formatString(level, tag, msg)
    }
}