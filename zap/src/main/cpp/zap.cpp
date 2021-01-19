//
// Created by #Suyghur, on 2021/1/15.
//

#include <jni.h>

#include <sys/stat.h>
#include <fcntl.h>
#include <sstream>

#include "include/zap_buffer.h"

static const char *const kClassDocScanner = "cn/flyfun/zap/ZapBuffer";

static char *openMMap(int _buffer_fd, size_t _buffer_size);

static void writeDirtyLogToFile(int _buffer_fd);

static AsyncFileFlush *pAsyncFileFlush = nullptr;

static jlong initNative(JNIEnv *env, jclass clazz, jstring _buffer_path,
                        jint _capacity, jstring _log_path, jboolean _compress) {
    const char *buffer_path = env->GetStringUTFChars(_buffer_path, JNI_FALSE);
    const char *log_path = env->GetStringUTFChars(_log_path, JNI_FALSE);
    auto buffer_size = static_cast<size_t>(_capacity);
    int buffer_fd = open(buffer_path, O_RDWR | O_CREAT, S_IRUSR | S_IWUSR | S_IRGRP | S_IROTH);
    //buffer 的第一个字节会用于存储日志路径名称长度，后面紧跟日志路径，之后才是日志信息
    if (pAsyncFileFlush == nullptr) {
        pAsyncFileFlush = new AsyncFileFlush();
    }
    //加上头占用的大小
    buffer_size = buffer_size + ZapBufferHeader::calculateHeaderLen(strlen(log_path));
    char *buffer_ptr = openMMap(buffer_fd, buffer_size);
    bool map_buffer = true;
    //如果打开 mmap 失败，则降级使用内存缓存
    if (buffer_ptr == nullptr) {
        buffer_ptr = new char[buffer_size];
        map_buffer = false;
    }
    auto *pZapBuffer = new ZapBuffer(buffer_ptr, buffer_size);
    pZapBuffer->setAsyncFileFlush(pAsyncFileFlush);
    //将buffer内的数据清0， 并写入日志文件路径
    pZapBuffer->initData((char *) log_path, strlen(log_path), _compress);
    pZapBuffer->map_buffer = map_buffer;

    env->ReleaseStringUTFChars(_buffer_path, buffer_path);
    env->ReleaseStringUTFChars(_log_path, log_path);
    return reinterpret_cast<long>(pZapBuffer);
}

static char *openMMap(int _buffer_fd, size_t _buffer_size) {
    char *map_ptr = nullptr;
    if (_buffer_fd != -1) {
        // 写脏数据
        writeDirtyLogToFile(_buffer_fd);
        // 根据 buffer size 调整 buffer 文件大小
        ftruncate(_buffer_fd, static_cast<int>(_buffer_size));
        lseek(_buffer_fd, 0, SEEK_SET);
        map_ptr = (char *) mmap(0, _buffer_size, PROT_WRITE | PROT_READ, MAP_SHARED, _buffer_fd, 0);
        if (map_ptr == MAP_FAILED) {
            map_ptr = nullptr;
        }
    }
    return map_ptr;
}

static void writeDirtyLogToFile(int _buffer_fd) {
    struct stat file_stat{};
    if (fstat(_buffer_fd, &file_stat) >= 0) {
        auto buffered_size = static_cast<size_t>(file_stat.st_size);
        // buffer_size 必须是大于文件头长度的，否则会导致下标溢出
        if (buffered_size > ZapBufferHeader::calculateHeaderLen(0)) {
            char *buffer_ptr_tmp = (char *) mmap(0, buffered_size, PROT_WRITE | PROT_READ,
                                                 MAP_SHARED, _buffer_fd, 0);
            if (buffer_ptr_tmp != MAP_FAILED) {
                auto *tmp = new ZapBuffer(buffer_ptr_tmp, buffered_size);
                size_t data_size = tmp->length();
                if (data_size > 0) {
                    tmp->asyncFlush(pAsyncFileFlush, tmp);
                } else {
                    delete tmp;
                }
            }
        }
    }
}

static void writeNative(JNIEnv *env, jobject instance, jlong _ptr, jstring _log) {
    const char *log = env->GetStringUTFChars(_log, 0);
    jsize log_len = env->GetStringUTFLength(_log);
    auto *pZapBuffer = reinterpret_cast<ZapBuffer *>(_ptr);
    // 缓存写不下时异步刷新
    if (log_len >= pZapBuffer->emptySize()) {
        pZapBuffer->asyncFlush(pAsyncFileFlush);
    }
    pZapBuffer->append(log, (size_t) log_len);
    env->ReleaseStringUTFChars(_log, log);
}

static void releaseNative(JNIEnv *env, jobject instance, jlong _ptr) {
    auto *pZapBuffer = reinterpret_cast<ZapBuffer *>(_ptr);
    pZapBuffer->asyncFlush(pAsyncFileFlush, pZapBuffer);
    if (pAsyncFileFlush != nullptr) {
        delete pAsyncFileFlush;
    }
    pAsyncFileFlush = nullptr;
}

static void changeLogPathNative(JNIEnv *env, jobject instance, jlong _ptr, jstring logFilePath) {
    const char *log_path = env->GetStringUTFChars(logFilePath, 0);
    auto *pZapBuffer = reinterpret_cast<ZapBuffer *>(_ptr);
    pZapBuffer->changeLogPath(const_cast<char *>(log_path));
    env->ReleaseStringUTFChars(logFilePath, log_path);
}

static void flushAsyncNative(JNIEnv *env, jobject instance, jlong _ptr) {
    auto *pZapBuffer = reinterpret_cast<ZapBuffer *>(_ptr);
    pZapBuffer->asyncFlush(pAsyncFileFlush);
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
    JNIEnv *env = nullptr;
    if (vm->GetEnv((void **) &env, JNI_VERSION_1_4) != JNI_OK) {
        return JNI_FALSE;
    }
    jclass classDocScanner = env->FindClass(kClassDocScanner);
    if (env->RegisterNatives(classDocScanner, gMethods, sizeof(gMethods) / sizeof(gMethods[0])) <
        0) {
        return JNI_FALSE;
    }
    return JNI_VERSION_1_4;
}