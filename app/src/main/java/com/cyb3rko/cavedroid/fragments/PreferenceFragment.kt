package com.cyb3rko.cavedroid.fragments

import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreference
import com.cyb3rko.cavedroid.*
import com.cyb3rko.cavedroid.ANALYTICS_COLLECTION
import com.cyb3rko.cavedroid.ANNOUNCEMENT_IMAGE
import com.cyb3rko.cavedroid.CRASHLYTICS_COLLECTION
import com.cyb3rko.cavedroid.DATA_DELETION
import com.cyb3rko.cavedroid.NIGHTMODE
import com.cyb3rko.cavedroid.SHARED_PREFERENCE
import com.cyb3rko.cavedroid.SHOW_ANNOUNCEMENTS
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics

class PreferenceFragment : PreferenceFragmentCompat() {
    private lateinit var mySPR: SharedPreferences

    private lateinit var nightModeList: ListPreference
    private lateinit var backgroundImageList: ListPreference
    private lateinit var avatarTypeList: ListPreference
    private lateinit var showAnnouncementSwitch: SwitchPreference
    private lateinit var announcementImageSwitch: SwitchPreference
    private lateinit var analyticsCollectionSwitch: SwitchPreference
    private lateinit var crashlyticsCollectionSwitch: SwitchPreference

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {}

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        addPreferencesFromResource(R.xml.preferences)
        preferenceManager.sharedPreferencesName = SHARED_PREFERENCE
        mySPR = preferenceManager.sharedPreferences!!
        nightModeList = findPreference(NIGHTMODE)!!
        backgroundImageList = findPreference(BACKGROUND_IMAGE)!!
        avatarTypeList = findPreference(AVATAR_TYPE)!!
        showAnnouncementSwitch = findPreference(SHOW_ANNOUNCEMENTS)!!
        announcementImageSwitch = findPreference(ANNOUNCEMENT_IMAGE)!!
        analyticsCollectionSwitch = findPreference(ANALYTICS_COLLECTION)!!
        crashlyticsCollectionSwitch = findPreference(CRASHLYTICS_COLLECTION)!!

        nightModeList.value = mySPR.getString(NIGHTMODE, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM.toString())
        backgroundImageList.value = mySPR.getString(BACKGROUND_IMAGE, "0")
        avatarTypeList.value = mySPR.getString(AVATAR_TYPE, "avatar")
        showAnnouncementSwitch.isChecked = mySPR.getBoolean(SHOW_ANNOUNCEMENTS, true)
        announcementImageSwitch.isChecked = mySPR.getBoolean(ANNOUNCEMENT_IMAGE, true)
        analyticsCollectionSwitch.isChecked = mySPR.getBoolean(ANALYTICS_COLLECTION, true)
        crashlyticsCollectionSwitch.isChecked = mySPR.getBoolean(CRASHLYTICS_COLLECTION, true)
    }

    override fun onPreferenceTreeClick(preference: Preference): Boolean {
        return when (preference.key) {
            NIGHTMODE -> {
                nightModeList.setOnPreferenceChangeListener { _, newValue ->
                    AppCompatDelegate.setDefaultNightMode(newValue.toString().toInt())
                    true
                }
                true
            }
            BACKGROUND_IMAGE -> {
                backgroundImageList.setOnPreferenceChangeListener { _, newValue ->
                    val value = newValue.toString().toInt()
                    val newThemeId = when (value) {
                        -1, 0-> R.style.Theme_Cavedroid_Standard
                        1 -> R.style.Theme_Cavedroid_Green
                        else -> R.style.Theme_Cavedroid_Standard
                    }
                    val editor = mySPR.edit()
                    editor.putString(THEME, newThemeId.toString()).commit()
                    requireActivity().recreate()
                    true
                }
                true
            }
            ANALYTICS_COLLECTION -> {
                FirebaseAnalytics.getInstance(requireContext())
                    .setAnalyticsCollectionEnabled(analyticsCollectionSwitch.isChecked)
                true
            }
            CRASHLYTICS_COLLECTION -> {
                FirebaseCrashlytics.getInstance()
                    .setCrashlyticsCollectionEnabled(crashlyticsCollectionSwitch.isChecked)
                true
            }
            DATA_DELETION -> {
                FirebaseAnalytics.getInstance(requireActivity()).resetAnalyticsData()
                FirebaseCrashlytics.getInstance().deleteUnsentReports()
                showToast(getString(R.string.preferences_deletion_done))
                true
            }
            else -> false
        }
    }
}
