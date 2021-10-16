package com.cyb3rko.cavedroid.fragments

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.*
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.text.Html
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.UnderlineSpan
import android.util.Log
import android.view.*
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.EditText
import android.widget.SearchView
import androidx.core.view.forEach
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.callbacks.onPreShow
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.afollestad.materialdialogs.list.listItems
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.cyb3rko.cavedroid.*
import com.cyb3rko.cavedroid.appintro.MyAppIntro
import com.cyb3rko.cavedroid.databinding.FragmentHomeBinding
import com.cyb3rko.cavetaleapi.CavetaleAPI
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root = binding.root
        myContext = requireContext()

        mySPR = requireActivity().getSharedPreferences(SHARED_PREFERENCE, Context.MODE_PRIVATE)
        mySPREditor = mySPR.edit()

        currentName = mySPR.getString(NAME, "")!!
        if (currentName.isNotBlank() && args.name.isBlank()) {
            loadProfile(currentName)
        } else if (currentName.isNotBlank() && args.name.isNotBlank()) {
            if (args.name != currentName) {
                currentName = args.name
                GlobalScope.launch {
                    requireActivity().runOnUiThread {
                        while (!this@HomeFragment::topMenu.isInitialized);
                        topMenu.forEach {
                            it.isVisible = false
                        }
                        val searchView = topMenu.findItem(R.id.search)
                        searchView.expandActionView()
                        (searchView.actionView as SearchView).setQuery(args.name, true)
                    }
                }
            } else {
                currentName = args.name
                loadProfile(currentName)
            }
        } else {
            showNameDialog(false)
        }

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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
        val progressDialog = ProgressDialog(myContext)
        progressDialog.setMessage(getString(R.string.name_history_fetching))
        progressDialog.show()

        var viewType = 0
        val webView = HtmlWebView(myContext)
        webView.webViewClient = object: WebViewClient() {
            @SuppressLint("CheckResult")
            override fun onPageFinished(view: WebView?, url: String?) {
                webView.fetchHmtl()

                if (viewType == 0) {
                    GlobalScope.launch {
                        try {
                            while (webView.javascriptInterface.html == null) {
                                Thread.sleep(50)
                            }

                            val uuid = api.getNameUuid(webView.javascriptInterface.html!!)
                            webView.javascriptInterface.clearHTML()
                            requireActivity().runOnUiThread {
                                webView.loadUrl(api.getNameHistoryLink(uuid))
                            }
                            viewType = 1
                        } catch (e: Exception) {
                            Log.e("Cavedroid.NameHistory", e.message!!)
                        }
                    }
                } else {
                    GlobalScope.launch {
                        try {
                            while (webView.javascriptInterface.html == null) {
                                Thread.sleep(50)
                            }

                            requireActivity().runOnUiThread {
                                progressDialog.dismiss()
                                MaterialDialog(myContext).show {
                                    noAutoDismiss()
                                    title(R.string.name_history_title)
                                    listItems(items = api.getNameHistory(webView.javascriptInterface.html!!)) { _, _, name ->
                                        Utils.storeToClipboard(myContext, name.toString())
                                        Utils.showClipboardToast(myContext, getString(R.string.clipboard_category_name))
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            Log.e("Cavedroid.NameHistory", e.message!!)
                        }
                    }
                }
            }
        }
        webView.loadUrl(api.getNameUuidLink(currentName))
    }

    private fun loadProfile(name: String) {
        showInformation(false)
        showAnimation(true, true)
        val formattedName = if (!name.contains(" ")) name else name.replace(" ", "%20")
        val avatarName = if (name != "The Bank") name else "God"
        GlobalScope.launch {
            try {
                val user = api.getUser(formattedName)
                requireActivity().runOnUiThread {
                    Glide.with(this@HomeFragment)
                        .load(Utils.getAvatarLink(mySPR, api, avatarName, 500))
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .listener(object: RequestListener<Drawable> {
                            override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
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
                                showAnimation(false, true)
                                amountItemsSold = user.itemsSold.toInt()
                                amountItemsBought = user.itemsBought.toInt()
                                amountOffers = user.currentOffers.toInt()
                                binding.apply {
                                    nameView.visibility = View.VISIBLE
                                    nameView.text = name
                                    balanceView.text = getFormattedInformation(getString(R.string.home_balance_caption), user.balance)
                                    earningsView.text = getFormattedInformation(getString(R.string.home_earnings_caption), user.marketEarnings)
                                    spendingView.text = getFormattedInformation(getString(R.string.home_spendings_caption), user.marketSpendings)
                                    soldView.text = getFormattedInformation(getString(R.string.home_sold_caption), user.itemsSold)
                                    boughtView.text = getFormattedInformation(getString(R.string.home_bought_caption), user.itemsBought)
                                    offersView.text = getFormattedInformation(getString(R.string.home_offers_caption), user.currentOffers)
                                }
                                showInformation(true)
                                return false
                            }
                        })
                        .into(binding.avatarView)
                }
            } catch (e: Exception) {
                Log.e("Cavedroid.Data", "${e.cause}, ${e.message!!}")
                requireActivity().runOnUiThread {
                    showAnimation(true, false)
                }
            }
        }
    }

    private fun getFormattedInformation(category: String, value: String) = Html.fromHtml("<b>$category</b><br/>$value")

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
        searchView.setOnQueryTextListener(object: SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                Utils.hideKeyboard(requireActivity())
                binding.animationView.playAnimation()
                binding.animationView.visibility = View.VISIBLE
                showInformation(false)
                val formattedQuery = query?.trim()
                if (formattedQuery != null) {
                    loadProfile(formattedQuery)
                    currentName = formattedQuery
                }
                if (formattedQuery?.isNotBlank() == true) {
                    FirebaseAnalytics.getInstance(myContext).logEvent("player_search") {
                        param("player", formattedQuery)
                    }
                }

                return true
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
            (requireActivity() as MainActivity).receiveLatestAnnouncement(true)
            true
        }

        menu.findItem(R.id.about).setOnMenuItemClickListener {
            findNavController().navigate(R.id.navigation_about)
            true
        }

        menu.findItem(R.id.feedback).setOnMenuItemClickListener {
            MaterialDialog(requireActivity())
                .title(R.string.feedback_dialog_title)
                .message(R.string.feedback_dialog_message)
                .positiveButton(R.string.feedback_dialog_button) {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/cyb3rko/cavedroid/issues")))
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
        MaterialDialog(requireActivity())
            .noAutoDismiss()
            .onPreShow {
                val input = it.getCustomView().findViewById<EditText>(R.id.md_input)
                input.setText(mySPR.getString(NAME, ""))
            }
            .customView(R.layout.dialog_view)
            .cancelable(cancelable)
            .title(R.string.name_dialog_title)
            .positiveButton(R.string.name_dialog_button) {
                val input = it.getCustomView().findViewById<EditText>(R.id.md_input)
                val newName = input.text.toString().trim()
                if (newName.isNotEmpty()) {
                    mySPREditor.putString(NAME, newName).apply()
                    binding.animationView.playAnimation()
                    binding.animationView.visibility = View.VISIBLE
                    showInformation(false)
                    currentName = newName
                    loadProfile(newName)
                    it.cancel()
                    if (!it.cancelable) (requireActivity() as MainActivity).receiveLatestAnnouncement()
                } else {
                    input.error = getString(R.string.name_dialog_error)
                }
            }
            .show()
    }

    private fun showUserConsentDialog() {
        var dialogMessage = getString(R.string.end_user_consent_2_message_1)
        dialogMessage += mySPR.getString(CONSENT_DATE, getString(R.string.end_user_consent_2_date_not_found)) +
                getString(R.string.end_user_consent_2_message_2) +
                mySPR.getString(CONSENT_TIME, getString(R.string.end_user_consent_2_time_not_found))
        val spannableString = SpannableString(dialogMessage)
        val clickableSpan1 = object : ClickableSpan() {
            override fun onClick(view: View) {
                Utils.showLicenseDialog(myContext, PRIVACY_POLICY)
            }
        }
        val clickableSpan2 = object : ClickableSpan() {
            override fun onClick(view: View) {
                Utils.showLicenseDialog(myContext, TERMS_OF_USE)
            }
        }
        var currentText = getString(R.string.end_user_consent_2_privacy_policy)
        var index = dialogMessage.indexOf(currentText)
        spannableString.setSpan(
            clickableSpan1, index, index + currentText.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        currentText = getString(R.string.end_user_consent_2_terms_of_use)
        index = dialogMessage.indexOf(currentText)
        spannableString.setSpan(
            clickableSpan2, index, index + currentText.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        currentText = getString(R.string.end_user_consent_2_date)
        index = dialogMessage.indexOf(currentText)
        repeat(2) {
            spannableString.setSpan(UnderlineSpan(), index, index + currentText.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            currentText = getString(R.string.end_user_consent_2_time)
            index = dialogMessage.indexOf(currentText)
        }

        MaterialDialog(myContext).show {
            title(R.string.end_user_consent_2_title)
            message(text = spannableString) {
                messageTextView.movementMethod = LinkMovementMethod.getInstance()
            }
            positiveButton(android.R.string.ok)
            negativeButton(R.string.end_user_consent_2_button_2) {
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
        }
    }
}
