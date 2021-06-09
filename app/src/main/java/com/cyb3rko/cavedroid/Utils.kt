package com.cyb3rko.cavedroid

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.text.Html
import android.text.method.LinkMovementMethod
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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
internal const val LATEST_MESSAGE = "latest_message"
internal const val NAME = "name"
internal const val NIGHTMODE = "nightmode"
internal const val SHARED_PREFERENCE = "Safe"
internal const val SHOW_ANNOUNCEMENTS = "show_announcements"

object Utils {
    internal fun showToast(context: Context, message: String, length: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(context, message, length).show()
    }

    internal fun showClipboardToast(context: Context, content: String) = showToast(context, "Copied $content to clipboard")

    internal fun storeToClipboard(context: Context, label: String, text: String = label) {
        val clip = ClipData.newPlainText(label, text)
        (context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager).setPrimaryClip(clip)
    }

    internal fun showLicenseDialog(context: Context?, type: String) {
        MaterialDialog(context!!, BottomSheet()).show {
            @Suppress("DEPRECATION")
            message(0, Html.fromHtml(context.assets.open("$type.html").bufferedReader().use { it.readText() })) {
                messageTextView.movementMethod = LinkMovementMethod.getInstance()
            }
        }
    }

    internal fun getFormattedDialogInformation(category: String, value: String, lineBreak: Boolean = true): String {
        var information = "<b>$category</b>: $value"
        if (lineBreak) {
            information += "<br/>"
        }
        return information
    }

    internal fun getFormattedDialogPriceInformation(category: String, value: String, lineBreak: Boolean = true): String {
        return getFormattedDialogInformation(category, "$value Coins", lineBreak)
    }

    internal fun loadItemIcon(context: Context, imageView: ImageView, item: String) {
        val itemName = item.replace(" ", "_").toLowerCase()
        val formattedName = itemName.split(",")[0]
        val avatarResId = context.resources.getIdentifier("_item_$formattedName", "drawable", context.packageName)
        if (avatarResId != 0) {
            imageView.setImageResource(avatarResId)
        } else {
            imageView.setImageResource(0)
        }
    }

    internal fun hideKeyboard(activity: Activity) {
        val imm = activity.getSystemService(AppCompatActivity.INPUT_METHOD_SERVICE) as InputMethodManager
        var view = activity.currentFocus
        if (view == null) {
            view = View(activity)
        }
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }
}