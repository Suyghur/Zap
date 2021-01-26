//
// Created by #Suyghur, on 2021/1/18.
//

#ifndef ZAP_ZAP_BUFFER_H
#define ZAP_ZAP_BUFFER_H

#include <string>
#include <math.h>
#include <unistd.h>
#include <sys/mman.h>
#include <thread>
#include <vector>
#include <mutex>
#include <condition_variable>
#include "async_file_flush.h"
#include "flush_buffer.h"
#include "zap_buffer_header.h"
#include <zlib.h>

using namespace zap_header;

class ZapBuffer {
public:
    ZapBuffer(char *ptr, size_t buffer_size);

    ~ZapBuffer();

    void initData(char *log_path, size_t log_path_len, bool compress);

    size_t length();

    size_t append(const char *log, size_t len);

    void release();

    size_t emptySize();

    char *getLogPath();

    void setAsyncFileFlush(AsyncFileFlush *file_flush);

    void asyncFlush();

    void asyncFlush(AsyncFileFlush *file_flush);

    void asyncFlush(AsyncFileFlush *file_flush, ZapBuffer *buffer);

    void changeLogPath(char *log_path);

public:
    bool map_buffer = true;

private:
    void clear();

    void setLength(size_t len);

    bool initCompress(bool compress);

    bool openSetLogFile(const char *log_path);

    FILE *log_file_ptr = nullptr;
    AsyncFileFlush *file_flush_ptr = nullptr;
    char *const buffer_ptr = nullptr;
    char *data_ptr = nullptr;
    char *write_ptr = nullptr;

    size_t buffer_size = 0;
    std::recursive_mutex log_mtx;

    ZapBufferHeader zapHeader;
    z_stream zStream;
    bool is_compress = false;

};


#endif //ZAP_ZAP_BUFFER_H
