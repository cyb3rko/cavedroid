package com.cyb3rko.cavedroid.appintro

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import com.cyb3rko.cavedroid.*
import com.cyb3rko.cavedroid.ANALYTICS_COLLECTION
import com.cyb3rko.cavedroid.CRASHLYTICS_COLLECTION
import com.cyb3rko.cavedroid.SHARED_PREFERENCE
import com.github.appintro.AppIntro
import com.github.appintro.AppIntroFragment
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import java.text.SimpleDateFormat
import java.util.*

class MyAppIntro : AppIntro() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val backgroundColor = ResourcesCompat.getColor(resources, R.color.colorPrimaryContainer, theme)
        addSlide(AppIntroFragment.newInstance(
            title = getString(R.string.intro_fragment1_title),
            description = getString(R.string.intro_fragment1_description),
            imageDrawable = R.drawable.ic_launcher_playstore,
            backgroundColor = backgroundColor
        ))
        addSlide(AppIntroFragment.newInstance(
            title = getString(R.string.intro_fragment2_title),
            imageDrawable = R.drawable._ic_github,
            description = getString(R.string.intro_fragment2_description),
            backgroundColor = backgroundColor
        ))

        addSlide(AppIntro3rdFragment.newInstance())
        addSlide(AppIntro4thFragment.newInstance())
        addSlide(AppIntroFragment.newInstance(
            title = getString(R.string.intro_fragment5_title),
            imageDrawable = R.drawable._ic_start,
            description = getString(R.string.intro_fragment5_description),
            backgroundColor = backgroundColor
        ))

        isWizardMode = true
        isSystemBackButtonLocked = true
    }

    override fun onDonePressed(currentFragment: Fragment?) {
        super.onDonePressed(currentFragment)

        try {
            val mySPR = applicationContext.getSharedPreferences(SHARED_PREFERENCE, MODE_PRIVATE)
            val editor = mySPR.edit()

            FirebaseAnalytics.getInstance(this).setAnalyticsCollectionEnabled(mySPR.getBoolean(ANALYTICS_COLLECTION, true))
            FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(mySPR.getBoolean(CRASHLYTICS_COLLECTION, true))

            val date = Calendar.getInstance().time
            @SuppressLint("SimpleDateFormat") val sDF = SimpleDateFormat("yyyy-MM-dd")
            @SuppressLint("SimpleDateFormat") val sDF2 = SimpleDateFormat("HH:mm:ss")
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                editor.putBoolean(OLD_ANDROID, true)
                editor.putBoolean(SHOW_ANNOUNCEMENTS, false)
                editor.putBoolean(ANNOUNCEMENT_IMAGE, false)
            }
            editor.putString(CONSENT_DATE, sDF.format(date))
            editor.putString(CONSENT_TIME, sDF2.format(date))
            editor.putBoolean(FIRST_START, false).apply()
        } catch (e: Exception) {
            Log.e("Cavedroid.Intro", "${e.cause}, ${e.message!!}")
        }

        finish()
        startActivity(Intent(applicationContext, MainActivity::class.java))
    }
}