//
// Created by #Suyghur, on 2021/1/15.
//

#ifndef ZAP_FLUSH_BUFFER_H
#define ZAP_FLUSH_BUFFER_H

#include <sys/types.h>
#include <string.h>
#include <math.h>
#include <stdio.h>

class FlushBuffer {

public:
    FlushBuffer(FILE *log_file, size_t size = 128);

    ~FlushBuffer();

    void write(void *data, size_t len);

    void reset();

    size_t length();

    void *ptr();

    FILE *logFile();

    void releaseThis(void *buffer);

private:
    FILE *log_file_ptr = nullptr;
    void *release_ptr = nullptr;
    char *data_ptr = nullptr;
    char *write_ptr = nullptr;
    size_t capacity;

    size_t emptySize();

};

#endif //ZAP_FLUSH_BUFFER_H
