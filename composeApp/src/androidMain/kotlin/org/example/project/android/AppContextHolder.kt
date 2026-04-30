package org.example.project.android

import android.app.Application

object AppContextHolder {
    @Volatile
    var application: Application? = null
}
