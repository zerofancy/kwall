package top.ntutn

import org.dom4j.Document
import org.dom4j.Element
import org.dom4j.tree.DefaultDocument
import org.dom4j.tree.DefaultElement
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import javax.print.Doc

fun main() {
    println("随机更换一张pics中的壁纸。")
    // 检测config.xml是否存在，不存在则生成config.xml、data.xml、test.jpg
    val fileConfig = File("config.xml")
    if (!fileConfig.exists()) {
        println("未找到配置文件。第一次启动？")
        println("尝试释放配置文件。")
        ResourceUtil.getPicDictory().mkdir()
        ResourceUtil.copyResourceTo("/config.xml", ResourceUtil.getConfigFileString())
        ResourceUtil.copyResourceTo("/data.xml", ResourceUtil.getAbsolutePath(relative = "./data.xml"))
        ResourceUtil.copyResourceTo("/test.jpg", ResourceUtil.getAbsolutePath(relative = "./pics/test.jpg"))
    }

    // 读取data.xml，若为空，则重新遍历计算。若遍历结果空，do nothing
    var document = XMLUtil.readXMLDocument(ResourceUtil.getAbsolutePath(relative = "./data.xml"))
    val allPics = mutableListOf<String>()
    document = document ?: DefaultDocument(DefaultElement("data"))
    document.rootElement?.elements()?.forEach {
        allPics.add(it.textTrim)
    }

    if (allPics.isEmpty()) {
        println("缓存列表为空，重新扫描图片文件夹。")
        allPics.addAll(
                getAllChildrenFiles(File(ResourceUtil.getCurrentPath(), "./pics")).map {
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
            saveResult(document, allPics)
            main()
            return
        }
        randomFileString = allPics.random()
        allPics -= randomFileString
        randomFile = File(randomFileString)
    }
    println("最终取到的文件是$randomFileString")
    saveResult(document, allPics)
}

fun saveResult(document: Document, allPics: MutableList<String>) {
    document.rootElement.elements().clear()
    allPics.forEach {
        document.rootElement.add(DefaultElement("item").apply { text = it })
    }
    XMLUtil.writeXMLDocument(document, File(ResourceUtil.getAbsolutePath(relative = "./data.xml")))
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

    fun getPicDictory()=File(getCurrentPath(), "./pics")

    fun getConfigFileString()= getAbsolutePath(relative = "./config.xml")

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