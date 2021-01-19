//
// Created by #Suyghur, on 2021/1/19.
//

#include "include/logger.h"

void Logger::logd(const std::string &msg) {
    LOGD("JNI -> %s", msg.c_str());
}
