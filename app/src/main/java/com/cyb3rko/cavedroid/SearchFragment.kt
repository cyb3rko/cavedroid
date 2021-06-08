package com.cyb3rko.cavedroid

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.text.Html
import android.view.*
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItems
import com.bumptech.glide.Glide
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
    private lateinit var webView: WebView

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
                    vh.amountView.text = "${marketEntry.amount}x ${marketEntry.item}"
                    vh.priceView.text = "${marketEntry.price}\nCoins"
                    vh.sellerView.text = "${marketEntry.seller}"
                    vh.cardView.setOnClickListener {
                        MaterialDialog(myContext).show {
                            icon(drawable = vh.avatarView.drawable)
                            title(text = marketEntry.seller)
                            message(text = Html.fromHtml("<b>Item</b>: ${marketEntry.item}<br/>" +
                                    "<b>Stack Size</b>: ${marketEntry.amount}<br/>" +
                                    "<b>Total price</b>: ${marketEntry.price} Coins<br/>" +
                                    "<b>Per Item Price</b>: ${marketEntry.perItem} Coins")) {
                                lineSpacing(1.4f)
                            }
                            listItems(items = listOf(
                                "View Seller Profile",
                                "Copy Seller Name",
                                "Copy Item Name",
                                "Copy Price",
                                "Copy Per Item Price")
                            ) { _, index, _ ->
                                when (index) {
                                    0 -> {
                                        findNavController().navigate(R.id.go_to_home, bundleOf("name" to marketEntry.seller))
                                    }
                                    1 -> {
                                        val clip = ClipData.newPlainText(marketEntry.seller, marketEntry.seller)
                                        (myContext.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager).setPrimaryClip(clip)
                                        showClipboardToast()
                                    }
                                    2 -> {
                                        val clip = ClipData.newPlainText(marketEntry.item, marketEntry.item)
                                        (myContext.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager).setPrimaryClip(clip)
                                        showClipboardToast()
                                    }
                                    3 -> {
                                        val text = "${marketEntry.price} Coins"
                                        val clip = ClipData.newPlainText(text, text)
                                        (myContext.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager).setPrimaryClip(clip)
                                        showClipboardToast()
                                    }
                                    4 -> {
                                        val text = "${marketEntry.perItem} Coins"
                                        val clip = ClipData.newPlainText(text, text)
                                        (myContext.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager).setPrimaryClip(clip)
                                        showClipboardToast()
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

        webView = WebView(myContext)
        webView.settings.javaScriptEnabled = true
        val webInterface = JavascriptInterface()
        webView.addJavascriptInterface(webInterface, "HtmlViewer")
        webView.webViewClient = object: WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                val handler = Handler()
                handler.postDelayed({
                    kotlin.run { webView.loadUrl(
                        "javascript:window.HtmlViewer.showHTML('<head>'+document.getElementsByTagName('html')[0].innerHTML+'</head>');"
                    )
                    }
                }, 600)

                GlobalScope.launch {
                    while (webInterface.html == null) {
                        Thread.sleep(100)
                    }

                    loadHtmlIntoRecycler(webInterface, adapter)
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
                hideKeyboard()
                webInterface.html = null
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

        if (args.item != "") {
            binding.searchInput.apply {
                setText(args.item)
                onEditorAction(EditorInfo.IME_ACTION_SEARCH)
            }
        }
    }

    private fun showClipboardToast() {
        Toast.makeText(myContext, "Copied to clipboard", Toast.LENGTH_SHORT).show()
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

    private fun hideKeyboard() {
        val imm = requireActivity().getSystemService(AppCompatActivity.INPUT_METHOD_SERVICE) as InputMethodManager
        var view = requireActivity().currentFocus
        if (view == null) {
            view = View(requireActivity())
        }
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        menu.clear()
    }
}