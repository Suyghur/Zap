package com.zap.demo

import android.app.Application
import android.content.Context
import cn.zap.Zap

/**
 * @author #Suyghur,
 * Created on 2021/1/18
 */
class ZapApplication : Application() {

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        Zap.default(this, debug = true)
    }

    override fun onCreate() {
        super.onCreate()
    }
}