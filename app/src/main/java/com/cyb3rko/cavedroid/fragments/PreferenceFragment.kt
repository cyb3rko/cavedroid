package com.cyb3rko.cavedroid.fragments

import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Toast
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
    private lateinit var showAnnouncementSwitch: SwitchPreference
    private lateinit var announcementImageSwitch: SwitchPreference
    private lateinit var analyticsCollectionSwitch: SwitchPreference
    private lateinit var crashlyticsCollectionSwitch: SwitchPreference

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {}

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        addPreferencesFromResource(R.xml.preferences)
        preferenceManager.sharedPreferencesName = SHARED_PREFERENCE
        mySPR = preferenceManager.sharedPreferences
        nightModeList = findPreference(NIGHTMODE)!!
        showAnnouncementSwitch = findPreference(SHOW_ANNOUNCEMENTS)!!
        announcementImageSwitch = findPreference(ANNOUNCEMENT_IMAGE)!!
        analyticsCollectionSwitch = findPreference(ANALYTICS_COLLECTION)!!
        crashlyticsCollectionSwitch = findPreference(CRASHLYTICS_COLLECTION)!!

        nightModeList.value = mySPR.getString(NIGHTMODE, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM.toString())
        showAnnouncementSwitch.isChecked = mySPR.getBoolean(SHOW_ANNOUNCEMENTS, true)
        announcementImageSwitch.isChecked = mySPR.getBoolean(ANNOUNCEMENT_IMAGE, true)
        analyticsCollectionSwitch.isChecked = mySPR.getBoolean(ANALYTICS_COLLECTION, true)
        crashlyticsCollectionSwitch.isChecked = mySPR.getBoolean(CRASHLYTICS_COLLECTION, true)
    }

    override fun onPreferenceTreeClick(preference: Preference?): Boolean {
        return when (preference?.key) {
            NIGHTMODE -> {
                nightModeList.setOnPreferenceChangeListener { _, newValue ->
                    AppCompatDelegate.setDefaultNightMode(newValue.toString().toInt())
                    true
                }
                true
            }
            ANALYTICS_COLLECTION -> {
                FirebaseAnalytics.getInstance(requireContext()).setAnalyticsCollectionEnabled(analyticsCollectionSwitch.isChecked)
                true
            }
            CRASHLYTICS_COLLECTION -> {
                FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(crashlyticsCollectionSwitch.isChecked)
                true
            }
            DATA_DELETION -> {
                FirebaseAnalytics.getInstance(requireActivity()).resetAnalyticsData()
                FirebaseCrashlytics.getInstance().deleteUnsentReports()
                Toast.makeText(requireContext(), getString(R.string.preferences_deletion_done), Toast.LENGTH_SHORT).show()
                true
            }
            else -> false
        }
    }
}