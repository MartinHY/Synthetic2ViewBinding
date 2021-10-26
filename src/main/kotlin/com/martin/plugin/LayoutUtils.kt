package com.martin.plugin

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.module.ModuleUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope.everythingScope

/**
 * 作者：MartinBZDQSM on 2021/10/20 17:16
 * 博客：http://www.jianshu.com/users/78f0e5f4a403/latest_articles
 * github：https://github.com/MartinHY
 */
object LayoutUtils {
    fun getViewId(value: String?): String? {
        //@+id/line
        if (value == null || value.isEmpty()) return null
        if (!value.startsWith("@+id/") && !value.startsWith("@id/")) return null
        val result = value.split("/").toTypedArray()
        return if (result.size != 2) null else result[1]
    }

    fun canFindLayoutResourceElement(file: PsiFile, editor: Editor): Boolean {
        val offset = editor.caretModel.offset
        val candidateA = file.findElementAt(offset)
        val candidateB = file.findElementAt(offset - 1)
        var element = findLayoutResourceElement(candidateA)
        if (element == null) element = findLayoutResourceElement( candidateB)
        return element != null
    }

    fun getLayoutFileFromCaret(file: PsiFile, editor: Editor): PsiFile? {
        val offset = editor.caretModel.offset
        val candidateA = file.findElementAt(offset)
        val candidateB = file.findElementAt(offset - 1)
        val layout = findLayoutResource(candidateA)
        return layout ?: findLayoutResource(candidateB)
    }

    private fun findLayoutResourceElement(element: PsiElement?): PsiElement? {
        if (element == null) return null
        // no file to process
        val layout: PsiElement = element.parent.parent.firstChild
            ?: return null // no file to process

        return if ("R.layout" != layout.text) {
            null // not layout file
        } else layout
    }

    fun getLayoutFileFromCaretName(file: PsiFile, editor: Editor): String? {
        val offset = editor.caretModel.offset
        val candidateA = file.findElementAt(offset)
        val candidateB = file.findElementAt(offset - 1)
        val layout = findLayoutResourceName(candidateA)
        return layout ?: findLayoutResourceName(candidateB)
    }

    private fun findLayoutResourceName(element: PsiElement?): String? {
        if (element == null) {
            return null // nothing to be used
        }
        //        if (isKotlin) {
        //element.getParent().getParent(): R.layout.activity_main
        val layout: PsiElement = element.parent.parent.firstChild
                ?: return null // no file to process
        //        } else {
//            //element.getParent(): R.layout.activity_main
//            layout = element.getParent().getFirstChild();
//            if (layout == null) {
//                return null; // no file to process
//            }
//        }
        return if ("R.layout" != layout.text) {
            null // not layout file
        } else element.text
    }

    private fun findLayoutResource(element: PsiElement?): PsiFile? {
        if (element == null) {
            return null // nothing to be used
        }
        val layout: PsiElement = element.parent.parent.firstChild
                ?: return null
        // no file to process
        //        if (isKotlin) {
        //element.getParent().getParent(): R.layout.activity_main
        //        } else {
//            //element.getParent(): R.layout.activity_main
//            layout = element.getParent().getFirstChild();
//            if (layout == null) {
//                return null; // no file to process
//            }
//        }
        if ("R.layout" != layout.text) {
            return null // not layout file
        }
        val name = String.format("%s.xml", element.text)
        return resolveLayoutResourceFile(element, name)
    }

    fun resolveLayoutResourceFile(element: PsiElement?, layoutName: String?): PsiFile? {
        if (element == null || layoutName == null) return null
        val project = element.project
        val module = ModuleUtil.findModuleForPsiElement(element)
        var files: Array<PsiFile>? = null
        if (module != null) {
            val moduleScope = module.getModuleWithDependenciesAndLibrariesScope(false)
            files = FilenameIndex.getFilesByName(project, layoutName, moduleScope)
        }
        if (files == null || files.isEmpty()) {
            files = FilenameIndex.getFilesByName(project, layoutName, everythingScope(project))
        }
        return if (files.isEmpty()) {
            null //no matching files
        } else files[0]
    }

    fun getLayoutName(layout: String?): String? {
        if (layout == null || !layout.startsWith("@layout/")) {
            return null // it's not layout identifier
        }
        val parts = layout.split("/").toTypedArray()
        return if (parts.size != 2) {
            null // not enough parts
        } else parts[1]
    }
}