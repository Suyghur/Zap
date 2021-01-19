package cn.flyfun.zap.Interceptor

import cn.flyfun.zap.Level
import cn.flyfun.zap.ZapData

/**
 * @author #Suyghur,
 * Created on 2021/1/15
 */
class LevelInterceptor : IInterceptor {
    private var level = Level.VERBOSE

    fun setLevel(level: Int) {
        this.level = level
    }

    override fun intercept(data: ZapData): Boolean {
        return data.level >= level
    }

}