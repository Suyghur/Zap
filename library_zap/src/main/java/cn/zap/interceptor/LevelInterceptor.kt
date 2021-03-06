package cn.zap.interceptor

import cn.zap.Level
import cn.zap.ZapData

/**
 * @author #Suyghur,
 * Created on 2021/1/19
 */

class LevelInterceptor : IInterceptor {

    private var level = Level.DEBUG

    fun setLevel(level: Int) {
        this.level = level
    }

    override fun intercept(data: ZapData): Boolean {
        return data.level >= level
    }
}