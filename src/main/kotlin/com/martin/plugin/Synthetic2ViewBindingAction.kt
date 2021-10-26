package com.martin.plugin

import com.google.common.base.CaseFormat
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiFile
import com.intellij.psi.SyntheticElement
import com.intellij.psi.util.PsiTreeUtil
import com.martin.plugin.LayoutUtils.canFindLayoutResourceElement
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtFile


/**
 * 作者：MartinBZDQSM on 2021/10/20 10:47
 * 博客：http://www.jianshu.com/users/78f0e5f4a403/latest_articles
 * github：https://github.com/MartinHY
 */
class Synthetic2ViewBindingAction : AnAction() {

    override fun actionPerformed(event: AnActionEvent) {
        val project = event.project
        val psiFile = event.getData(CommonDataKeys.PSI_FILE)
        val editor = event.getData(CommonDataKeys.EDITOR)
        //不是 kt 文件且不是 java 文件且选中的 id 不是 layout 类型 , 直接返回
        if (project == null || psiFile == null || editor == null) return
        val fileName = psiFile.virtualFile.name
        if (!fileName.endsWith(".kt") || !canFindLayoutResourceElement(psiFile, editor)) {
            return
        }
        val layoutFile = LayoutUtils.getLayoutFileFromCaret(psiFile, editor)
        ViewIds.instance.collectViewId(layoutFile)

        val declarations = (psiFile as KtFile).declarations
        val ktClass = declarations[0] as KtClass
        SynthiticReplaceMethod(editor, psiFile, ktClass).doWriteAction(ViewIds.instance.viewIdInfoAsList)
    }

}