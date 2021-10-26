package com.martin.plugin

/**
 * 作者：MartinBZDQSM on 2021/10/20 18:05
 * 博客：http://www.jianshu.com/users/78f0e5f4a403/latest_articles
 * github：https://github.com/MartinHY
 */
interface Constants {
    companion object {
        const val COMMON_VIEW_BINDGIN = "private val binding by viewBinding(%sBinding::bind)"

        const val COMMON_VIEW = "binding.%s"
    }
}