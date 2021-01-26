package cn.flyfun.zap.interceptor

import cn.flyfun.zap.Level
import cn.flyfun.zap.ZapData

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