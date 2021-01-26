//
// Created by #Suyghur, on 2021/1/18.
//

#ifndef ZAP_ZAP_BUFFER_HEADER_H
#define ZAP_ZAP_BUFFER_HEADER_H

#include <string>

namespace zap_header {
    static const char kMagicHeader = '\x11';

    struct Header {
        char magic;
        size_t log_len;
        size_t log_path_len;
        char *log_path;
        bool isCompress;
    };

    class ZapBufferHeader {

    public:
        ZapBufferHeader(void *data, size_t size);

        ~ZapBufferHeader();

        void initHeader(Header &header);

        void *originPtr();

        void *ptr();

        void *write_ptr();

        Header *getHeader();

        size_t getHeaderLen();

        size_t getLogLen();

        size_t getLogPathLen();

        char *getLogPath();

        void setLogLen(size_t log_len);

        bool getIsCompress();

        bool isAvailable();

        static size_t calculateHeaderLen(size_t log_path_len);

    private:
        char *data_ptr;
        size_t data_size;
    };
}


#endif //ZAP_ZAP_BUFFER_HEADER_H
