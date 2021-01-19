//
// Created by #Suyghur, on 2021/1/15.
//

#ifndef ZAP_ASYNC_FILE_FLUSH_H
#define ZAP_ASYNC_FILE_FLUSH_H

#include <sys/types.h>
#include <vector>
#include <thread>
#include <mutex>
#include <condition_variable>
#include <unistd.h>
#include "flush_buffer.h"

class AsyncFileFlush {
public:
    AsyncFileFlush();

    ~AsyncFileFlush();

    bool asyncFlush(FlushBuffer *buffer);

    void stopFlush();

private:
    void asyncLogThread();

    ssize_t flush(FlushBuffer *buffer);

    bool exit = false;

    std::vector<FlushBuffer *> async_buffer;
    std::thread async_thread;
    std::condition_variable async_condition;
    std::mutex async_mtx;

};

#endif //ZAP_ASYNC_FILE_FLUSH_H
