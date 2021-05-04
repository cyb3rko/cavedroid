package com.cyb3rko.cavedroid

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.text.Html
import android.view.*
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.cyb3rko.cavedroid.databinding.FragmentItemSearchBinding
import com.cyb3rko.cavedroid.rankings.MarketEntryViewHolder
import com.cyb3rko.cavedroid.rankings.MarketViewState
import com.cyb3rko.cavetaleapi.CavetaleAPI
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
        val webInterface = JavaScriptInterface()
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
                hideKeyboard()
                webInterface.html = null
                adapter.submitList(emptyList())
                adapter.notifyDataSetChanged()
                binding.apply {
                    animationView.playAnimation()
                    animationView.visibility = View.VISIBLE
                }
                webView.loadUrl(api.getSearchPhrase(v.text.toString()))
            }
            true
        }
    }

    private fun loadHtmlIntoRecycler(
            webInterface: JavaScriptInterface,
            adapter: RecyclerViewAdapter<MarketViewState.MarketEntry, RecyclerViewHolder<MarketViewState.MarketEntry>>
    ) {
        val list = api.getMarketResults(webInterface.html)
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