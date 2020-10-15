package top.ntutn

import org.dom4j.Document
import org.dom4j.tree.DefaultDocument
import org.dom4j.tree.DefaultElement
import java.io.File


fun main() {
    println("随机更换一张pics中的壁纸。")
    // 检测config.xml是否存在，不存在则生成config.xml、data.xml、test.jpg
    val fileConfig = File("config.xml")
    if (!fileConfig.exists()) {
        println("未找到配置文件。第一次启动？")
        println("尝试释放配置文件。")
        PicUtil.getPicDirectory().mkdir()
        PicUtil.copyResourceTo("/config.xml", PicUtil.getConfigFileString())
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
    documentConfig.elementByID("type")?.textTrim?.split('|')?.forEach {
        PicUtil.types.add(it)
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
                getAllChildrenFiles(PicUtil.getPicDirectory()).filter {
                    it.extension in PicUtil.types
                }.map {
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
