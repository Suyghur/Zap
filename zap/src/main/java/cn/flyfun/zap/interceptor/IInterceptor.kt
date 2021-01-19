package cn.flyfun.zap.interceptor

import cn.flyfun.zap.ZapData

/**
 * @author #Suyghur,
 * Created on 2021/1/19
 */
interface IInterceptor {

    fun intercept(data: ZapData): Boolean
}