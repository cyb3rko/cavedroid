package com.cyb3rko.cavedroid

import android.content.SharedPreferences
import android.os.Bundle
import android.text.Html
import android.util.Log
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
import com.cyb3rko.cavedroid.databinding.ActivityMainBinding
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

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        receiveLatestAnnouncement()
    }

    private fun getAPIToken() = Secrets().getAPIToken(packageName)

    fun receiveLatestAnnouncement() {
        val sharedPreferences = getSharedPreferences("Safe", MODE_PRIVATE)
        if (!sharedPreferences.getBoolean(SHOW_ANNOUNCEMENTS, true)) return
        if (sharedPreferences.getString("name", "") == "") return

        GlobalScope.launch {
            try {
                val token = getAPIToken()
                if (token != "0") {
                    val kord = Kord(token)
                    val guild = kord.getGuild(Snowflake(195206438623248384))!!
                    val messageObject = (guild.getChannel(Snowflake(265060069194858496)) as MessageChannel).getLastMessage()!!

                    if (messageObject.id.value != sharedPreferences.getLong("latest_message", 0)) {
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

        while (message.contains("@!")) {
            val substrings = message.split("@!", limit = 2)
            val id = substrings[1].substring(0..17)
            val substring2 = substrings[1].substring(startIndex = 18)
            val name = guild.getMember(Snowflake(id)).displayName
            message = "${substrings[0]}$name $substring2"
        }

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

        message = message.replace("&%", "#")

        runOnUiThread {
            MaterialDialog(this@MainActivity, BottomSheet(LayoutMode.MATCH_PARENT)).show {
                customView(R.layout.announcement_dialog)
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

        sharedPreferences.edit().putLong("latest_message", messageObject.id.value).apply()
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