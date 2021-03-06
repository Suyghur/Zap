//
// Created by #Suyghur, on 2021/1/15.
//

#include "include/async_file_flush.h"

AsyncFileFlush::AsyncFileFlush() {
    async_thread = std::thread(&AsyncFileFlush::asyncLogThread, this);
}

AsyncFileFlush::~AsyncFileFlush() {
    stopFlush();
}

void AsyncFileFlush::asyncLogThread() {
    while (true) {
        std::unique_lock<std::mutex> lck_async_log_thread(async_mtx);
        while (!async_buffer.empty()) {
            FlushBuffer *data = async_buffer.back();
            async_buffer.pop_back();
            flush(data);
        }
        if (exit) {
            return;
        }
        async_condition.wait(lck_async_log_thread);
    }
}

ssize_t AsyncFileFlush::flush(FlushBuffer *buffer) {
    ssize_t written = 0;
    FILE *log_file = buffer->logFile();
    if (log_file != nullptr && buffer->length() > 0) {
        written = fwrite(buffer->ptr(), buffer->length(), 1, log_file);
        fflush(log_file);
    }
    delete buffer;
    return written;
}

bool AsyncFileFlush::async_flush(FlushBuffer *buffer) {
    std::unique_lock<std::mutex> lck_async_flush(async_mtx);
    if (exit) {
        delete buffer;
        return false;
    }
    async_buffer.push_back(buffer);
    async_condition.notify_all();
    return true;
}

void AsyncFileFlush::stopFlush() {
    exit = true;
    async_condition.notify_all();
    async_thread.join();
}
