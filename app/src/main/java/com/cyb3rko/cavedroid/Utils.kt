package com.cyb3rko.cavedroid

import android.content.Context
import android.text.Html
import android.text.method.LinkMovementMethod
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.bottomsheets.BottomSheet

internal const val PRIVACY_POLICY = "privacy_policy"
internal const val TERMS_OF_USE = "terms_of_use"

internal const val ANALYTICS_COLLECTION = "analytics_collection"
internal const val ANNOUNCEMENT_IMAGE = "announcement_image"
internal const val CONSENT_DATE = "consent_date"
internal const val CONSENT_TIME = "consent_time"
internal const val CRASHLYTICS_COLLECTION = "crashlytics_collection"
internal const val DATA_DELETION = "data_deletion"
internal const val FIRST_START = "first_start"
internal const val NIGHTMODE = "nightmode"
internal const val SHARED_PREFERENCE = "Safe"
internal const val SHOW_ANNOUNCEMENTS = "show_announcements"

object Utils {
    internal fun showLicenseDialog(context: Context?, type: String) {
        MaterialDialog(context!!, BottomSheet()).show {
            @Suppress("DEPRECATION")
            message(0, Html.fromHtml(context.assets.open("$type.html").bufferedReader().use { it.readText() })) {
                messageTextView.movementMethod = LinkMovementMethod.getInstance()
            }
        }
    }
}