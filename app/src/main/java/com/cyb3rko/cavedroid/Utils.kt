package com.cyb3rko.cavedroid

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.text.Html
import android.text.method.LinkMovementMethod
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.bottomsheets.BottomSheet
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.cyb3rko.cavedroid.skintools.Section
import com.cyb3rko.cavedroid.skintools.extractSection
import com.cyb3rko.cavetaleapi.CavetaleAPI
import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.entity.channel.MessageChannel
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

internal const val PRIVACY_POLICY = "privacy_policy"
internal const val TERMS_OF_USE = "terms_of_use"

internal const val ADAPTIVE_THEMING = "adaptive_theming"
internal const val ANALYTICS_COLLECTION = "analytics_collection"
internal const val ANNOUNCEMENT_IMAGE = "announcement_image"
internal const val AVATAR_TYPE = "avatar_type"
internal const val BACKGROUND_IMAGE = "background_image"
internal const val BACKGROUND_SET = "background_set"
internal const val CONSENT_DATE = "consent_date"
internal const val CONSENT_TIME = "consent_time"
internal const val CRASHLYTICS_COLLECTION = "crashlytics_collection"
internal const val DATA_DELETION = "data_deletion"
internal const val FIRST_START = "first_start"
internal const val LATEST_MESSAGE = "latest_message"
internal const val NAME = "name"
internal const val NIGHTMODE = "nightmode"
internal const val OLD_ANDROID = "old_android"
internal const val SHARED_PREFERENCE = "Safe"
internal const val SHOW_ANNOUNCEMENTS = "show_announcements"
internal const val THEME = "theme"

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

    internal fun showMissingIconsDialog(context: Context, missingIcons: MutableSet<String>, mySPR: SharedPreferences) {
        val message = if (missingIcons.isNotEmpty()) {
            context.getString(R.string.missing_icons_dialog_message1, missingIcons.size.toString())
        } else {
            context.getString(R.string.missing_icons_dialog_message2)
        }
        val name = mySPR.getString(NAME, "Null")
        MaterialDialog(context).show {
            title(R.string.missing_icons_dialog_title)
            message(text = message)
            positiveButton(R.string.missing_icons_dialog_button) {
                if (missingIcons.isEmpty()) return@positiveButton
                GlobalScope.launch {
                    try {
                        val kord = Kord(Secrets().getAPIToken(context.packageName))
                        val channel = kord.getGuild(Snowflake(840366805649457172))?.getChannel(Snowflake(852596801432453170))
                        var reportMessage = context.getString(R.string.missing_icons_discord_message, name, missingIcons.size)
                        missingIcons.sorted().forEach {
                            reportMessage += "\n$it"
                        }
                        (channel as MessageChannel).createMessage(reportMessage)
                        missingIcons.clear()
                    } catch (e: Exception) {
                        Log.e("Cavedroid.Utils", "Reading and reporting missing icons failed: $e, ${e.message}")
                    }
                }
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

    internal fun loadItemIcon(context: Context, imageView: ImageView, item: String, missingIcons: MutableSet<String>) {
        imageView.setImageResource(0)
        if (item.contains("Player Head")) {
            if (item == "Player Head" || item == "Player Head <>") {
                imageView.setImageResource(R.drawable._ic_no_image)
                return
            }
            var playerHeadName = item.split("<")[1].split(">")[0]
            playerHeadName = playerHeadName
                .replace(" ", "_")
                .replace("'", "")
                .replace("(", "")
                .replace(")", "")
                .toLowerCase()

            GlobalScope.launch {
                val resId = context.resources.getIdentifier(playerHeadName, "string", context.packageName)
                if (resId != 0) {
                    val skinId = context.getString(resId)
                    try {
                        val drawable = Glide.with(context)
                            .asBitmap()
                            .load("https://textures.minecraft.net/texture/$skinId")
                            .into(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                            .get()
                        (context as Activity).runOnUiThread {
                            imageView.setImageBitmapOnReady { head(drawable, width, width) }
                        }
                    } catch (e: Exception) {
                        missingIcons.add(item)
                    }
                } else {
                    (context as Activity).runOnUiThread {
                        Glide.with(context)
                            .load(CavetaleAPI().getAvatarLink(playerHeadName, 100))
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .addListener(object: RequestListener<Drawable> {
                                override fun onLoadFailed(
                                    e: GlideException?,
                                    model: Any?,
                                    target: Target<Drawable>?,
                                    isFirstResource: Boolean
                                ): Boolean {
                                    missingIcons.add(item)
                                    return false
                                }

                                override fun onResourceReady(
                                    resource: Drawable?,
                                    model: Any?,
                                    target: Target<Drawable>?,
                                    dataSource: DataSource?,
                                    isFirstResource: Boolean
                                ): Boolean {
                                    return false
                                }
                            })
                            .into(imageView)
                    }
                }
            }

            return
        }
        if (item.contains("pocket", true)) {
            val avatarResId = context.resources.getIdentifier("item_pocket_mob", "drawable", context.packageName)
            imageView.setImageResource(avatarResId)
            return
        }
        val itemName = item.replace(" ", "_").replace("'", "").toLowerCase()
        val formattedName = itemName.split(",")[0]
        val avatarResId = context.resources.getIdentifier("_item_$formattedName", "drawable", context.packageName)
        if (avatarResId != 0) {
            imageView.setImageResource(avatarResId)
        } else {
            missingIcons.add(item)
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

    internal fun loadAvatar(context: Context, api: CavetaleAPI, mySPR: SharedPreferences, imageView: ImageView, name: String, size: Int) {
        Glide.with(context)
            .load(getAvatarLink(mySPR, api, name, size))
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(imageView)
    }

    internal fun getAvatarLink(mySPR: SharedPreferences, api: CavetaleAPI, avatarName: String, size: Int): String {
        return when (mySPR.getString(AVATAR_TYPE, "avatar")) {
            "avatar" -> api.getAvatarLink(avatarName, size)
            "bust" -> api.getBustLink(avatarName, size)
            else -> ""
        }
    }

    internal fun getBackgroundDrawableId(resources: Resources, mySPR: SharedPreferences): Int {
        return when (mySPR.getString(BACKGROUND_IMAGE, "-1")!!.toInt()) {
            0 -> {
                if (isNightModeActive(resources)) {
                    R.drawable.background_forest2
                } else {
                    R.drawable.background_forest1
                }
            }
            1 -> R.drawable.background_house
            else -> -1
        }
    }

    internal fun isNightModeActive(resources: Resources): Boolean {
        when (AppCompatDelegate.getDefaultNightMode()) {
            AppCompatDelegate.MODE_NIGHT_YES -> return true
            AppCompatDelegate.MODE_NIGHT_NO -> return false
        }

        return when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_YES -> true
            Configuration.UI_MODE_NIGHT_NO -> false
            else -> false
        }
    }

    private fun head(skinTexture: Bitmap, width: Int, height: Int): Bitmap {
        val head = skinTexture.extractSection(Section.Head())
        val headOverlay = skinTexture.extractSection(Section.HeadOverlay())
        Canvas(head).drawBitmap(headOverlay, 0f, 0f, null)
        return Bitmap.createScaledBitmap(head, width, height, false)
            .also { head.recycle() }
    }

    private fun ImageView.setImageBitmapOnReady(func: ImageView.() -> Bitmap) {
        post { setImageBitmap(func()) }
    }
}