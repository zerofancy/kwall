package top.ntutn

import org.dom4j.Document
import org.dom4j.tree.DefaultDocument
import org.dom4j.tree.DefaultElement
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths


fun main() {
    println("随机更换一张pics中的壁纸。")
    // 检测config.xml是否存在，不存在则生成config.xml、data.xml、test.jpg
    val fileConfig = File("config.xml")
    if (!fileConfig.exists()) {
        println("未找到配置文件。第一次启动？")
        println("尝试释放配置文件。")
        PicUtil.getPicDirectory().mkdir()
        PicUtil.copyResourceTo("/config.xml", PicUtil.getConfigFileString())
        PicUtil.copyResourceTo("/data.xml", PicUtil.getDataFileString())
        PicUtil.copyResourceTo("/test.jpg", PicUtil.getTestFileString())
    }

    // 读取配置文件
    val documentConfig =
        XMLUtil.readXMLDocument(PicUtil.getConfigFileString())
            ?: DefaultDocument(DefaultElement("config"))
    documentConfig.elementByID("picPath")?.textTrim?.let {
        PicUtil.relativePicPathString = it
    }
    documentConfig.elementByID("command")?.textTrim?.let {
        PicUtil.command = it
    }

    // 读取data.xml，若为空，则重新遍历计算。若遍历结果空，do nothing
    val documentData =
        XMLUtil.readXMLDocument(PicUtil.getDataFileString())
            ?: DefaultDocument(DefaultElement("data"))
    val allPics = mutableListOf<String>()
    documentData.rootElement?.elements()?.forEach {
        allPics.add(it.textTrim)
    }

    if (allPics.isEmpty()) {
        println("缓存列表为空，重新扫描图片文件夹。")
        allPics.addAll(
            getAllChildrenFiles(PicUtil.getPicDirectory()).map {
                it.canonicalPath
            }
        )
    }
    if (allPics.isEmpty()) {
        println("未找到任何文件，请检查您的配置。")
        return
    }

    // 随机取出一条
    var randomFileString = allPics.random()
    allPics -= randomFileString
    var randomFile = File(randomFileString)

    // 检测取出文件是否存在，不存在则重复操作
    while (!(randomFile.isFile && randomFile.exists())) {
        if (allPics.isEmpty()) {
            //保存空结果，重新执行
            saveResult(documentData, allPics)
            main()
            return
        }
        randomFileString = allPics.random()
        allPics -= randomFileString
        randomFile = File(randomFileString)
    }

    println("最终取到的文件是$randomFileString，准备设置壁纸……")
    PicUtil.setWallPicture(randomFileString)
    saveResult(documentData, allPics)
}

fun saveResult(document: Document, allPics: MutableList<String>) {
    document.rootElement.elements().clear()
    allPics.forEach {
        document.rootElement.add(DefaultElement("item").apply { text = it })
    }
    XMLUtil.writeXMLDocument(document, PicUtil.getDataFile())
}

fun getAllChildrenFiles(rootFile: File): Array<File> {
    var res = arrayOf<File>()
    if (!rootFile.exists() || !rootFile.isDirectory || !rootFile.canRead()) {
        return res
    }
    rootFile.listFiles()?.forEach {
        if (it.isDirectory) {
            res += getAllChildrenFiles(it)
        } else {
            res += it
        }
    }
    return res
}

object PicUtil {

    var relativePicPathString = "./pics"
    var command = "echo \$pic"

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