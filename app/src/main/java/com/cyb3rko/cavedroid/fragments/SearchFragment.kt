package com.cyb3rko.cavedroid.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.text.Html
import android.view.*
import android.view.inputmethod.EditorInfo
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.annotation.ColorInt
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItems
import com.cyb3rko.cavedroid.*
import com.cyb3rko.cavedroid.databinding.FragmentItemSearchBinding
import com.cyb3rko.cavedroid.rankings.MarketEntryViewHolder
import com.cyb3rko.cavedroid.rankings.MarketViewState
import com.cyb3rko.cavetaleapi.CavetaleAPI
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.logEvent
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import me.ibrahimyilmaz.kiel.adapterOf
import me.ibrahimyilmaz.kiel.core.RecyclerViewHolder

class SearchFragment : Fragment() {
    private var _binding: FragmentItemSearchBinding? = null
    private lateinit var myContext: Context

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    @ColorInt
    var accentColor = 0
    private lateinit var adapter: ListAdapter<*, RecyclerViewHolder<*>>
    private val api = CavetaleAPI()
    private val args: SearchFragmentArgs by navArgs()
    private var lastQueryEmpty = false
    private val missingIcons = mutableSetOf<String>()
    private lateinit var mySPR: SharedPreferences
    private var text = ""
    private lateinit var webView: HtmlWebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentItemSearchBinding.inflate(inflater, container, false)
        val root = binding.root
        myContext = requireContext()

        mySPR = requireActivity().getSharedPreferences(SHARED_PREFERENCE, Context.MODE_PRIVATE)

        var searchAnimation = "search_blue_dark.json"

        if (mySPR.getBoolean(ADAPTIVE_THEMING, true)) {
            searchAnimation = when (mySPR.getString(THEME, "0")!!.toInt()) {
                R.style.Theme_Cavedroid_BlueLight -> "search_blue_light.json"
                R.style.Theme_Cavedroid_BlueDark -> "search_blue_dark.json"
                R.style.Theme_Cavedroid_GreenLight, R.style.Theme_Cavedroid_GreenDark -> "search_green.json"
                else -> "search_blue_dark.json"
            }
        }
        binding.animationView.setAnimation(searchAnimation)

        val drawableId = Utils.getBackgroundDrawableId(resources, mySPR)
        if (drawableId != -1) {
            root.background = ResourcesCompat.getDrawable(resources, drawableId, myContext.theme)
        }

        return root
    }

    @SuppressLint("CheckResult")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        retrieveAccentColor()

        adapter = adapterOf {
            register(
                layoutResource = R.layout.item_recycler_market,
                viewHolder = ::MarketEntryViewHolder,
                onBindViewHolder = { vh: MarketEntryViewHolder, _, marketEntry: MarketViewState.MarketEntry ->
                    if (accentColor != 0) {
                        vh.cardView.setCardBackgroundColor(ResourcesCompat.getColor(resources, accentColor, myContext.theme))
                    }
                    vh.amountView.text = getString(R.string.item_amount, marketEntry.amount, marketEntry.item)
                    vh.priceView.text = getString(R.string.item_price, marketEntry.price)
                    vh.playerView.text = marketEntry.player
                    vh.cardView.setOnClickListener {
                        MaterialDialog(myContext).show {
                            try {
                                icon(drawable = vh.avatarView.drawable)
                            } catch (e: Exception) {}
                            title(text = marketEntry.player)
                            message(text = Html.fromHtml(
                                Utils.getFormattedDialogInformation(getString(R.string.item_search_dialog_information1), marketEntry.item) +
                                        Utils.getFormattedDialogInformation(getString(R.string.item_search_dialog_information2), marketEntry.amount) +
                                        Utils.getFormattedDialogPriceInformation(getString(R.string.item_search_dialog_information3),
                                            marketEntry.price) +
                                        Utils.getFormattedDialogPriceInformation(getString(R.string.item_search_dialog_information4),
                                            marketEntry.perItem))
                            ) {
                                lineSpacing(1.4f)
                            }
                            listItems(items = listOf(
                                getString(R.string.item_search_dialog_button1),
                                getString(R.string.item_search_dialog_button2),
                                getString(R.string.item_search_dialog_button3),
                                getString(R.string.item_search_dialog_button4),
                                getString(R.string.item_search_dialog_button5)
                            )) { _, index, _ ->
                                when (index) {
                                    0 -> {
                                        findNavController().navigate(R.id.go_to_home, bundleOf("name" to marketEntry.player))
                                    }
                                    1 -> {
                                        Utils.storeToClipboard(myContext, marketEntry.player)
                                        Utils.showClipboardToast(myContext, getString(R.string.clipboard_category_name))
                                    }
                                    2 -> {
                                        Utils.storeToClipboard(myContext, marketEntry.item)
                                        Utils.showClipboardToast(myContext, getString(R.string.clipboard_category_item))
                                    }
                                    3 -> {
                                        val text = "${marketEntry.price} Coins"
                                        Utils.storeToClipboard(myContext, text)
                                        Utils.showClipboardToast(myContext, getString(R.string.clipboard_category_price))
                                    }
                                    4 -> {
                                        val text = "${marketEntry.perItem} Coins"
                                        Utils.storeToClipboard(myContext, text)
                                        Utils.showClipboardToast(myContext, getString(R.string.clipboard_category_per_item_price))
                                    }
                                }
                            }
                        }
                    }
                    if (marketEntry.player != "The Bank") {
                        Utils.loadAvatar(myContext, api, mySPR, vh.avatarView, marketEntry.player, 100)
                    } else {
                        Utils.loadAvatar(myContext, api, mySPR, vh.avatarView, "God", 100)
                    }
                    Utils.loadItemIcon(myContext, vh.iconView, marketEntry.item, missingIcons)
                }
            )
        }

        webView = HtmlWebView(myContext)
        webView.webViewClient = object: WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                Handler().postDelayed({
                    kotlin.run { webView.fetchHmtl() }
                }, 600)

                @Suppress("BlockingMethodInNonBlockingContext")
                GlobalScope.launch {
                    while (webView.javascriptInterface.html == null) {
                        Thread.sleep(50)
                    }

                    loadHtmlIntoRecycler(webView.javascriptInterface)
                    webView.javascriptInterface.clearHTML()
                }
            }

            override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                showAnimation(true, false)
            }
        }

        binding.recycler.apply {
            layoutManager = LinearLayoutManager(myContext)
            adapter = this@SearchFragment.adapter
        }

        binding.searchInput.setOnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                text = v.text.toString()
                FirebaseAnalytics.getInstance(myContext).logEvent("shop_search") {
                    param("item", text)
                }
                Utils.hideKeyboard(requireActivity())
                webView.javascriptInterface.clearHTML()
                adapter.submitList(emptyList())
                showAnimation(true)
                fetchData(text)
            }
            true
        }

        if (args.item.isNotBlank()) {
            binding.searchInput.apply {
                setText(args.item)
                onEditorAction(EditorInfo.IME_ACTION_SEARCH)
            }
        }

        binding.refreshLayout.apply {
            setProgressBackgroundColorSchemeResource(R.color.refreshLayoutBackground)
            setColorSchemeResources(R.color.refreshLayoutArrow)
            setOnRefreshListener {
                isRefreshing = false
                showRecycler(false)
                showAnimation(true)
                fetchData(text)
            }
        }
    }

    private fun retrieveAccentColor() {
        if (mySPR.getBoolean(ADAPTIVE_THEMING, true)) {
            accentColor = when (mySPR.getString(THEME, "0")!!.toInt()) {
                R.style.Theme_Cavedroid_BlueLight, R.style.Theme_Cavedroid_BlueDark -> R.color.forest_accent
                R.style.Theme_Cavedroid_GreenLight, R.style.Theme_Cavedroid_GreenDark -> R.color.house_accent
                else -> 0
            }
        }
    }

    private fun fetchData(searchPhrase: String) = webView.loadUrl(api.getSearchPhrase(searchPhrase))

    private fun loadHtmlIntoRecycler(webInterface: JavascriptInterface) {
        if (context != null) {
            val list = api.getMarketResults(webInterface.html!!)
            val finalList = MutableList(list.size) {
                val tempList = list[it]
                MarketViewState.MarketEntry(tempList[0], tempList[1], tempList[2], tempList[3], tempList[4], tempList[5])
            }
            requireActivity().runOnUiThread {
                if (finalList.isNotEmpty()) {
                    showAnimation(false)
                    adapter.submitList(finalList as List<Nothing>)
                    showRecycler(true)
                } else {
                    if (!lastQueryEmpty) {
                        fetchData(text)
                        lastQueryEmpty = true
                    } else {
                        showAnimation(true, true, true)
                        lastQueryEmpty = false
                    }
                }
            }
        }
    }

    private fun showRecycler(show: Boolean) {
        val visibility = if (show) View.VISIBLE else View.INVISIBLE
        binding.recycler.visibility = visibility
    }

    private fun showAnimation(show: Boolean, connected: Boolean = true, emptyList: Boolean = false) {
        val viewVisibility = if (show) View.VISIBLE else View.INVISIBLE
        val infoVisibility = if (show && !connected) View.VISIBLE else View.INVISIBLE
        val newSpeed = if (emptyList || !connected) 1.2f else 3f
        val animation = if (connected && !emptyList) {
            "coin-spin.json"
        } else if (emptyList) {
            "no-results.json"
        } else {
            "no-connection.json"
        }
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
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.topbar_menu2, menu)
        menu.findItem(R.id.missing_icons_report).setOnMenuItemClickListener {
            Utils.showMissingIconsDialog(myContext, missingIcons, requireActivity().getSharedPreferences(SHARED_PREFERENCE, Context.MODE_PRIVATE))
            true
        }
    }
}
