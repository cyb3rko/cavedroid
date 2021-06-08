package com.cyb3rko.cavedroid.appintro

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import com.cyb3rko.cavedroid.*
import com.cyb3rko.cavedroid.ANALYTICS_COLLECTION
import com.cyb3rko.cavedroid.CRASHLYTICS_COLLECTION
import com.cyb3rko.cavedroid.SHARED_PREFERENCE
import com.github.appintro.AppIntro
import com.github.appintro.AppIntroFragment
import com.github.appintro.AppIntroPageTransformerType
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import java.text.SimpleDateFormat
import java.util.*

class MyAppIntro : AppIntro() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val backgroundColor = ResourcesCompat.getColor(resources, R.color.colorPrimaryVariant, theme)
        addSlide(AppIntroFragment.newInstance(
            title = "Welcome...",
            description = "This is the unofficial Cavetale Android client made by Cyb3rKo",
            imageDrawable = R.drawable.ic_launcher_playstore,
            backgroundColor = backgroundColor
        ))
        addSlide(AppIntroFragment.newInstance(
            title = "Feel free to contribute",
            imageDrawable = R.drawable._ic_github,
            description = "If you experience any problems or if you have a idea for a new feature, feel free to visit the GitHub repository and " +
                    "open an issue or a pull request.\nThanks!",
            backgroundColor = backgroundColor
        ))

        addSlide(AppIntro3rdFragment.newInstance())
        addSlide(AppIntro4thFragment.newInstance())
        addSlide(AppIntroFragment.newInstance(
            title = "Ready…",
            imageDrawable = R.drawable._ic_start,
            description = "OK, everything is set up. Enjoy!",
            backgroundColor = backgroundColor
        ))

        setTransformer(AppIntroPageTransformerType.Parallax())
        isWizardMode = true
        isSystemBackButtonLocked = true
    }

    override fun onDonePressed(currentFragment: Fragment?) {
        super.onDonePressed(currentFragment)
        val mySPR = applicationContext.getSharedPreferences(SHARED_PREFERENCE, 0)
        val editor = mySPR.edit()

        FirebaseAnalytics.getInstance(this).setAnalyticsCollectionEnabled(mySPR.getBoolean(ANALYTICS_COLLECTION, true))
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(mySPR.getBoolean(CRASHLYTICS_COLLECTION, true))

        val date = Calendar.getInstance().time
        @SuppressLint("SimpleDateFormat") val sDF = SimpleDateFormat("yyyy-MM-dd")
        @SuppressLint("SimpleDateFormat") val sDF2 = SimpleDateFormat("HH:mm:ss")
        editor.putString(CONSENT_DATE, sDF.format(date))
        editor.putString(CONSENT_TIME, sDF2.format(date))
        editor.putBoolean(FIRST_START, false).apply()

        finish()
        startActivity(Intent(applicationContext, MainActivity::class.java))
    }
}