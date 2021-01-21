//
// Created by #Suyghur, on 2021/1/20.
//

#ifndef ZAP_LOGGER_H
#define ZAP_LOGGER_H

#include <string>
#include <android/log.h>
#include <jni.h>

#define TAG "flyfun_zap"
#define LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG,TAG,__VA_ARGS__) // 定义LOGD类型
#define LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,TAG,__VA_ARGS__) // 定义LOGD类型

class Logger {
public:
    static void logd(const std::string &msg);

    static void loge(const std::string &msg);
};

#endif //ZAP_LOGGER_H
