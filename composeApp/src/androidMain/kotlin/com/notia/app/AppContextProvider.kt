package com.notia.app

import android.annotation.SuppressLint
import android.content.Context

@SuppressLint("StaticFieldLeak")
object AppContextProvider {
    var context: Context? = null
}
