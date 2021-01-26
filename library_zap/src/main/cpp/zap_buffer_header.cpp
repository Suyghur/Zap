//
// Created by #Suyghur, on 2021/1/18.
//

#include <ctime>
#include "zap_buffer_header.h"

using namespace zap_header;

ZapBufferHeader::ZapBufferHeader(void *data, size_t size) : data_ptr((char *) data),
                                                            data_size(size) {
}

ZapBufferHeader::~ZapBufferHeader() = default;

void *ZapBufferHeader::originPtr() {
    return data_ptr;
}

Header *ZapBufferHeader::getHeader() {
    Header *header = new Header();
    if (isAvailable()) {
        header->magic = kMagicHeader;
        size_t log_len = 0;
        memcpy(&log_len, data_ptr + sizeof(char), sizeof(size_t));
        header->log_len = log_len;
        size_t log_path_len = 0;
        memcpy(&log_path_len, data_ptr + sizeof(char) + sizeof(size_t), sizeof(size_t));
        header->log_path_len = log_path_len;
        char *log_path = new char[log_path_len + 1];
        memset(log_path, 0, log_path_len + 1);
        memcpy(log_path, data_ptr + sizeof(char) + sizeof(size_t) + sizeof(size_t), log_path_len);
        header->log_path = log_path;
        char isCompress = (data_ptr + sizeof(char) + sizeof(size_t) + sizeof(size_t) +
                           log_path_len)[0];
        header->isCompress = isCompress == 1;
    }
    return header;
}

size_t ZapBufferHeader::getHeaderLen() {
    if (isAvailable()) {
        return calculateHeaderLen(getLogPathLen());
    }
    return 0;
}

void *ZapBufferHeader::ptr() {
    return data_ptr + getHeaderLen();
}

void *ZapBufferHeader::write_ptr() {
    return data_ptr + getHeaderLen() + getLogLen();
}

void ZapBufferHeader::initHeader(Header &header) {
    if ((sizeof(char) + sizeof(size_t) + sizeof(size_t) + header.log_path_len) > data_size) {
        return;
    }
    memcpy(data_ptr, &header.magic, sizeof(char));
    memcpy(data_ptr + sizeof(char), &header.log_len, sizeof(size_t));
    memcpy(data_ptr + sizeof(char) + sizeof(size_t), &header.log_path_len, sizeof(size_t));
    memcpy(data_ptr + sizeof(char) + sizeof(size_t) + sizeof(size_t), header.log_path,
           header.log_path_len);
    char isCompress = 0;
    if (header.isCompress) {
        isCompress = 1;
    }
    memcpy(data_ptr + sizeof(char) + sizeof(size_t) + sizeof(size_t) + header.log_path_len,
           &isCompress,
           sizeof(char));

}

size_t ZapBufferHeader::getLogLen() {
    if (isAvailable()) {
        size_t log_len = 0;
        memcpy(&log_len, data_ptr + sizeof(char), sizeof(size_t));
        // log长度总是大于 0 并小于 buffer_size 减去 header 长度的
        if (log_len <= 0 || log_len > (data_size - getHeaderLen())) {
            log_len = 0;
        }
        return log_len;
    }
    return 0;
}

size_t ZapBufferHeader::getLogPathLen() {
    if (isAvailable()) {
        size_t log_path_len = 0;
        memcpy(&log_path_len, data_ptr + sizeof(char) + sizeof(size_t), sizeof(size_t));
        // logpath 的长度不能大于整个buffer 减去header 中其它数据的长度
        if (log_path_len <= 0 || log_path_len > data_size - calculateHeaderLen(0)) {
            log_path_len = 0;
        }
        return log_path_len;
    }
    return 0;
}

char *ZapBufferHeader::getLogPath() {
    if (isAvailable()) {
        size_t log_path_len = getLogPathLen();
        if (log_path_len > 0) {
            char *log_path = new char[log_path_len + 1];
            memset(log_path, 0, log_path_len + 1);
            memcpy(log_path, data_ptr + sizeof(char) + sizeof(size_t) + sizeof(size_t),
                   log_path_len);
            return log_path;
        }
    }
    return nullptr;
}

void ZapBufferHeader::setLogLen(size_t log_len) {
    if (isAvailable()) {
        memcpy(data_ptr + sizeof(char), &log_len, sizeof(size_t));
    }
}

bool ZapBufferHeader::isAvailable() {
    return data_ptr[0] == kMagicHeader;
}

bool ZapBufferHeader::getIsCompress() {
    if (isAvailable()) {
        char isCompress = (data_ptr + sizeof(char) + sizeof(size_t) + sizeof(size_t) +
                           getLogPathLen())[0];
        return isCompress == 1;
    }
    return false;
}

size_t ZapBufferHeader::calculateHeaderLen(size_t log_path_len) {
    return sizeof(char) + sizeof(size_t) + sizeof(size_t) + log_path_len + sizeof(char);
}






