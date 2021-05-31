package com.cyb3rko.cavedroid

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
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
import com.cyb3rko.cavedroid.databinding.FragmentRankingBinding
import com.cyb3rko.cavedroid.rankings.ItemEntryViewHolder
import com.cyb3rko.cavedroid.rankings.ItemViewState
import com.cyb3rko.cavedroid.rankings.PlayerEntryViewHolder
import com.cyb3rko.cavedroid.rankings.PlayerViewState
import com.cyb3rko.cavetaleapi.CavetaleAPI
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import me.ibrahimyilmaz.kiel.adapter.RecyclerViewAdapter
import me.ibrahimyilmaz.kiel.core.RecyclerViewHolder
import kotlin.math.round

class RankingFragment : Fragment() {
    private var _binding: FragmentRankingBinding? = null
    private lateinit var myContext: Context

    private val args: RankingFragmentArgs by navArgs()
    private lateinit var adapter: RecyclerViewAdapter<*, RecyclerViewHolder<*>>

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentRankingBinding.inflate(inflater, container, false)
        val root = binding.root
        myContext = requireContext()

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val api = CavetaleAPI()
        var limit = 0
        var dataSuffix = ""

        when (args.rankingType) {
            0 -> ""// TODO Error
            1, 2 -> {
                adapter = RecyclerViewAdapter.adapterOf {
                    register(
                        layoutResource = R.layout.item_recycler_player,
                        viewHolder = ::PlayerEntryViewHolder,
                        onBindBindViewHolder = { vh, index, player ->
                            vh.rankView.text = "#${index + 1}"
                            vh.nameView.text = player.name
                            vh.dataView.text = player.data
                            vh.cardView.setOnClickListener {
                                MaterialDialog(myContext).show {
                                    icon(drawable = vh.avatarView.drawable)
                                    title(text = player.name)
                                    listItems(items = listOf("View Profile", "Copy Name", "Copy Balance")) { _, index, _ ->
                                        when (index) {
                                            0 -> {
                                                findNavController().navigate(R.id.go_to_home, bundleOf("name" to player.name))
                                            }
                                            1 -> {
                                                val clip = ClipData.newPlainText(player.name, player.name)
                                                (myContext.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager).setPrimaryClip(clip)
                                                showClipboardToast()
                                            }
                                            2 -> {
                                                val clip = ClipData.newPlainText(player.data, player.data)
                                                (myContext.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager).setPrimaryClip(clip)
                                                showClipboardToast()
                                            }
                                        }
                                    }
                                }
                            }
                            if (player.name != "The Bank") {
                                Glide.with(myContext)
                                    .load(api.getAvatarLink(player.name, 100))
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

                limit = if (args.rankingType == 1) 100 else 50
                dataSuffix = if (args.rankingType == 1) "Coins" else "Coins Turnover"
            }
            3 -> {
                adapter = RecyclerViewAdapter.adapterOf {
                    register(
                        layoutResource = R.layout.item_recycler_item,
                        viewHolder = ::ItemEntryViewHolder,
                        onBindBindViewHolder = { vh, index, item ->
                            vh.rankView.text = "#${index + 1}"
                            vh.nameView.text = item.name
                            vh.amountView.text = item.amount
                            vh.turnoverView.text = item.turnover
                            vh.cardView.setOnClickListener {
                                val amount = item.amount.dropLast(11).toInt()
                                val price = item.turnover.replace(",", "").dropLast(6).toFloat()
                                val perItem = round(price / amount * 100) / 100
                                MaterialDialog(myContext).show {
                                    if (vh.avatarView.drawable != null) icon(drawable = vh.avatarView.drawable)
                                    title(text = item.name)
                                    message(text = Html.fromHtml("<b>Sold units</b>: ${item.amount}<br/>" +
                                            "<b>Total Turnover</b>: ${item.turnover}<br/>" +
                                            "<b>Per Item Average</b>: $perItem Coins")) {
                                        lineSpacing(1.4f)
                                    }
                                    listItems(items = listOf(
                                        "Search for Item",
                                        "Copy Item Name",
                                        "Copy Sold Units",
                                        "Copy Total Turnover",
                                        "Copy Per Item Price"))
                                    { _, index, _ ->
                                        when (index) {
                                            0 -> {
                                                findNavController().navigate(R.id.go_to_item_search, bundleOf("item" to item.name))
                                            }
                                            1 -> {
                                                val clip = ClipData.newPlainText(item.name, item.name)
                                                (myContext.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager).setPrimaryClip(clip)
                                                showClipboardToast()
                                            }
                                            2 -> {
                                                val clip = ClipData.newPlainText(item.amount, item.amount)
                                                (myContext.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager).setPrimaryClip(clip)
                                                showClipboardToast()
                                            }
                                            3 -> {
                                                val clip = ClipData.newPlainText(item.turnover, item.turnover)
                                                (myContext.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager).setPrimaryClip(clip)
                                                showClipboardToast()
                                            }
                                            4 -> {
                                                val string = "$perItem Coins"
                                                val clip = ClipData.newPlainText(string, string)
                                                (myContext.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager).setPrimaryClip(clip)
                                                showClipboardToast()
                                            }
                                        }
                                    }
                                }
                            }
                            val itemName = item.name.replace(" ", "_").toLowerCase()
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

                limit = 50
            }
        }

        GlobalScope.launch {
            val list = when (args.rankingType) {
                1 -> api.getRichlist(limit)
                2 -> api.getTopSellers(limit)
                3 -> api.getTopItems(limit)
                else -> listOf()
            }
            if (list.isNotEmpty()) {
                when (args.rankingType) {
                    1, 2 -> {
                        val tempList = MutableList(limit) {
                            val pair = list[it] as Pair<String, String>
                            PlayerViewState.PlayerEntry(pair.first, "${pair.second}\n$dataSuffix")
                        }
                        adapter.submitList(tempList as List<Nothing>?)
                    }
                    3 -> {
                        val tempList = MutableList(limit) {
                            val triple = list[it] as Triple<String, String, String>
                            ItemViewState.ItemEntry(triple.first, "${triple.second}\nSold Units", "${triple.third}\nCoins")
                        }
                        adapter.submitList(tempList as List<Nothing>?)
                    }
                }
                activity?.runOnUiThread {
                    binding.animationView.visibility = View.INVISIBLE
                    binding.animationView.pauseAnimation()
                    binding.recycler.layoutManager = LinearLayoutManager(myContext)
                    binding.recycler.adapter = adapter
                }
            }
        }
    }

    private fun showClipboardToast() {
        Toast.makeText(myContext, "Copied to clipboard", Toast.LENGTH_SHORT).show()
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        menu.clear()
    }
}