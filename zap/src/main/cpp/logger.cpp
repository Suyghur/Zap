//
// Created by #Suyghur, on 2021/1/20.
//

#include "include/logger.h"

void Logger::logd(const std::string &msg) {
    LOGD("JNI -> %s", msg.c_str());
}

void Logger::loge(const std::string &msg) {
    LOGE("JNI -> %s", msg.c_str());
}
