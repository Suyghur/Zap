//
// Created by #Suyghur, on 2021/1/18.
//

#ifndef ZAP_LOG_BUFFER_H
#define ZAP_LOG_BUFFER_H

#include <string>
#include <math.h>
#include <unistd.h>
#include <sys/mman.h>
#include <thread>
#include <vector>
#include <mutex>
#include <condition_variable>
#include "log_buffer_header.h"
#include "async_file_flush.h"
#include <zlib.h>

using namespace log_header;

class LogBuffer {
public:

    bool map_buffer = true;

    LogBuffer(char *ptr, size_t capacity);

    ~LogBuffer();

    void initData(char *log_path, size_t log_path_len, bool compress);

    size_t length();

    size_t append(const char *log, size_t len);

    void release();

    size_t emptySize();

    char *getLogPath();

    void setAsyncFileFlush(AsyncFileFlush *flush);

    void asyncFlush();

    void asyncFlush(AsyncFileFlush *flush);

    void asyncFlush(AsyncFileFlush *flush, LogBuffer *release_this);

    void changeLogPath(char *log_path);

private:
    void clear();

    void setLength(size_t len);

    bool initCompress(bool compress);

    bool openSetLogFile(const char *log_path);

    FILE *log_file = nullptr;
    AsyncFileFlush *file_flush = nullptr;
    char *const buffer_ptr = nullptr;
    char *data_ptr = nullptr;
    char *write_ptr = nullptr;

    size_t buffer_size = 0;
    std::recursive_mutex log_mtx;
    LogBufferHeader log_header;
    z_stream zStream{};
    bool is_compress = false;
};

#endif //ZAP_LOG_BUFFER_H
