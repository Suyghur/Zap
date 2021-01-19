//
// Created by #Suyghur, on 2021/1/18.
//

#include "include/log_buffer.h"


LogBuffer::LogBuffer(char *ptr, size_t capacity) : buffer_ptr(ptr), buffer_size(capacity),
                                                   log_header(buffer_ptr, capacity) {
    if (log_header.isAvailable()) {
        data_ptr = (char *) log_header.ptr();
        write_ptr = (char *) log_header.writePtr();
        if (log_header.isCompress()) {
            initCompress(true);
        }
        char *_log_path = getLogPath();
        if (_log_path != nullptr) {
            openSetLogFile(_log_path);
            delete[]_log_path;
        }
    }
    memset(&zStream, 0, sizeof(zStream));
}

LogBuffer::~LogBuffer() {
    release();
}

size_t LogBuffer::length() {
    return write_ptr - data_ptr;
}

void LogBuffer::setLength(size_t len) {
    log_header.setLogLen(len);
}

size_t LogBuffer::append(const char *log, size_t len) {
    std::lock_guard<std::recursive_mutex> _lck_append(log_mtx);

    if (length() == 0) {
        initCompress(is_compress);
    }

    size_t _free_size = emptySize();
    size_t _write_size;
    if (is_compress) {
        zStream.avail_in = (uInt) len;
        zStream.next_in = (Bytef *) log;

        zStream.avail_out = (uInt) _free_size;
        zStream.next_out = (Bytef *) write_ptr;

        if (Z_OK != deflate(&zStream, Z_SYNC_FLUSH)) {
            return 0;
        }

        _write_size = _free_size - zStream.avail_out;
    } else {
        _write_size = len <= _free_size ? len : _free_size;
        memcpy(write_ptr, log, _write_size);
    }
    write_ptr += _write_size;
    setLength(length());
    return _write_size;
}

void LogBuffer::setAsyncFileFlush(AsyncFileFlush *flush) {
    this->file_flush = flush;
}

void LogBuffer::asyncFlush() {
    asyncFlush(file_flush);
}

void LogBuffer::asyncFlush(AsyncFileFlush *flush) {
    asyncFlush(flush, nullptr);
}

void LogBuffer::asyncFlush(AsyncFileFlush *flush, LogBuffer *release_this) {
    if (flush == nullptr) {
        delete release_this;
        return;
    }
    std::lock_guard<std::recursive_mutex> _lck_clear(log_mtx);
    if (length() > 0) {
        if (is_compress && Z_NULL != zStream.state) {
            deflateEnd(&zStream);
        }
        auto *_flush_buffer = new FlushBuffer(log_file);
        _flush_buffer->write(data_ptr, length());
        _flush_buffer->releaseThis(release_this);
        clear();
        flush->asyncFlush(_flush_buffer);
    } else {
        delete release_this;
    }
}

void LogBuffer::clear() {
    std::lock_guard<std::recursive_mutex> _lck_clear(log_mtx);
    write_ptr = data_ptr;
    memset(write_ptr, '\0', emptySize());
    setLength(length());
}

void LogBuffer::release() {
    std::lock_guard<std::recursive_mutex> _lck_release(log_mtx);
    if (is_compress && Z_NULL != zStream.state) {
        deflateEnd(&zStream);
    }
    if (map_buffer) {
        munmap(buffer_ptr, buffer_size);
    } else {
        delete[]buffer_ptr;
    }
    if (log_file != nullptr) {
        fclose(log_file);
    }
}

size_t LogBuffer::emptySize() {
    return buffer_size - (write_ptr - buffer_ptr);
}

void LogBuffer::initData(char *log_path, size_t log_path_len, bool compress) {
    std::lock_guard<std::recursive_mutex> _lck_release(log_mtx);
    memset(buffer_ptr, '\0', buffer_size);

    log_header::Header _header{};
    _header.magic = kMagicHeader;
    _header.log_path_len = log_path_len;
    _header.log_path = log_path;
    _header.log_len = 0;
    _header.compress = compress;

    log_header.initHeader(_header);
    initCompress(compress);

    data_ptr = (char *) log_header.ptr();
    write_ptr = (char *) log_header.writePtr();

    openSetLogFile(log_path);
}

char *LogBuffer::getLogPath() {
    return log_header.getLogPath();
}

bool LogBuffer::initCompress(bool compress) {
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

bool LogBuffer::openSetLogFile(const char *log_path) {
    if (log_path != nullptr) {
        FILE *_file_log = fopen(log_path, "ab+");
        if (_file_log != nullptr) {
            log_file = _file_log;
            return true;
        }
    }
    return false;
}

void LogBuffer::changeLogPath(char *log_path) {
    if (log_path != nullptr) {
        asyncFlush();
    }
    initData(log_path, strlen(log_path), is_compress);
}


