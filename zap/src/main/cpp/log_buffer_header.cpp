//
// Created by #Suyghur, on 2021/1/18.
//

#include "include/log_buffer_header.h"


using namespace log_header;

LogBufferHeader::LogBufferHeader(void *data, size_t size) : data_ptr((char *) data),
                                                            data_size(size) {

}

LogBufferHeader::~LogBufferHeader() = default;

void *LogBufferHeader::originPtr() {
    return data_ptr;
}

Header *LogBufferHeader::getHeader() {
    auto *_header = new Header();
    if (isAvailable()) {
        _header->magic = kMagicHeader;
        size_t _log_len = 0;
        memcpy(&_log_len, data_ptr + sizeof(char), sizeof(size_t));
        _header->log_len = _log_len;
        size_t _log_path_len = 0;
        memcpy(&_log_path_len, data_ptr + sizeof(char) + sizeof(size_t), sizeof(size_t));
        _header->log_path_len = _log_path_len;
        char *_log_path = new char[_log_path_len + 1];
        memset(_log_path, 0, _log_path_len + 1);
        memcpy(_log_path, data_ptr + sizeof(char) + sizeof(size_t) + sizeof(size_t), _log_path_len);
        _header->log_path = _log_path;
        char _compress = (data_ptr + sizeof(char) + sizeof(size_t) + sizeof(size_t) +
                          _log_path_len)[0];
        _header->compress = _compress;
    }
    return _header;
}

size_t LogBufferHeader::getHeaderLen() {
    if (isAvailable()) {
        return calculateHeaderLen(getLogPathLen());
    }
    return 0;
}

void *LogBufferHeader::ptr() {
    return data_ptr + getHeaderLen();
}

void *LogBufferHeader::writePtr() {
    return data_ptr + getHeaderLen() + getLogLen();
}

void LogBufferHeader::initHeader(Header &header) {
    if ((sizeof(char) + sizeof(size_t) + sizeof(size_t) + header.log_path_len) > data_size) {
        return;
    }
    memcpy(data_ptr, &header.magic, sizeof(char));
    memcpy(data_ptr + sizeof(char), &header.log_len, sizeof(size_t));
    memcpy(data_ptr + sizeof(char) + sizeof(size_t), &header.log_path_len, sizeof(size_t));
    memcpy(data_ptr + sizeof(char) + sizeof(size_t) + sizeof(size_t), header.log_path,
           header.log_path_len);
    char _compress = 0;
    if (header.compress) {
        _compress = 1;
    }
    memcpy(data_ptr + sizeof(char) + sizeof(size_t) + sizeof(size_t) + header.log_path_len,
           &_compress, sizeof(char));
}

size_t LogBufferHeader::getLogLen() {
    if (isAvailable()) {
        size_t _log_len = 0;
        memcpy(&_log_len, data_ptr + sizeof(char), sizeof(size_t));
        //log长度总是大于0并小于buffer_size减去header长度
        if (_log_len <= 0 || _log_len > (data_size - getHeaderLen())) {
            _log_len = 0;
        }
        return _log_len;
    }
    return 0;
}

size_t LogBufferHeader::getLogPathLen() {
    if (isAvailable()) {
        size_t _log_path_len = 0;
        memcpy(&_log_path_len, data_ptr + sizeof(char) + sizeof(size_t), sizeof(size_t));
        //log_path的长度不能大于整个buffer减去header中其他数据的长度
        if (_log_path_len <= 0 || _log_path_len > data_size - calculateHeaderLen(0)) {
            _log_path_len = 0;
        }
        return _log_path_len;
    }
    return 0;
}

char *LogBufferHeader::getLogPath() {
    if (isAvailable()) {
        size_t _log_path_len = getLogPathLen();
        if (_log_path_len > 0) {
            char *_log_path = new char[_log_path_len + 1];
            memset(_log_path, 0, _log_path_len + 1);
            memcpy(_log_path, data_ptr + sizeof(char) + sizeof(size_t) + sizeof(size_t),
                   _log_path_len);
            return _log_path;
        }
    }
    return nullptr;
}

void LogBufferHeader::setLogLen(size_t len) {
    if (isAvailable()) {
        memcpy(data_ptr + sizeof(char), &len, sizeof(size_t));
    }
}

bool LogBufferHeader::isAvailable() {
    return data_ptr[0] == kMagicHeader;
}

bool LogBufferHeader::isCompress() {
    if (isAvailable()) {
        char _compress = (data_ptr + sizeof(char) + sizeof(size_t) + sizeof(size_t) +
                          getLogPathLen())[0];
        return _compress == 1;
    }
    return false;
}

size_t LogBufferHeader::calculateHeaderLen(size_t len) {
    return sizeof(char) + sizeof(size_t) + sizeof(size_t) + len + sizeof(char);
}