package top.ntutn

import java.io.File
import java.nio.file.Files
import java.nio.file.Paths

fun main() {
    println("随机更换一张pics中的壁纸。")
    // 检测config.xml是否存在，不存在则生成config.xml、data.xml、test.jpg
    val fileConfig = File("config.xml")
    if (!fileConfig.exists()) {
        println("未找到配置文件。第一次启动？")
        println("尝试释放配置文件。")
        File(ResourceUtil.getCurrentPath(), "./pics").mkdir()
        ResourceUtil.copyResourceTo("/config.xml", ResourceUtil.getAbsolutePath(relative = "./config.xml"))
        ResourceUtil.copyResourceTo("/data.xml", ResourceUtil.getAbsolutePath(relative = "./data.xml"))
        ResourceUtil.copyResourceTo("/test.jpg", ResourceUtil.getAbsolutePath(relative = "./pics/test.jpg"))
    }

    // 读取data.xml，若为空，则重新遍历计算。若遍历结果空，do nothing
    val document = XMLUtil.readXMLDocument(ResourceUtil.getAbsolutePath(relative = "./data.xml"))
    val allPics = mutableListOf<String>()
    document?.rootElement?.elements()?.forEach {
        allPics.add(it.textTrim)
    }

    if (allPics.isEmpty()) {

    }

    // 随机取出一条
    // 检测取出文件是否存在，不存在则重复操作

}

object ResourceUtil {
    /**
     * 获取当前路径
     */
    fun getCurrentPath() = System.getProperty("user.dir");

    /**
     * 获取绝对路径
     * @param currentPath 父文件夹路径
     * @param relative 相对路径
     */
    fun getAbsolutePath(currentPath: String? = null, relative: String) = File(currentPath
            ?: getCurrentPath(), relative).canonicalPath

    /**
     * 获取主配置文件所在的路径（包括文件名）
     */
    fun getConfigPathAndFilename() = getAbsolutePath(getCurrentPath(), "./config.xml")

    /**
     * 获取资源文件所在的位置
     */
    fun getResourcePath() = this::class.java.classLoader.getResource("")

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
}