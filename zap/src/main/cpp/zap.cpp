//
// Created by #Suyghur, on 2021/1/15.
//


#include <jni.h>

#include <sys/stat.h>
#include <fcntl.h>
#include <sstream>
#include <logger.h>

#include "include/log_buffer.h"


static const char *const kClzDocScanner = "cn/flyfun/zap/ZapBuffer";

static char *onpenMMap(int buffer_fd, size_t buffer_size);

static void writeDirtyLog2File(int buffer_fd);

static char *openMMap(int fd, size_t size);

static AsyncFileFlush *file_flush = nullptr;


static jlong
initNative(JNIEnv *env, jclass clazz, jstring buffer_path,
           jint capacity, jstring log_path, jboolean compress) {
    const char *_buffer_path = env->GetStringUTFChars(buffer_path, JNI_FALSE);
    const char *_log_path = env->GetStringUTFChars(log_path, JNI_FALSE);
    auto _buffer_size = static_cast<size_t>(capacity);
    int _buffer_fd = open(_buffer_path, O_RDWR | O_CREAT, S_IRUSR | S_IWUSR | S_IRGRP | S_IROTH);
    //buffer 的第一个字节会用于存储日志路径名称长度，后面紧跟日志路径，之后才是日志信息
    if (file_flush == nullptr) {
        file_flush = new AsyncFileFlush();
    }
    //加上头占用的大小
    _buffer_size = _buffer_size + LogBufferHeader::calculateHeaderLen(strlen(_log_path));
    char *_buffer_ptr = openMMap(_buffer_fd, _buffer_size);
    bool _map_buffer = true;
    //如果打开mmap失败，则降级使用内存缓存
    if (_buffer_ptr == nullptr) {
        _buffer_ptr = new char[_buffer_size];
        _map_buffer = false;
    }
    auto *_log_buffer = new LogBuffer(_buffer_ptr, _buffer_size);
    _log_buffer->setAsyncFileFlush(file_flush);
    //讲buffer内的数据清0，并写入日志文件路径
    _log_buffer->initData((char *) _log_path, strlen(_log_path), compress);
    _log_buffer->map_buffer = _map_buffer;

    env->ReleaseStringUTFChars(buffer_path, _buffer_path);
    env->ReleaseStringUTFChars(log_path, _log_path);
    return reinterpret_cast<long>(_log_buffer);
}

static char *openMMap(int buffer_fd, size_t buffer_size) {
    char *_map_ptr = nullptr;
    if (buffer_fd != -1) {
        //写脏数据
        writeDirtyLog2File(buffer_fd);
        //根据buffer_size调整buffer文件大小
        ftruncate(buffer_fd, static_cast<int>(buffer_size));
        lseek(buffer_fd, 0, SEEK_SET);
        _map_ptr = (char *) mmap(0, buffer_size, PROT_WRITE | PROT_READ, MAP_SHARED, buffer_fd, 0);
        if (_map_ptr == MAP_FAILED) {
            _map_ptr = nullptr;
        }
    }
    return _map_ptr;
}

static void writeDirtyLog2File(int buffer_fd) {
    struct stat _file_info{};
    if (fstat(buffer_fd, &_file_info) >= 0) {
        auto _buffered_size = static_cast<size_t>(_file_info.st_size);
        //buffer_size必须是大于头文件长度的，否则会导致下标溢出
        if (_buffered_size > LogBufferHeader::calculateHeaderLen(0)) {
            char *_buffer_ptr_map = (char *) mmap(0, _buffered_size, PROT_WRITE | PROT_READ,
                                                  MAP_SHARED, buffer_fd, 0);
            if (_buffer_ptr_map != MAP_FAILED) {
                auto *_tmp = new LogBuffer(_buffer_ptr_map, _buffered_size);
                size_t _data_size = _tmp->length();
                if (_data_size > 0) {
                    _tmp->asyncFlush(file_flush, _tmp);
                } else {
                    delete _tmp;
                }
            }
        }
    }
}

static void writeNative(JNIEnv *env, jobject instance, jlong ptr, jstring log) {
    const char *_log = env->GetStringUTFChars(log, JNI_FALSE);
    Logger::logd(_log);
    jsize _log_len = env->GetStringUTFLength(log);
    auto *_log_buffer = reinterpret_cast<LogBuffer *>(ptr);
    //缓存写不下时异步刷新
    if (_log_len >= _log_buffer->emptySize()) {
        _log_buffer->asyncFlush(file_flush);
    }
    _log_buffer->append(_log, (size_t) _log_len);
    env->ReleaseStringUTFChars(log, _log);
}

static void releaseNative(JNIEnv *env, jobject instance, jlong ptr) {
    auto *_log_buffer = reinterpret_cast<LogBuffer *>(ptr);
    _log_buffer->asyncFlush(file_flush, _log_buffer);
    delete file_flush;
    file_flush = nullptr;
}

static void changeLogPathNative(JNIEnv *env, jobject instance, jlong ptr, jstring log_file_path) {
    const char *_log_path = env->GetStringUTFChars(log_file_path, JNI_FALSE);
    auto *_log_buffer = reinterpret_cast<LogBuffer *>(ptr);
    _log_buffer->changeLogPath(const_cast<char *>(_log_path));
    env->ReleaseStringUTFChars(log_file_path, _log_path);
}

static void flushAsyncNative(JNIEnv *env, jobject instance, jlong ptr) {
    auto *_log_buffer = reinterpret_cast<LogBuffer *>(ptr);
    _log_buffer->asyncFlush(file_flush);
}

static JNINativeMethod gMethods[] = {
        {
                "initNative",
                "(Ljava/lang/String;ILjava/lang/String;Z)J",
                (void *) initNative
        },
        {
                "writeNative",
                "(JLjava/lang/String;)V",
                (void *) writeNative
        },
        {
                "flushAsyncNative",
                "(J)V",
                (void *) flushAsyncNative
        },
        {
                "changeLogPathNative",
                "(JLjava/lang/String;)V",
                (void *) changeLogPathNative
        },
        {
                "releaseNative",
                "(J)V",
                (void *) releaseNative
        }
};

extern "C"
JNIEXPORT jint JNICALL
JNI_OnLoad(JavaVM *vm, void *reserved) {
    JNIEnv *_env = nullptr;
    if (vm->GetEnv((void **) &_env, JNI_VERSION_1_4) != JNI_OK) {
        return JNI_FALSE;
    }
    jclass _clz_doc_scanner = _env->FindClass(kClzDocScanner);
    if (_env->RegisterNatives(_clz_doc_scanner, gMethods,
                              sizeof(gMethods) / sizeof(gMethods[0])) < 0) {
        return JNI_FALSE;
    }
    return JNI_VERSION_1_4;
}
