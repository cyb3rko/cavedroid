package com.cyb3rko.cavedroid

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        AppCompatDelegate.setDefaultNightMode(getSharedPreferences(SHARED_PREFERENCE, MODE_PRIVATE).getString(NIGHTMODE, AppCompatDelegate
            .MODE_NIGHT_FOLLOW_SYSTEM.toString())!!.toInt())
    }
}