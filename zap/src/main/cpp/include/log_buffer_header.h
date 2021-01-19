//
// Created by #Suyghur, on 2021/1/18.
//

#ifndef ZAP_LOG_BUFFER_HEADER_H
#define ZAP_LOG_BUFFER_HEADER_H

#include <string>

namespace log_header {
    static const char kMagicHeader = '\x11';
    struct Header {
        char magic;
        size_t log_len;
        size_t log_path_len;
        char *log_path;
        bool compress;
    };

    class LogBufferHeader {
    public:
        LogBufferHeader(void *data, size_t size);

        ~LogBufferHeader();

        void initHeader(Header &header);

        void *originPtr();

        void *ptr();

        void *writePtr();

        Header *getHeader();

        size_t getHeaderLen();

        size_t getLogLen();

        size_t getLogPathLen();

        char *getLogPath();

        void setLogLen(size_t len);

        bool isCompress();

        bool isAvailable();

        static size_t calculateHeaderLen(size_t len);

    private:
        char *data_ptr;
        size_t data_size;
    };
}

#endif //ZAP_LOG_BUFFER_HEADER_H
