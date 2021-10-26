package com.martin.plugin

import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.XmlRecursiveElementVisitor
import com.intellij.psi.xml.XmlTag

/**
 * 作者：MartinBZDQSM on 2021/10/20 18:24
 * 博客：http://www.jianshu.com/users/78f0e5f4a403/latest_articles
 * github：https://github.com/MartinHY
 */
class ViewIds private constructor() {
    /**
     * layout resources filename.
     */
    private val mLayoutFileNames: MutableSet<String> = HashSet()

    /**
     * K:id,V:ViewInfo.
     */
    private val mViewIdMappingTable: MutableMap<String, ViewInfo> = LinkedHashMap()

    val layoutFileNames: Set<String>
        get() = mLayoutFileNames

    val viewIdMappingTable: Map<String, ViewInfo>
        get() = mViewIdMappingTable

    val viewIdInfoAsList: List<ViewInfo>
        get() {
            val infoList: MutableList<ViewInfo> = ArrayList()
            for ((_, info) in mViewIdMappingTable) {
                if (StringUtil.isEmpty(info.id)) continue
                infoList.add(info)
            }
            return infoList
        }

    fun clear() {
        mLayoutFileNames.clear()
        mViewIdMappingTable.clear()
    }

    fun collectViewId(layoutFile: PsiFile?) {
        clear()
        searchViewId(layoutFile)
    }

    private fun searchViewId(layoutFile: PsiFile?) {
        if (layoutFile == null) return
        mLayoutFileNames.add(layoutFile.name)
        layoutFile.accept(object : XmlRecursiveElementVisitor() {
            override fun visitElement(element: PsiElement) {
                super.visitElement(element)
                if (element !is XmlTag) return
                if (filterTag(element.name)) return
                if ("include" == element.name) {
                    val layoutAttribute = element.getAttribute("layout")
                    if (layoutAttribute != null) {
                        val includeFile = LayoutUtils.resolveLayoutResourceFile(layoutAttribute.valueElement, String.format("%s.xml", LayoutUtils.getLayoutName(layoutAttribute.value)))
                        includeFile?.let { searchViewId(it) }
                    }
                } else {
                    val idAttribute = element.getAttribute("android:id") ?: return
                    // missing android:id attribute
                    val value = idAttribute.value
                    if (value == null || value.isEmpty()) return  // empty value
                    var name = element.name
                    val clazz = element.getAttribute("class")
                    name = clazz?.value ?: ""
                    val id = LayoutUtils.getViewId(value)
                    if (id == null || id.isEmpty() ) return
                    val viewInfo = ViewInfo(name, id)
                    mViewIdMappingTable[id] = viewInfo
                }
            }
        })
    }

    private fun filterTag(name: String): Boolean {
        return "view" == name || "tag" == name || "requestFocus" == name
    }

    companion object {
        @Volatile
        private var sInstance: ViewIds? = null
        val instance: ViewIds
            get() {
                if (sInstance == null) {
                    synchronized(ViewIds::class.java) {
                        if (sInstance == null) {
                            sInstance = ViewIds()
                        }
                    }
                }
                return sInstance!!
            }
    }
}