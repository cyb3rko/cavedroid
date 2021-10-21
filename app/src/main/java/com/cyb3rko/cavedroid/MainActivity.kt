package com.cyb3rko.cavedroid

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.util.TypedValue
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.afollestad.materialdialogs.LayoutMode
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.bottomsheets.BottomSheet
import com.afollestad.materialdialogs.callbacks.onPreShow
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.cyb3rko.cavedroid.appintro.MyAppIntro
import com.cyb3rko.cavedroid.databinding.ActivityMainBinding
import com.cyb3rko.cavedroid.fragments.ProfileCategoryFragment
import com.github.rjeschke.txtmark.Processor
import com.google.android.material.bottomnavigation.BottomNavigationView
import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.entity.Guild
import dev.kord.core.entity.Message
import dev.kord.core.entity.channel.MessageChannel
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private lateinit var mySPR: SharedPreferences
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mySPR = getSharedPreferences(SHARED_PREFERENCE, MODE_PRIVATE)
        if (mySPR.getBoolean(FIRST_START, true) || mySPR.getString(CONSENT_DATE, "")!!.isEmpty()) {
            finish()
            startActivity(Intent(applicationContext, MyAppIntro::class.java))
            return
        }

        if (mySPR.getBoolean(ADAPTIVE_THEMING, false)) {
            setTheme(mySPR.getString(THEME, R.style.Theme_Cavedroid_Standard.toString())!!.toInt())
        } else {
            setTheme(R.style.Theme_Cavedroid_Standard)
        }

        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView

        navController = findNavController(R.id.nav_host_fragment_activity_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_notifications, R.id.navigation_rankings
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        when (intent.action) {
            "com.cyb3rko.cavedroid.searchshortcut" -> navController.navigate(R.id.navigation_notifications)
            "com.cyb3rko.cavedroid.rankingsshortcut" -> navController.navigate(R.id.navigation_rankings)
        }
    }

    override fun onStart() {
        super.onStart()
        if (mySPR.getBoolean(ADAPTIVE_THEMING, true)) {
            setTheme(mySPR.getString(THEME, R.style.Theme_Cavedroid_Standard.toString())!!.toInt())
            val typedValue = TypedValue()
            theme.resolveAttribute(R.attr.colorPrimary, typedValue, true)
            supportActionBar?.setBackgroundDrawable(ColorDrawable(typedValue.data))
            if (mySPR.getString(THEME, R.style.Theme_Cavedroid_Standard.toString())!!.toInt() !=
                R.style.Theme_Cavedroid_Standard) {
                if (!Utils.isNightModeActive(resources)) {
                    theme.resolveAttribute(R.attr.colorSecondaryVariant, typedValue, true)
                    binding.navView.setBackgroundColor(typedValue.data)
                }
            }
        }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        receiveLatestAnnouncement()
    }

    private fun getAPIToken() = Secrets().getAPIToken(packageName)

    fun receiveLatestAnnouncement(force: Boolean = false) {
        val sharedPreferences = getSharedPreferences(SHARED_PREFERENCE, MODE_PRIVATE)
        if (sharedPreferences.getBoolean(OLD_ANDROID, false)) return
        if (!force && !sharedPreferences.getBoolean(SHOW_ANNOUNCEMENTS, true)) return
        if (sharedPreferences.getString(NAME, "")!!.isBlank()) return

        GlobalScope.launch {
            try {
                val token = getAPIToken()
                if (token != "0") {
                    val kord = Kord(token)
                    val guild = kord.getGuild(Snowflake(195206438623248384))!!
                    val messageObject = (guild.getChannel(Snowflake(265060069194858496)) as MessageChannel).getLastMessage()!!

                    if (force || messageObject.id.value != sharedPreferences.getLong(LATEST_MESSAGE, 0)) {
                        showAnnouncementDialog(guild, messageObject, sharedPreferences)
                    }
                }
            } catch (e: Exception) {
                Log.e("Cavedroid.MainActivity", "Reading and showing announcement failed: $e, ${e.message}")
            }
        }

    }

    private suspend fun showAnnouncementDialog(guild: Guild, messageObject: Message, sharedPreferences: SharedPreferences) {
        var message = messageObject.content
            .replace("<", "")
            .replace(">", "")
            .replace("\n", "<br/>")

        // Replace user ids with server nick names
        while (message.contains("@!")) {
            val substrings = message.split("@!", limit = 2)
            val id = substrings[1].substring(0..17)
            val substring2 = substrings[1].substring(startIndex = 18)
            val name = guild.getMember(Snowflake(id)).displayName
            message = "${substrings[0]}$name $substring2"
        }

        // Replace channel ids with channel names
        while (message.contains(" #")) {
            try {
                val substrings = message.split("#", limit = 2)
                val id = substrings[1].substring(0..17)
                val channel = guild.getChannelOrNull(Snowflake(id))
                val name = channel?.name ?: "deleted-channel"
                message = "${substrings[0]}&%$name${substrings[1].drop(18)}"
            } catch (e: Exception) {
                message = message.replaceFirst("#", "&%")
                Log.e("Cavedroid.MainActivity", "Reading and showing announcement failed: $e, ${e.message}")
            }
        }

        // Remove time left until event
        while (message.contains("(t:")) {
            val index = message.indexOf("(t:")
            val endIndex = message.indexOf(":R)", index + 12)
            message = message.substring(0 until index - 1) + message.substring(endIndex + 3)
        }

        // Replace timestamp with formatted date and time
        while (message.contains("t:")) {
            val index = message.indexOf("t:")
            val endIndex = message.indexOf(":F", index + 12)
            val time = message.substring(index + 2 until endIndex).toLong() * 1000
            val date = Date(time)
            @SuppressLint("SimpleDateFormat")
            val formattedDate = SimpleDateFormat("MM/dd/yyyy - HH:mm 'UTC'").format(date)
            message = message.substring(0 until index) + formattedDate + message.substring(endIndex + 2)
        }

        message = message.replace("&%", "#")

        runOnUiThread {
            MaterialDialog(this@MainActivity, BottomSheet(LayoutMode.MATCH_PARENT)).show {
                customView(viewRes = R.layout.announcement_dialog, scrollable = true, noVerticalPadding = true)
                onPreShow {
                    val view = it.getCustomView()
                    if (messageObject.attachments.isNotEmpty()) {
                        val drawable = messageObject.attachments.toList()[0]
                        if (sharedPreferences.getBoolean(ANNOUNCEMENT_IMAGE, true) && drawable.isImage) {
                            Glide.with(applicationContext)
                                .load(drawable.url)
                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                .into(view.findViewById(R.id.image))
                        }
                    }
                    view.findViewById<TextView>(R.id.message).text = Html.fromHtml(Processor.process(message))
                }
            }
        }

        sharedPreferences.edit().putLong(LATEST_MESSAGE, messageObject.id.value).apply()
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    override fun onBackPressed() {
        val hostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_activity_main)
        val fragment = hostFragment?.childFragmentManager?.fragments?.get(0)
        if (fragment is ProfileCategoryFragment) {
            fragment.returnToHome()
        } else {
            super.onBackPressed()
        }
    }
}