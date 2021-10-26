package com.martin.plugin

import com.google.common.base.CaseFormat
import com.intellij.openapi.command.WriteCommandAction.writeCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiFile
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtPsiFactory


/**
 * SynthiticReplaceMethod
 * @author Martin
 * @email hy569835826@163.com
 * @descraption
 * @time 2021/10/21 14:00
 */
class SynthiticReplaceMethod(
    mEditor: Editor,
    mFile: KtFile,
    private var mClass: KtClass
) {

    private val mProject = mClass.project

    // 获取Factory
    private val mFactory: KtPsiFactory = KtPsiFactory(mProject, false)

    private val bindingCode = generateViewBindingBaseCode(mFile, mEditor)


    fun doWriteAction(viewIdInfoAsList: List<ViewInfo>) {
        writeCommandAction(mProject).run<Exception> {
            try {
                var classStr = mClass.text
                //按文字长度降序进行替换 , 以防重复替换子元素
                val viewList = viewIdInfoAsList.sortedByDescending {
                    it.id.length
                }
                for (info in viewList) {
                    var str = String.format(
                        Constants.COMMON_VIEW,
                        info.id
                    )
                    if (info.id.contains("_")){
                        str = String.format(
                            Constants.COMMON_VIEW,
                            CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, info.id)
                        )
                    }

                    println("SynthiticReplaceMethod  info.id ${info.id}   str $str")
                    classStr = classStr.replace(
                        info.id, str
                    )
                }
                mClass = mClass.replace(mFactory.createClass(classStr)) as KtClass
                mClass.body?.addAfter(mFactory.createProperty(bindingCode), mClass.body?.firstChild)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * @return 生成 viewbinding 构建方法
     * private val binding by viewBinding(%sBinding::bind)
     */
    private fun generateViewBindingBaseCode(psiFile: KtFile, editor: Editor): String {
        var bindingCode = LayoutUtils.getLayoutFileFromCaretName(psiFile as PsiFile, editor)
        bindingCode = CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, bindingCode!!)
        bindingCode = String.format(Constants.COMMON_VIEW_BINDGIN, bindingCode)
        return bindingCode
    }

}