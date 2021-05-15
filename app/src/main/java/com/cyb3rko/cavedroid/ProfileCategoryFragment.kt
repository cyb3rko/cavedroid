package com.cyb3rko.cavedroid

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.view.*
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.cyb3rko.cavedroid.databinding.FragmentProfileCategoryBinding
import com.cyb3rko.cavedroid.rankings.MarketEntryViewHolder
import com.cyb3rko.cavedroid.rankings.MarketViewState
import com.cyb3rko.cavetaleapi.CavetaleAPI
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import me.ibrahimyilmaz.kiel.adapter.RecyclerViewAdapter
import me.ibrahimyilmaz.kiel.core.RecyclerViewHolder

class ProfileCategoryFragment : Fragment() {
    private var _binding: FragmentProfileCategoryBinding? = null
    private lateinit var myContext: Context

    private lateinit var adapter: RecyclerViewAdapter<*, RecyclerViewHolder<*>>
    private val api = CavetaleAPI()
    private val args: ProfileCategoryFragmentArgs by navArgs()
    private lateinit var webView: WebView

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentProfileCategoryBinding.inflate(inflater, container, false)
        val root = binding.root
        myContext = requireContext()

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val api = CavetaleAPI()
        var limit = 100

        when (args.category) {
            0 -> ""// TODO Error
            1, 2 -> {
                adapter = RecyclerViewAdapter.adapterOf {
                    register(
                        layoutResource = R.layout.item_recycler_market,
                        viewHolder = ::MarketEntryViewHolder,
                        onBindBindViewHolder = { vh, _, entry ->
                            vh.amountView.text = "${entry.amount}x ${entry.item}"
                            vh.priceView.text = entry.price
                            vh.sellerView.text = entry.seller
                            if (entry.seller != "The Bank") {
                                Glide.with(myContext)
                                    .load(api.getAvatarLink(entry.seller, 100))
                                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                                    .into(vh.avatarView)
                            } else {
                                Glide.with(myContext)
                                    .load(api.getAvatarLink("God", 100))
                                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                                    .into(vh.avatarView)
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
                            vh.amountView.text = "${entry.amount}x ${entry.item}"
                            vh.priceView.text = entry.price
                            vh.sellerView.text = entry.seller
                            val itemName = entry.item.replace(" ", "_").toLowerCase()
                            val avatarResId = myContext.resources.getIdentifier("_item_$itemName", "drawable", myContext.packageName)
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