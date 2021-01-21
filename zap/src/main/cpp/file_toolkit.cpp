//
// Created by #Suyghur, on 2021/1/20.
//

#include <logger.h>
#include <dirent.h>
#include "include/file_toolkit.h"

void FileToolkit::showAllFiles(const std::string &dir_name) {
    if (dir_name.empty()) {
        Logger::loge("dir is null!");
        return;
    }

    DIR *dir = opendir(dir_name.c_str());
    if (dir == nullptr) {
        Logger::loge("can not open dir");
        return;
    }

    struct dirent *file;
    while ((file = readdir(dir)) != nullptr) {
        //skip "." and ".."
        std::string file_name = file->d_name;
        if (strcmp(file->d_name, ".") == 0 || strcmp(file->d_name, "..") == 0) {
            Logger::logd("ignore : " + std::string(file->d_name));
            continue;
        }
//        if (file->d_type == DT_DIR) {
//            std::string file_path = dir_name + "/" + file->d_name;
//            showAllFiles(file_path);
//        } else {
//            Logger::logd("file path : " + dir_name + "/" + file->d_name);
//        }
        if (file->d_type != DT_DIR) {
            Logger::logd("file path : " + dir_name + "/" + file->d_name);
            Logger::logd("file name : " + file_name);

            std::string file_type = file_name.substr(file_name.find_last_of('.') + 1);
            Logger::logd("file type : " + file_type);
        }
    }
    closedir(dir);
}
