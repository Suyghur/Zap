package cn.flyfun.zap.Interceptor

import cn.flyfun.zap.ZapData

/**
 * @author #Suyghur,
 * Created on 2021/1/15
 */
interface IInterceptor {
    fun intercept(data: ZapData): Boolean
}