package com.cyb3rko.cavedroid.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.text.Html
import android.text.SpannableString
import android.text.Spanned
import android.text.style.UnderlineSpan
import android.util.Log
import android.view.*
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.SearchView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.forEach
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.cyb3rko.cavedroid.*
import com.cyb3rko.cavedroid.appintro.MyAppIntro
import com.cyb3rko.cavedroid.databinding.FragmentHomeBinding
import com.cyb3rko.cavedroid.webviews.HtmlWebView
import com.cyb3rko.cavetaleapi.CavetaleAPI
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.coroutines.*

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private lateinit var myContext: Context

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    private val api = CavetaleAPI()
    private val args: HomeFragmentArgs by navArgs()
    private var currentName = ""
    private lateinit var topMenu: Menu
    private lateinit var mySPR: SharedPreferences
    private lateinit var mySPREditor: SharedPreferences.Editor

    private var amountItemsSold = 0
    private var amountItemsBought = 0
    private var amountOffers = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        myContext = requireContext()
        mySPR = requireActivity().getSharedPreferences(SHARED_PREFERENCE, Context.MODE_PRIVATE)
        mySPREditor = mySPR.edit()

        val drawableId = Utils.getBackgroundDrawableId(resources, mySPR)
        if (drawableId != -1) {
            view.background = ResourcesCompat.getDrawable(resources, drawableId, myContext.theme)
        }

        currentName = mySPR.getString(NAME, "")!!
        if (currentName.isNotBlank() && args.name.isBlank()) {
            loadProfile(currentName)
        } else if (currentName.isNotBlank() && args.name.isNotBlank()) {
            if (args.name != currentName) {
                currentName = args.name
                lifecycleScope.launch(Dispatchers.Main) {
                    while (!this@HomeFragment::topMenu.isInitialized)
                    topMenu.forEach {
                        it.isVisible = false
                    }
                    val searchView = topMenu.findItem(R.id.search)
                    searchView.expandActionView()
                    (searchView.actionView as SearchView).setQuery(args.name, true)
                }
            } else {
                currentName = args.name
                loadProfile(currentName)
            }
        } else {
            showNameDialog(false)
        }

        binding.refreshLayout.apply {
            setProgressBackgroundColorSchemeResource(R.color.refreshLayoutBackground)
            setColorSchemeResources(R.color.refreshLayoutArrow)
            setOnRefreshListener {
                isRefreshing = false
                loadProfile(mySPR.getString(NAME, "")!!)
            }
        }

        binding.soldContainer.setOnClickListener {
            val action = HomeFragmentDirections.openProfileCategory(
                1,
                currentName,
                amountItemsSold,
                getString(R.string.topbar_title_items_sold)
            )
            findNavController().navigate(action)
        }

        binding.boughtContainer.setOnClickListener {
            val action = HomeFragmentDirections.openProfileCategory(
                2,
                currentName,
                amountItemsBought,
                getString(R.string.topbar_title_items_bought)
            )
            findNavController().navigate(action)
        }

        binding.offersContainer.setOnClickListener {
            val action = HomeFragmentDirections.openProfileCategory(
                3,
                currentName,
                amountOffers,
                getString(R.string.topbar_title_offers)
            )
            findNavController().navigate(action)
        }

        binding.header.setOnClickListener {
            loadNameHistory()
        }
    }

    private fun loadNameHistory() {
        val progressDialog = getProgressDialog()
        progressDialog.show()

        var viewType = 0
        val webView = HtmlWebView(myContext)
        webView.webViewClient = object: WebViewClient() {
            @SuppressLint("CheckResult")
            override fun onPageFinished(view: WebView?, url: String?) {
                webView.fetchHmtl()

                if (viewType == 0) {
                    lifecycleScope.launch {
                        try {
                            while (webView.javascriptInterface.html == null) {
                                delay(50)
                            }

                            val uuid = api.getNameUuid(webView.javascriptInterface.html!!)
                            webView.javascriptInterface.clearHTML()
                            webView.loadUrl(api.getNameHistoryLink(uuid))
                            viewType = 1
                        } catch (e: Exception) {
                            Log.e("Cavedroid.NameHistory", e.message!!)
                        }
                    }
                } else {
                    lifecycleScope.launch {
                        try {
                            while (webView.javascriptInterface.html == null) {
                                delay(50)
                            }

                            progressDialog.cancel()
                            val list = api.getNameHistory(webView.javascriptInterface.html!!)
                            MaterialAlertDialogBuilder(myContext, R.style.Dialog)
                                .setTitle(R.string.name_history_title)
                                .setItems(list.toTypedArray()) { _, id ->
                                    Utils.storeToClipboard(myContext, list[id])
                                    Utils.showClipboardToast(myContext, getString(R.string.clipboard_category_name))
                                }
                                .show()
                        } catch (e: Exception) {
                            Log.e("Cavedroid.NameHistory", e.message!!)
                        }
                    }
                }
            }
        }
        webView.loadUrl(api.getNameUuidLink(currentName))
    }

    private fun getProgressDialog(): AlertDialog {
        return MaterialAlertDialogBuilder(myContext, R.style.Dialog)
            .setView(R.layout.dialog_view_progress)
            .show()
    }

    private fun loadProfile(name: String) {
        showInformation(false)
        showAnimation(true, true)
        val formattedName = if (!name.contains(" ")) {
            name
        } else {
            name.replace(" ", "%20")
        }
        val avatarName = if (name != "The Bank") name else "God"
        lifecycleScope.launch {
            try {
                val userJob = async(Dispatchers.IO) { api.getUser(formattedName) }
                val user = userJob.await()

                if (user == null) {
                    showAnimation(true, false)
                    return@launch
                }

                withContext(Dispatchers.Main) {
                    Glide.with(this@HomeFragment)
                        .load(Utils.getAvatarLink(mySPR, api, avatarName, 500))
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .listener(object: RequestListener<Drawable> {
                            override fun onLoadFailed(
                                e: GlideException?,
                                model: Any?,
                                target: Target<Drawable>?,
                                isFirstResource: Boolean
                            ): Boolean {
                                Log.e("Cavedroid.Avatar", e?.message!!)
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
                        .into(binding.avatarView)

                    showAnimation(false, true)
                    amountItemsSold = user.itemsSold.toInt()
                    amountItemsBought = user.itemsBought.toInt()
                    amountOffers = user.currentOffers.toInt()
                    binding.apply {
                        nameView.visibility = View.VISIBLE
                        nameView.text = name
                        balanceView.text = getFormattedInformation(
                            getString(R.string.home_balance_caption),
                            user.balance
                        )
                        earningsView.text = getFormattedInformation(
                            getString(R.string.home_earnings_caption),
                            user.marketEarnings
                        )
                        spendingView.text = getFormattedInformation(
                            getString(R.string.home_spendings_caption),
                            user.marketSpendings
                        )
                        soldView.text = getFormattedInformation(
                            getString(R.string.home_sold_caption),
                            user.itemsSold
                        )
                        boughtView.text = getFormattedInformation(
                            getString(R.string.home_bought_caption),
                            user.itemsBought
                        )
                        offersView.text = getFormattedInformation(
                            getString(R.string.home_offers_caption),
                            user.currentOffers
                        )
                    }
                    showInformation(true)
                }
            } catch (e: Exception) {
                Log.e("Cavedroid.Data", "${e.cause}, ${e.message}")
                showAnimation(true, false)
            }
        }
    }

    private fun getFormattedInformation(category: String, value: String): Spanned {
        return Html.fromHtml("<b>$category</b><br/>$value")
    }

    private fun showInformation(show: Boolean) {
        val visibility = if (show) View.VISIBLE else View.INVISIBLE
        binding.apply {
            header.visibility = visibility
            balanceContainer.visibility = visibility
            earningsContainer.visibility = visibility
            spendingContainer.visibility = visibility
            soldContainer.visibility = visibility
            boughtContainer.visibility = visibility
            offersContainer.visibility = visibility
        }
    }

    private fun showAnimation(show: Boolean, connected: Boolean) {
        val viewVisibility = if (show) View.VISIBLE else View.INVISIBLE
        val infoVisibility = if (show && !connected) View.VISIBLE else View.INVISIBLE
        val newSpeed = if (!connected) 1.2f else 3f
        val animation = if (connected) "coin-spin.json" else "no-connection.json"
        binding.apply {
            animationView.apply {
                setAnimation(animation)
                speed = newSpeed
                visibility = viewVisibility
                playAnimation()
            }
            animationInfo.visibility = infoVisibility
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.topbar_menu, menu)
        topMenu = menu
        val menuSearchItem = menu.findItem(R.id.search)
        val searchView = menuSearchItem.actionView as SearchView
        searchView.queryHint = getString(R.string.search_view_hint)
        searchView.isIconifiedByDefault = false
        val id = searchView.context.resources.getIdentifier(
            "android:id/search_src_text",
            null,
            null
        )
        val textview = searchView.findViewById<AutoCompleteTextView>(id)
        textview.setTextColor(resources.getColor(R.color.colorSearchView))
        textview.setHintTextColor(resources.getColor(R.color.colorSearchViewHint))
        searchView.setOnQueryTextListener(object: SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query != null) {
                    val formattedQuery = query.trim()
                    if (formattedQuery == "The Bank" ||
                        (Regex("[a-zA-Z0-9_]+").matches(query) && formattedQuery.length < 17)
                    ) {
                        Utils.hideKeyboard(requireActivity())
                        binding.animationView.playAnimation()
                        binding.animationView.visibility = View.VISIBLE
                        showInformation(false)
                        loadProfile(formattedQuery)
                        currentName = formattedQuery
                        if (formattedQuery.isNotBlank()) {
                            FirebaseAnalytics.getInstance(myContext)
                                .logEvent("player_search") {
                                param("player", formattedQuery)
                            }
                        }
                        return true
                    } else {
                        textview.error = "Invalid Name"
                        return false
                    }
                } else {
                    return false
                }
            }
            override fun onQueryTextChange(newText: String?): Boolean { return false }
        })

        menuSearchItem.setOnMenuItemClickListener {
            searchView.isIconified = false
            searchView.requestFocusFromTouch()
        }

        menuSearchItem.setOnActionExpandListener(object: MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(item: MenuItem?): Boolean {
                menu.forEach {
                    it.isVisible = false
                }
                return true
            }

            override fun onMenuItemActionCollapse(item: MenuItem?): Boolean {
                Utils.hideKeyboard(requireActivity())
                menu.forEach {
                    it.isVisible = true
                }
                binding.animationView.playAnimation()
                binding.animationView.visibility = View.VISIBLE
                showInformation(false)
                val oldName = mySPR.getString(NAME, "")!!
                loadProfile(oldName)
                currentName = oldName
                return true
            }
        })

        menu.findItem(R.id.profile_name_dialog).setOnMenuItemClickListener {
            showNameDialog(true)
            true
        }

        menu.findItem(R.id.recent_announcement).setOnMenuItemClickListener {
            val progressDialog = getProgressDialog()
            progressDialog.show()
            (requireActivity() as MainActivity).receiveLatestAnnouncement(true, progressDialog)
            true
        }

        menu.findItem(R.id.about).setOnMenuItemClickListener {
            findNavController().navigate(R.id.navigation_about)
            true
        }

        menu.findItem(R.id.feedback).setOnMenuItemClickListener {
            MaterialAlertDialogBuilder(myContext, R.style.Dialog)
                .setTitle(R.string.feedback_dialog_title)
                .setMessage(R.string.feedback_dialog_message)
                .setPositiveButton(R.string.feedback_dialog_button) { _, _ ->
                    startActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("https://github.com/cyb3rko/cavedroid/issues")
                        )
                    )
                }
                .show()
            true
        }

        menu.findItem(R.id.end_user_consent).setOnMenuItemClickListener {
            showUserConsentDialog()
            true
        }
    }

    private fun showNameDialog(cancelable: Boolean) {
        @SuppressLint("InflateParams")
        val inputField = layoutInflater.inflate(R.layout.dialog_view_name, null)
            .findViewById<TextInputLayout>(R.id.md_input)

        @SuppressLint("InflateParams")
        val inputTextField = inputField.findViewById<TextInputEditText>(R.id.md_input_text)

        MaterialAlertDialogBuilder(myContext, R.style.Dialog)
            .setView(inputField)
            .setCancelable(cancelable)
            .setTitle(R.string.name_dialog_title)
            .setPositiveButton(android.R.string.ok, null)
            .create().apply {
                setOnShowListener {
                    getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                        val newName = inputTextField.text.toString()
                        if (Regex("[a-zA-Z0-9_]+").matches(newName) && newName.length < 17) {
                            dismiss()
                            mySPREditor.putString(NAME, newName).apply()
                            binding.animationView.playAnimation()
                            binding.animationView.visibility = View.VISIBLE
                            showInformation(false)
                            currentName = newName
                            loadProfile(newName)

                            if (!cancelable) {
                                (requireActivity() as MainActivity).receiveLatestAnnouncement()
                            }
                        } else {
                            inputField.error = getString(R.string.name_dialog_error)
                        }
                    }
                }
                show()
            }
    }

    private fun showUserConsentDialog() {
        var dialogMessage = getString(R.string.end_user_consent_2_message_1)
        dialogMessage +=
            mySPR.getString(CONSENT_DATE, getString(R.string.end_user_consent_2_date_not_found)) +
                    getString(R.string.end_user_consent_2_message_2) +
                    mySPR.getString(CONSENT_TIME, getString(R.string.end_user_consent_2_time_not_found))
        val spannableString = SpannableString(dialogMessage)
        var currentText = getString(R.string.end_user_consent_2_date)
        var index = dialogMessage.indexOf(currentText)
        repeat(2) {
            spannableString.setSpan(
                UnderlineSpan(),
                index,
                index + currentText.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            currentText = getString(R.string.end_user_consent_2_time)
            index = dialogMessage.indexOf(currentText)
        }

        MaterialAlertDialogBuilder(myContext, R.style.Dialog)
            .setView(R.layout.dialog_end_user_consent)
            .setTitle(R.string.end_user_consent_2_title)
            .setMessage(spannableString)
            .setPositiveButton(android.R.string.ok, null)
            .setNegativeButton(R.string.end_user_consent_2_button_2) { _, _ ->
                val analytics = FirebaseAnalytics.getInstance(myContext)
                analytics.resetAnalyticsData()
                analytics.setAnalyticsCollectionEnabled(false)
                val crashlytics = FirebaseCrashlytics.getInstance()
                crashlytics.deleteUnsentReports()
                crashlytics.setCrashlyticsCollectionEnabled(false)
                mySPREditor.clear().commit()
                requireActivity().finish()
                startActivity(Intent(myContext, MyAppIntro::class.java))
            }
            .create().apply {
                setOnShowListener {
                    window?.findViewById<Button>(R.id.privacy_policy_button)?.setOnClickListener {
                        Utils.showLicenseDialog(myContext, PRIVACY_POLICY)
                    }
                    window?.findViewById<Button>(R.id.terms_of_use_button)?.setOnClickListener {
                        Utils.showLicenseDialog(myContext, TERMS_OF_USE)
                    }
                }
                show()
            }
    }
}
