package top.ntutn

import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths

object PicUtil {

    var relativePicPathString = "./pics"
    var command = "echo \$pic"
    var types = mutableListOf<String>()

    /**
     * 获取当前路径
     */
    private fun getCurrentPath(): String = System.getProperty("user.dir")

    /**
     * 获取绝对路径
     * @param currentPath 父文件夹路径
     * @param relative 相对路径
     */
    private fun getAbsolutePath(currentPath: String? = null, relative: String): String = File(
        currentPath
            ?: getCurrentPath(), relative
    ).canonicalPath

    fun getConfigFileString() = getAbsolutePath(relative = "./config.xml")
    fun getDataFileString() = getAbsolutePath(relative = "./data.xml")
    fun getTestFileString() = getAbsolutePath(relative = "./pics/test.jpg")

    fun getPicDirectory() = File(getCurrentPath(), relativePicPathString)
    fun getDataFile() = File(getDataFileString())

    /**
     * 将资源文件复制到指定位置
     * @param resource 资源文件，“/”开头
     * @param to 目标位置，绝对路径，包括文件名
     */
    fun copyResourceTo(resource: String, to: String) {

        try {
            val ins = this::class.java.getResourceAsStream(resource)
            val dist = Paths.get(to)
            Files.copy(ins, dist)
        } catch (e: Throwable) {
            when (e) {
                is FileAlreadyExistsException, is java.nio.file.FileAlreadyExistsException ->
                    println("文件${to}已经存在了。")
            }
        }
    }

    /**
     * 设置壁纸
     * @param url
     */
    fun setWallPicture(url: String) {
        try {
            val finalCommand = command.replace("\$pic", url)
            println("执行命令$finalCommand")
            Runtime.getRuntime().exec(finalCommand)
        } catch (e: IOException) {
            System.err.println("设置壁纸失败：$e")
        }
    }
}