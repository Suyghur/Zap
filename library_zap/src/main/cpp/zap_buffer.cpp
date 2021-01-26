//
// Created by #Suyghur, on 2021/1/18.
//

#include "include/zap_buffer.h"

ZapBuffer::ZapBuffer(char *ptr, size_t buffer_size) : buffer_ptr(ptr), buffer_size(buffer_size),
                                                      zapHeader(buffer_ptr, buffer_size) {
    if (zapHeader.isAvailable()) {
        data_ptr = (char *) zapHeader.ptr();
        write_ptr = (char *) zapHeader.write_ptr();
        if (zapHeader.getIsCompress()) {
            initCompress(true);
        }
        char *log_path = getLogPath();
        if (log_path != nullptr) {
            openSetLogFile(log_path);
            delete[] log_path;
        }
    }
    memset(&zStream, 0, sizeof(zStream));
}

ZapBuffer::~ZapBuffer() {
    release();
}

size_t ZapBuffer::length() {
    return write_ptr - data_ptr;
}

void ZapBuffer::setLength(size_t len) {
    zapHeader.setLogLen(len);
}

size_t ZapBuffer::append(const char *log, size_t len) {
    std::lock_guard<std::recursive_mutex> lck_append(log_mtx);

    if (length() == 0) {
        initCompress(is_compress);
    }

    size_t free_size = emptySize();
    size_t write_size = 0;
    if (is_compress) {
        zStream.avail_in = (uInt) len;
        zStream.next_in = (Bytef *) log;

        zStream.avail_out = (uInt) free_size;
        zStream.next_out = (Bytef *) write_ptr;

        if (Z_OK != deflate(&zStream, Z_SYNC_FLUSH)) {
            return 0;
        }

        write_size = free_size - zStream.avail_out;
    } else {
        write_size = len <= free_size ? len : free_size;
        memcpy(write_ptr, log, write_size);
    }
    write_ptr += write_size;
    setLength(length());
    return write_size;
}

void ZapBuffer::setAsyncFileFlush(AsyncFileFlush *file_flush) {
    file_flush_ptr = file_flush;
}

void ZapBuffer::asyncFlush() {
    asyncFlush(file_flush_ptr);
}

void ZapBuffer::asyncFlush(AsyncFileFlush *file_flush) {
    asyncFlush(file_flush, nullptr);
}

void ZapBuffer::asyncFlush(AsyncFileFlush *file_flush, ZapBuffer *buffer) {
    if (file_flush == nullptr) {
        if (buffer != nullptr) {
            delete buffer;
        }
        return;
    }
    std::lock_guard<std::recursive_mutex> lck_clear(log_mtx);
    if (length() > 0) {
        if (is_compress && Z_NULL != zStream.state) {
            deflateEnd(&zStream);
        }
        auto *flush_buffer = new FlushBuffer(log_file_ptr);
        flush_buffer->write(data_ptr, length());
        flush_buffer->releaseThis(buffer);
        clear();
        file_flush->async_flush(flush_buffer);
    } else {
        delete buffer;
    }
}

void ZapBuffer::clear() {
    std::lock_guard<std::recursive_mutex> lck_clear(log_mtx);
    write_ptr = data_ptr;
    memset(write_ptr, '\0', emptySize());
    setLength(length());
}

void ZapBuffer::release() {
    std::lock_guard<std::recursive_mutex> lck_release(log_mtx);
    if (is_compress && Z_NULL != zStream.state) {
        deflateEnd(&zStream);
    }
    if (map_buffer) {
        munmap(buffer_ptr, buffer_size);
    } else {
        delete[] buffer_ptr;
    }
    if (log_file_ptr != nullptr) {
        fclose(log_file_ptr);
    }
}

size_t ZapBuffer::emptySize() {
    return buffer_size - (write_ptr - buffer_ptr);
}

void ZapBuffer::initData(char *log_path, size_t log_path_len, bool compress) {
    std::lock_guard<std::recursive_mutex> lck_release(log_mtx);
    memset(buffer_ptr, '\0', buffer_size);

    zap_header::Header header{};
    header.magic = kMagicHeader;
    header.log_path_len = log_path_len;
    header.log_path = log_path;
    header.log_len = 0;
    header.isCompress = compress;

    zapHeader.initHeader(header);
    initCompress(compress);

    data_ptr = (char *) zapHeader.ptr();
    write_ptr = (char *) zapHeader.write_ptr();

    openSetLogFile(log_path);
}

char *ZapBuffer::getLogPath() {
    return zapHeader.getLogPath();
}

bool ZapBuffer::initCompress(bool compress) {
    is_compress = compress;
    if (is_compress) {
        zStream.zalloc = Z_NULL;
        zStream.zfree = Z_NULL;
        zStream.opaque = Z_NULL;
        return Z_OK ==
               deflateInit2(&zStream, Z_BEST_COMPRESSION, Z_DEFLATED, -MAX_WBITS, MAX_MEM_LEVEL,
                            Z_DEFAULT_STRATEGY);
    }
    return false;
}

bool ZapBuffer::openSetLogFile(const char *log_path) {
    if (log_path != nullptr) {
        FILE *file_log = fopen(log_path, "ab+");
        if (file_log != nullptr) {
            log_file_ptr = file_log;
            return true;
        }
    }
    return false;
}

void ZapBuffer::changeLogPath(char *log_path) {
    if (log_file_ptr != nullptr) {
        asyncFlush();
    }
    initData(log_path, strlen(log_path), is_compress);
}






