package com.kbyamy.githubclient.common.util

import timber.log.Timber

class GithubClientDebugTree : Timber.DebugTree() {

    override fun createStackElementTag(element: StackTraceElement): String? {
        return "(${element.fileName}:${element.lineNumber})#${element.methodName}"
    }

}