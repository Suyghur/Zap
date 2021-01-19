package com.flyfun.zap

import android.app.Application

/**
 * @author #Suyghur,
 * Created on 2021/1/18
 */
class ZapApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        ZapInit.install(this)
    }
}