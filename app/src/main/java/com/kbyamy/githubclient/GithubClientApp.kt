package com.kbyamy.githubclient

import android.app.Application
import com.kbyamy.githubclient.common.util.GithubClientDebugTree
import timber.log.Timber

class GithubClientApp : Application() {

    override fun onCreate() {
        super.onCreate()

        setupTimber()
    }

    private fun setupTimber() {
        if (BuildConfig.DEBUG) Timber.plant(GithubClientDebugTree())
    }
}