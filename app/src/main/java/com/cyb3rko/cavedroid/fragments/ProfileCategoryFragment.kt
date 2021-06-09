package com.cyb3rko.cavedroid.fragments

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.text.Html
import android.view.*
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItems
import com.cyb3rko.cavedroid.*
import com.cyb3rko.cavedroid.databinding.FragmentListingBinding
import com.cyb3rko.cavedroid.rankings.MarketEntryViewHolder
import com.cyb3rko.cavedroid.rankings.MarketViewState
import com.cyb3rko.cavetaleapi.CavetaleAPI
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import me.ibrahimyilmaz.kiel.adapter.RecyclerViewAdapter
import me.ibrahimyilmaz.kiel.core.RecyclerViewHolder
import kotlin.math.round

class ProfileCategoryFragment : Fragment() {
    private var _binding: FragmentListingBinding? = null
    private lateinit var myContext: Context

    private lateinit var adapter: RecyclerViewAdapter<*, RecyclerViewHolder<*>>
    private val api = CavetaleAPI()
    private val args: ProfileCategoryFragmentArgs by navArgs()
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
        val root = binding.root
        myContext = requireContext()

        mySPR = requireActivity().getSharedPreferences(SHARED_PREFERENCE, Context.MODE_PRIVATE)

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val api = CavetaleAPI()

        when (args.category) {
            0 -> ""// TODO Error
            1, 2 -> {
                adapter = RecyclerViewAdapter.adapterOf {
                    register(
                        layoutResource = R.layout.item_recycler_market,
                        viewHolder = ::MarketEntryViewHolder,
                        onBindBindViewHolder = { vh, _, entry ->
                            vh.amountView.text = getString(R.string.item_amount, entry.amount, entry.item)
                            vh.priceView.text = getString(R.string.item_price, entry.price)
                            vh.sellerView.text = entry.seller
                            vh.cardView.setOnClickListener {
                                val amount = entry.amount.toInt()
                                val price = entry.price.replace(",", "").toFloat()
                                val perItem = round(price / amount * 100) / 100
                                MaterialDialog(myContext).show {
                                    icon(drawable = vh.avatarView.drawable)
                                    title(text = entry.seller)
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
                                                findNavController().navigate(R.id.go_to_home, bundleOf("name" to entry.seller))
                                            }
                                            2 -> {
                                                Utils.storeToClipboard(myContext, entry.seller)
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
                            if (entry.seller != "The Bank") {
                                Utils.loadAvatar(myContext, api, mySPR, vh.avatarView, entry.seller, 100)
                            } else {
                                Utils.loadAvatar(myContext, api, mySPR, vh.avatarView, "God", 100)
                            }
                        }
                    )
                }
            }
            3 -> {
                adapter = RecyclerViewAdapter.adapterOf {
                    register(
                        layoutResource = R.layout.item_recycler_market,
                        viewHolder = ::MarketEntryViewHolder,
                        onBindBindViewHolder = { vh, _, entry ->
                            vh.amountView.text = getString(R.string.item_amount, entry.amount, entry.item)
                            vh.priceView.text = getString(R.string.item_price, entry.price)
                            vh.sellerView.visibility = View.GONE
                            vh.cardView.setOnClickListener {
                                val amount = entry.amount.toInt()
                                val price = entry.price.replace(",", "").toFloat()
                                val perItem = round(price / amount * 100) / 100
                                MaterialDialog(myContext).show {
                                    if (vh.avatarView.drawable != null) icon(drawable = vh.avatarView.drawable)
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
                            val itemName = entry.item.replace(" ", "_").toLowerCase()
                            val formattedName = itemName.split(",")[0]
                            val avatarResId = myContext.resources.getIdentifier("_item_$formattedName", "drawable", myContext.packageName)
                            if (avatarResId != 0) {
                                vh.avatarView.setImageResource(avatarResId)
                            } else {
                                vh.avatarView.setImageResource(0)
                            }
                        }
                    )
                }
            }
        }

        webView = HtmlWebView(myContext)
        webView.webViewClient = object: WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                Handler().postDelayed({
                    kotlin.run { webView.fetchHmtl() }
                }, 600)

                GlobalScope.launch {
                    while (webView.javascriptInterface.html == null) {
                        Thread.sleep(50)
                    }

                    loadHtmlIntoRecycler(webView.javascriptInterface, adapter)
                }
            }
        }

        binding.recycler.layoutManager = LinearLayoutManager(myContext)
        binding.recycler.adapter = adapter

        binding.apply {
            animationView.playAnimation()
            animationView.visibility = View.VISIBLE
        }

        val link = when (args.category) {
            1 -> api.getItemsSoldLink(args.name)
            2 -> api.getItemsBoughtLink(args.name)
            3 -> api.getCurrentOffersLink(args.name)
            else -> ""
        }
        webView.loadUrl(link)
    }

    private fun loadHtmlIntoRecycler(
        webInterface: JavascriptInterface,
        adapter: RecyclerViewAdapter<*, RecyclerViewHolder<*>>
    ) {
        val list = when (args.category) {
            1 -> api.getItemsSold(webInterface.html!!)
            2 -> api.getItemsBought(webInterface.html!!)
            3 -> api.getCurrentOffers(webInterface.html!!)
            else -> emptyList()
        }
        val finalList = MutableList(list.size) {
            val tempList = list[it]
            when (args.category) {
                1, 2 -> MarketViewState.MarketEntry(tempList[3], tempList[0], tempList[1], tempList[2], "", "")
                3 -> MarketViewState.MarketEntry("", tempList[0], tempList[1], tempList[2], tempList[3], tempList[4])
                else -> MarketViewState.MarketEntry("", tempList[0], tempList[1], tempList[2], tempList[3], tempList[4])
            }
        }
        activity?.runOnUiThread {
            binding.animationView.visibility = View.INVISIBLE
            binding.animationView.pauseAnimation()
            adapter.submitList(finalList as List<Nothing>)
            adapter.notifyDataSetChanged()
        }
    }

    fun returnToHome() {
        val action = ProfileCategoryFragmentDirections.closeProfileCategory(args.name)
        findNavController().navigate(action)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        menu.clear()
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