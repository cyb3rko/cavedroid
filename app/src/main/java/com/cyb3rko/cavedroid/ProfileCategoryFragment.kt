package com.cyb3rko.cavedroid

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.text.Html
import android.view.*
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItems
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
import kotlin.math.round

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
                            vh.cardView.setOnClickListener {
                                val amount = entry.amount.toInt()
                                val price = entry.price.replace(",", "").toFloat()
                                val perItem = round(price / amount * 100) / 100
                                println(entry.item + " " + entry.marketAvg)
                                MaterialDialog(myContext).show {
                                    icon(drawable = vh.avatarView.drawable)
                                    title(text = entry.seller)
                                    message(text = Html.fromHtml("<b>Item</b>: ${entry.item}<br/>" +
                                            "<b>Amount</b>: ${entry.amount}<br/>" +
                                            "<b>Price</b>: ${entry.price} Coins<br/>" +
                                            "<b>Per Item</b>: $perItem Coins")) {
                                        lineSpacing(1.4f)
                                    }
                                    listItems(items = listOf(
                                        "Search for Item",
                                        "View Buyer",
                                        "Copy Player Name",
                                        "Copy Item Name",
                                        "Copy Price",
                                        "Copy Per Item Price")
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
                                                val clip = ClipData.newPlainText(entry.seller, entry.seller)
                                                (myContext.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager).setPrimaryClip(clip)
                                                showClipboardToast()
                                            }
                                            3 -> {
                                                val clip = ClipData.newPlainText(entry.item, entry.item)
                                                (myContext.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager).setPrimaryClip(clip)
                                                showClipboardToast()
                                            }
                                            4 -> {
                                                val text = "${entry.price} Coins"
                                                val clip = ClipData.newPlainText(text, text)
                                                (myContext.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager).setPrimaryClip(clip)
                                                showClipboardToast()
                                            }
                                            5 -> {
                                                val string = "$perItem Coins"
                                                val clip = ClipData.newPlainText(string, string)
                                                (myContext.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager).setPrimaryClip(clip)
                                                showClipboardToast()
                                            }
                                        }
                                    }
                                }
                            }
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
                            vh.cardView.setOnClickListener {
                                val amount = entry.amount.toInt()
                                val price = entry.price.replace(",", "").toFloat()
                                val perItem = round(price / amount * 100) / 100
                                println(entry.item + " " + entry.marketAvg)
                                MaterialDialog(myContext).show {
                                    if (vh.avatarView.drawable != null) icon(drawable = vh.avatarView.drawable)
                                    title(text = entry.item)
                                    message(text = Html.fromHtml("<b>Amount</b>: ${entry.amount}<br/>" +
                                            "<b>Price</b>: ${entry.price} Coins<br/>" +
                                            "<b>Per Item</b>: $perItem Coins")) {
                                        lineSpacing(1.4f)
                                    }
                                    listItems(items = listOf(
                                        "Search for Item",
                                        "Copy Item Name",
                                        "Copy Price",
                                        "Copy Per Item Price"))
                                    { _, index, _ ->
                                        when (index) {
                                            0 -> {
                                                findNavController().navigate(ProfileCategoryFragmentDirections.goToItemSearch(entry.item))
                                            }
                                            1 -> {
                                                val clip = ClipData.newPlainText(entry.item, entry.item)
                                                (myContext.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager).setPrimaryClip(clip)
                                                showClipboardToast()
                                            }
                                            2 -> {
                                                val clip = ClipData.newPlainText(entry.price, entry.price)
                                                (myContext.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager).setPrimaryClip(clip)
                                                showClipboardToast()
                                            }
                                            3 -> {
                                                val string = perItem.toString()
                                                val clip = ClipData.newPlainText(string, string)
                                                (myContext.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager).setPrimaryClip(clip)
                                                showClipboardToast()
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

    private fun showClipboardToast() {
        Toast.makeText(myContext, "Copied to clipboard", Toast.LENGTH_SHORT).show()
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