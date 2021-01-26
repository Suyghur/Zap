package com.flyfun.zap

import android.app.Application
import cn.flyfun.zap.Zap

/**
 * @author #Suyghur,
 * Created on 2021/1/18
 */
class ZapApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Zap.default(this, debug = true)
    }
}