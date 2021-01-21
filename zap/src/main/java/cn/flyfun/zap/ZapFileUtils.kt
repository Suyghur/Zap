package cn.flyfun.zap

import android.content.Context
import android.text.TextUtils
import java.io.*
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * @author #Suyghur,
 * Created on 2021/1/20
 */
object ZapFileUtils {


    fun getAllLogFiles(path: String): MutableList<String> {
        val fileTree = File(path).walk()
        val logs = mutableListOf<String>()
        fileTree.maxDepth(1)
                .filter { it.isFile }
                .filter { it.extension == "txt" }
                .forEach {
                    logs.add(it.name)
                }
        return logs
    }

    fun packLogFiles(context: Context): String {
        val path = context.getExternalFilesDir("zap")?.absolutePath
        path?.let {
            val logFiles = getAllLogFiles(it)
            for (log in logFiles) {
                copyFile(File("$it/$log"), File("$it/tmp/$log"))
            }
            zipFolder("$it/tmp", "$it/tmp/log.zip")
            return "$it/tmp/log.zip"
        }
        return ""
    }

    /**
     * 复制文件
     *
     * @param src
     * @param dest
     * @return
     */
    fun copyFile(src: File, dest: File): Boolean {
        if (!src.exists()) {
            Zap.w("src file is not exist : ${src.absolutePath}")
            return false
        }

        if (!dest.exists()) {
            Zap.e("dest file is not exist : ${dest.absolutePath} , auto create")
            createNewFile(dest.absolutePath)
        }

        var bis: BufferedInputStream? = null
        var bos: BufferedOutputStream? = null
        try {
            bis = BufferedInputStream(FileInputStream(src))
            bos = BufferedOutputStream(FileOutputStream(dest))

            val buffer = ByteArray(4 * 1024)
            var count = 0
            while (bis.read(buffer, 0, buffer.size).also { count = it } != -1) {
                if (count > 0) {
                    bos.write(buffer, 0, count)
                }
            }
            bos.flush()
            return true
        } catch (e: Exception) {
            Zap.e("catch exception : ", e)
            e.printStackTrace()
        } finally {
            bis?.close()
            bos?.close()
        }
        return false
    }

    /**
     * 创建文件夹
     *
     * @param dirPath
     * @return
     */
    fun mkdirs(dirPath: String): File? {
        if (TextUtils.isEmpty(dirPath)) {
            Zap.e("mkdirs error , dir path is empty")
            return null
        }
        val dir = File(dirPath)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    /**
     * 删除文件
     *
     * @param filePath
     * @return
     */
    fun deleteFile(filePath: String): Boolean {
        if (TextUtils.isEmpty(filePath)) {
            Zap.e("delete file error , file path is empty")
            return false
        }
        val file = File(filePath)

        if (!file.exists()) {
            return false
        }
        if (file.isDirectory) {
            for (f in file.listFiles()) {
                deleteFile(f.absolutePath)
            }
        } else {
            return file.delete()
        }
        return file.delete()
    }

    /**
     * 创建文件
     *
     * @param filePath
     * @return
     */
    private fun createNewFile(filePath: String): File? {
        if (TextUtils.isEmpty(filePath)) {
            Zap.e("create new file error , file path is empty")
            return null
        }
        val file = File(filePath)
        val parentFile = file.parentFile!!
        if (!parentFile.exists()) {
            parentFile.mkdirs()
        }
        if (!file.exists()) {
            file.createNewFile()
        }
        return file
    }

    fun zip(files: List<File>, zipFilePath: String) {
        if (files.isEmpty()) return

        val zipFile = createNewFile(zipFilePath)
        val buffer = ByteArray(1024)
        var zipOutputStream: ZipOutputStream? = null
        var inputStream: FileInputStream? = null
        try {
            zipOutputStream = ZipOutputStream(FileOutputStream(zipFile))
            for (file in files) {
                if (!file.exists()) continue
                zipOutputStream.putNextEntry(ZipEntry(file.name))
                inputStream = FileInputStream(file)
                var len: Int
                while (inputStream.read(buffer).also { len = it } > 0) {
                    zipOutputStream.write(buffer, 0, len)
                }
                zipOutputStream.closeEntry()
            }
        } finally {
            inputStream?.close()
            zipOutputStream?.close()
        }
    }

    fun zipFolder(fileDir: String, zipFilePath: String) {
        val folder = File(fileDir)
        if (folder.exists() && folder.isDirectory) {
            val files = folder.listFiles()
            val filesList: List<File> = files.toList()
            zip(filesList, zipFilePath)
        }
    }
}