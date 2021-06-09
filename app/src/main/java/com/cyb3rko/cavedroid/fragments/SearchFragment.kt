package com.cyb3rko.cavedroid.fragments

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.text.Html
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItems
import com.bumptech.glide.Glide
import com.cyb3rko.cavedroid.HtmlWebView
import com.cyb3rko.cavedroid.JavascriptInterface
import com.cyb3rko.cavedroid.R
import com.cyb3rko.cavedroid.Utils
import com.cyb3rko.cavedroid.databinding.FragmentItemSearchBinding
import com.cyb3rko.cavedroid.rankings.MarketEntryViewHolder
import com.cyb3rko.cavedroid.rankings.MarketViewState
import com.cyb3rko.cavetaleapi.CavetaleAPI
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.logEvent
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import me.ibrahimyilmaz.kiel.adapter.RecyclerViewAdapter
import me.ibrahimyilmaz.kiel.core.RecyclerViewHolder

class SearchFragment : Fragment() {
    private var _binding: FragmentItemSearchBinding? = null
    private lateinit var myContext: Context

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    private val api = CavetaleAPI()
    private val args: SearchFragmentArgs by navArgs()
    private lateinit var webView: HtmlWebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentItemSearchBinding.inflate(inflater, container, false)
        val root = binding.root
        myContext = requireContext()

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = RecyclerViewAdapter.adapterOf<MarketViewState.MarketEntry> {
            register(
                layoutResource = R.layout.item_recycler_market,
                viewHolder = ::MarketEntryViewHolder,
                onBindBindViewHolder = { vh: MarketEntryViewHolder, _, marketEntry: MarketViewState.MarketEntry ->
                    vh.amountView.text = getString(R.string.item_amount, marketEntry.amount, marketEntry.item)
                    vh.priceView.text = getString(R.string.item_price, marketEntry.price)
                    vh.sellerView.text = marketEntry.seller
                    vh.cardView.setOnClickListener {
                        MaterialDialog(myContext).show {
                            icon(drawable = vh.avatarView.drawable)
                            title(text = marketEntry.seller)
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
                                        findNavController().navigate(R.id.go_to_home, bundleOf("name" to marketEntry.seller))
                                    }
                                    1 -> {
                                        Utils.storeToClipboard(myContext, marketEntry.seller)
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
                    if (marketEntry.seller != "The Bank") {
                        Glide.with(myContext).load(api.getAvatarLink(marketEntry.seller, 100)).into(vh.avatarView)
                    } else {
                        Glide.with(myContext).load(api.getAvatarLink("God", 100)).into(vh.avatarView)
                    }
                }
            )
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

        binding.searchInput.setOnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val text = v.text.toString()
                FirebaseAnalytics.getInstance(myContext).logEvent("shop_search") {
                    param("item", text)
                }
                Utils.hideKeyboard(requireActivity())
                webView.javascriptInterface.clearHTML()
                adapter.submitList(emptyList())
                adapter.notifyDataSetChanged()
                binding.apply {
                    animationView.playAnimation()
                    animationView.visibility = View.VISIBLE
                }
                webView.loadUrl(api.getSearchPhrase(text))
            }
            true
        }

        if (args.item.isNotBlank()) {
            binding.searchInput.apply {
                setText(args.item)
                onEditorAction(EditorInfo.IME_ACTION_SEARCH)
            }
        }
    }

    private fun loadHtmlIntoRecycler(
        webInterface: JavascriptInterface,
        adapter: RecyclerViewAdapter<MarketViewState.MarketEntry, RecyclerViewHolder<MarketViewState.MarketEntry>>
    ) {
        val list = api.getMarketResults(webInterface.html!!)
        val finalList = MutableList(list.size) {
            val tempList = list[it]
            MarketViewState.MarketEntry(tempList[0], tempList[1], tempList[2], tempList[3], tempList[4], tempList[5])
        }
        activity?.runOnUiThread {
            binding.animationView.visibility = View.INVISIBLE
            binding.animationView.pauseAnimation()
            adapter.submitList(finalList)
            adapter.notifyDataSetChanged()
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        menu.clear()
    }
}