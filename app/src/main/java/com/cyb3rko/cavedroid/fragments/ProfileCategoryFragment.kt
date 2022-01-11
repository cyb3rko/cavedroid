package com.cyb3rko.cavedroid.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Html
import android.view.*
import android.webkit.*
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItems
import com.cyb3rko.cavedroid.*
import com.cyb3rko.cavedroid.JavascriptInterface
import com.cyb3rko.cavedroid.databinding.FragmentListingBinding
import com.cyb3rko.cavedroid.rankings.MarketEntryViewHolder
import com.cyb3rko.cavedroid.rankings.MarketViewState
import com.cyb3rko.cavedroid.rankings.OfferEntryViewHolder
import com.cyb3rko.cavedroid.rankings.OfferEntryViewState
import com.cyb3rko.cavetaleapi.CavetaleAPI
import kotlin.math.round
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.ibrahimyilmaz.kiel.adapterOf
import me.ibrahimyilmaz.kiel.core.RecyclerViewHolder

class ProfileCategoryFragment : Fragment() {
    private var _binding: FragmentListingBinding? = null
    private lateinit var myContext: Context
    private lateinit var scope: LifecycleCoroutineScope

    private lateinit var adapter: ListAdapter<*, RecyclerViewHolder<*>>
    private val api = CavetaleAPI()
    private val args: ProfileCategoryFragmentArgs by navArgs()
    private var link = ""
    private val missingIcons = mutableSetOf<String>()
    private lateinit var mySPR: SharedPreferences
    private lateinit var webView: HtmlWebView

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentListingBinding.inflate(inflater, container, false)
        scope = viewLifecycleOwner.lifecycleScope
        return binding.root
    }

    @SuppressLint("CheckResult")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        myContext = requireContext()
        mySPR = requireActivity().getSharedPreferences(SHARED_PREFERENCE, Context.MODE_PRIVATE)

        val drawableId = Utils.getBackgroundDrawableId(resources, mySPR)
        if (drawableId != -1) {
            view.background = ResourcesCompat.getDrawable(resources, drawableId, myContext.theme)
        }

        val api = CavetaleAPI()

        when (args.category) {
            1, 2 -> {
                adapter = adapterOf {
                    register(
                        layoutResource = R.layout.item_recycler_market,
                        viewHolder = ::MarketEntryViewHolder,
                        onBindViewHolder = { vh, _, entry ->
                            vh.amountView.text = getString(R.string.item_amount, entry.amount, entry.item)
                            vh.priceView.text = getString(R.string.item_price, entry.price)
                            vh.playerView.text = entry.player
                            vh.cardView.setOnClickListener {
                                val amount = entry.amount.toInt()
                                val price = entry.price.replace(",", "").toFloat()
                                val perItem = round(price / amount * 100) / 100
                                MaterialDialog(myContext).show {
                                    try {
                                        icon(drawable = vh.avatarView.drawable)
                                    } catch (e: Exception) {}
                                    title(text = entry.player)
                                    message(text = Html.fromHtml(
                                        Utils.getFormattedDialogInformation(getString(R.string.item_dialog_information1), entry.item) +
                                                Utils.getFormattedDialogInformation(getString(R.string.item_dialog_information2), entry.amount) +
                                                Utils.getFormattedDialogPriceInformation(getString(R.string.item_dialog_information3), entry.price) +
                                                Utils.getFormattedDialogPriceInformation(getString(R.string.item_dialog_information4), entry.perItem,
                                                    false)
                                    )) {
                                        lineSpacing(1.4f)
                                    }
                                    listItems(items = listOf(
                                        getString(R.string.item_dialog_button1),
                                        if (args.category == 1) getString(R.string.item_dialog_button2_1) else getString(
                                            R.string.item_dialog_button2_2),
                                        getString(R.string.item_dialog_button3),
                                        getString(R.string.item_dialog_button4),
                                        getString(R.string.item_dialog_button5),
                                        getString(R.string.item_dialog_button6))
                                    )
                                    { _, index, _ ->
                                        when (index) {
                                            0 -> {
                                                findNavController().navigate(R.id.go_to_item_search, bundleOf("item" to entry.item))
                                            }
                                            1 -> {
                                                findNavController().navigate(R.id.go_to_home, bundleOf("name" to entry.player))
                                            }
                                            2 -> {
                                                Utils.storeToClipboard(myContext, entry.player)
                                                Utils.showClipboardToast(myContext, if (args.category == 1) "buyer name" else "seller name")
                                            }
                                            3 -> {
                                                Utils.storeToClipboard(myContext, entry.item)
                                                Utils.showClipboardToast(myContext, getString(R.string.clipboard_category_item))
                                            }
                                            4 -> {
                                                val text = "${entry.price} Coins"
                                                Utils.storeToClipboard(myContext, text)
                                                Utils.showClipboardToast(myContext, getString(R.string.clipboard_category_price))
                                            }
                                            5 -> {
                                                val text = "$perItem Coins"
                                                Utils.storeToClipboard(myContext, text)
                                                Utils.showClipboardToast(myContext, getString(R.string.clipboard_category_per_item_price))
                                            }
                                        }
                                    }
                                }
                            }
                            if (entry.player != "The Bank") {
                                Utils.loadAvatar(myContext, api, mySPR, vh.avatarView, entry.player, 100)
                            } else {
                                Utils.loadAvatar(myContext, api, mySPR, vh.avatarView, "God", 100)
                            }
                            Utils.loadItemIcon(myContext, vh.iconView, entry.item, missingIcons)
                        }
                    )
                }
            }
            3 -> {
                adapter = adapterOf {
                    register(
                        layoutResource = R.layout.item_recycler_offer,
                        viewHolder = ::OfferEntryViewHolder,
                        onBindViewHolder = { vh, _, entry ->
                            vh.amountView.text = getString(R.string.item_amount, entry.amount, entry.item)
                            vh.priceView.text = getString(R.string.item_price, entry.price)
                            vh.cardView.setOnClickListener {
                                val amount = entry.amount.toInt()
                                val price = entry.price.replace(",", "").toFloat()
                                val perItem = round(price / amount * 100) / 100
                                MaterialDialog(myContext).show {
                                    try {
                                        icon(drawable = vh.iconView.drawable)
                                    } catch (e: Exception) {}
                                    title(text = entry.item)
                                    message(text = Html.fromHtml(
                                        Utils.getFormattedDialogInformation(getString(R.string.item_dialog_information2), entry.amount) +
                                                Utils.getFormattedDialogPriceInformation(getString(R.string.item_dialog_information3), entry.price) +
                                                Utils.getFormattedDialogPriceInformation(getString(R.string.item_dialog_information4), entry.perItem,
                                                    false)
                                    )) {
                                        lineSpacing(1.4f)
                                    }
                                    listItems(items = listOf(
                                        getString(R.string.item_dialog_button1),
                                        getString(R.string.item_dialog_button4),
                                        getString(R.string.item_dialog_button5),
                                        getString(R.string.item_dialog_button6)))
                                    { _, index, _ ->
                                        when (index) {
                                            0 -> {
                                                findNavController().navigate(ProfileCategoryFragmentDirections.goToItemSearch(entry.item))
                                            }
                                            1 -> {
                                                Utils.storeToClipboard(myContext, entry.item)
                                                Utils.showClipboardToast(myContext, getString(R.string.clipboard_category_item))
                                            }
                                            2 -> {
                                                val text = "${entry.price} Coins"
                                                Utils.storeToClipboard(myContext, text)
                                                Utils.showClipboardToast(myContext, getString(R.string.clipboard_category_price))
                                            }
                                            3 -> {
                                                val text = "$perItem Coins"
                                                Utils.storeToClipboard(myContext, text)
                                                Utils.showClipboardToast(myContext, getString(R.string.clipboard_category_per_item_price))
                                            }
                                        }
                                    }
                                }
                            }
                            Utils.loadItemIcon(myContext, vh.iconView, entry.item, missingIcons)
                        }
                    )
                }
            }
        }

        webView = HtmlWebView(myContext)
        webView.webViewClient = object: WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                Handler(Looper.getMainLooper()).postDelayed({
                    kotlin.run { webView.fetchHmtl() }
                }, 600)

                scope.launch {
                    while (webView.javascriptInterface.html == null) {
                        delay(50)
                    }

                    loadHtmlIntoRecycler(webView.javascriptInterface)
                    webView.javascriptInterface.clearHTML()
                }
            }

            override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                showAnimation(true, false, false)
            }
        }

        binding.apply {
            recycler.apply {
                layoutManager = LinearLayoutManager(myContext)
                adapter = this@ProfileCategoryFragment.adapter
            }
            animationView.playAnimation()
            animationView.visibility = View.VISIBLE
        }

        link = when (args.category) {
            1 -> api.getItemsSoldLink(args.name)
            2 -> api.getItemsBoughtLink(args.name)
            3 -> api.getCurrentOffersLink(args.name)
            else -> ""
        }
        fetchData()

        binding.refreshLayout.apply {
            setProgressBackgroundColorSchemeResource(R.color.refreshLayoutBackground)
            setColorSchemeResources(R.color.refreshLayoutArrow)
            setOnRefreshListener {
                isRefreshing = false
                showRecycler(false)
                showAnimation(true, true, false)
                fetchData()
            }
        }
    }

    private fun fetchData() = webView.loadUrl(link)

    private fun loadHtmlIntoRecycler(webInterface: JavascriptInterface) {
        val list = when (args.category) {
            1 -> api.getItemsSold(webInterface.html!!)
            2 -> api.getItemsBought(webInterface.html!!)
            3 -> api.getCurrentOffers(webInterface.html!!)
            else -> emptyList()
        }
        if (list.isNotEmpty()) {
            val finalList = MutableList(list.size) {
                val tempList = list[it]
                when (args.category) {
                    1, 2 -> MarketViewState.MarketEntry(tempList[3], tempList[0], tempList[1], tempList[2], "", "")
                    3 -> OfferEntryViewState.OfferEntry(tempList[0], tempList[1], tempList[2], tempList[3], tempList[4])
                    else -> MarketViewState.MarketEntry("", tempList[0], tempList[1], tempList[2], tempList[3], tempList[4])
                }
            }
            showAnimation(false, true, false)
            adapter.submitList(finalList as List<Nothing>)
            showRecycler(true)
        } else {
            if (args.amount > 0) {
                fetchData()
            } else {
                showAnimation(true, true, true)
            }
        }
    }

    private fun showRecycler(show: Boolean) {
        val visibility = if (show) View.VISIBLE else View.INVISIBLE
        binding.recycler.visibility = visibility
    }

    private fun showAnimation(show: Boolean, connected: Boolean, emptyList: Boolean) {
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

    fun returnToHome() {
        val action = ProfileCategoryFragmentDirections.closeProfileCategory(args.name)
        findNavController().navigate(action)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.topbar_menu2, menu)
        menu.findItem(R.id.missing_icons_report).setOnMenuItemClickListener {
            Utils.showMissingIconsDialog(myContext, missingIcons, mySPR)
            true
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                returnToHome()
            }
        }
        return super.onOptionsItemSelected(item)
    }
}