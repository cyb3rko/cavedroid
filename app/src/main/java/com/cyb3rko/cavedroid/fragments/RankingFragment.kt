package com.cyb3rko.cavedroid.fragments

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Html
import android.view.*
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItems
import com.cyb3rko.cavedroid.R
import com.cyb3rko.cavedroid.SHARED_PREFERENCE
import com.cyb3rko.cavedroid.Utils
import com.cyb3rko.cavedroid.databinding.FragmentListingBinding
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
    private var _binding: FragmentListingBinding? = null
    private lateinit var myContext: Context

    private val args: RankingFragmentArgs by navArgs()
    private lateinit var adapter: RecyclerViewAdapter<*, RecyclerViewHolder<*>>
    private val missingIcons = mutableSetOf<String>()
    private lateinit var mySPR: SharedPreferences

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
        var limit = 0

        when (args.rankingType) {
            0 -> ""// TODO Error
            1, 2 -> {
                adapter = RecyclerViewAdapter.adapterOf {
                    register(
                        layoutResource = R.layout.item_recycler_player,
                        viewHolder = ::PlayerEntryViewHolder,
                        onBindBindViewHolder = { vh, index, player ->
                            vh.rankView.text = getString(R.string.ranking_entry, index + 1)
                            vh.nameView.text = player.name
                            vh.dataView.text = player.data
                            vh.cardView.setOnClickListener {
                                MaterialDialog(myContext).show {
                                    icon(drawable = vh.avatarView.drawable)
                                    title(text = player.name)
                                    listItems(items = listOf(
                                        getString(R.string.ranking_player_dialog_button1),
                                        getString(R.string.ranking_player_dialog_button2),
                                        getString(R.string.ranking_player_dialog_button3))
                                    ) { _, index, _ ->
                                        when (index) {
                                            0 -> {
                                                findNavController().navigate(R.id.go_to_home, bundleOf("name" to player.name))
                                            }
                                            1 -> {
                                                Utils.storeToClipboard(myContext, player.name)
                                                Utils.showClipboardToast(myContext, getString(R.string.clipboard_category_name))
                                            }
                                            2 -> {
                                                Utils.storeToClipboard(myContext, player.data)
                                                Utils.showClipboardToast(myContext, getString(R.string.clipboard_category_name))
                                            }
                                        }
                                    }
                                }
                            }
                            if (player.name != "The Bank") {
                                Utils.loadAvatar(myContext, api, mySPR, vh.avatarView, player.name, 100)
                            } else {
                                Utils.loadAvatar(myContext, api, mySPR, vh.avatarView, "God", 100)
                            }
                        }
                    )
                }

                limit = if (args.rankingType == 1) 100 else 50
            }
            3 -> {
                adapter = RecyclerViewAdapter.adapterOf {
                    register(
                        layoutResource = R.layout.item_recycler_item,
                        viewHolder = ::ItemEntryViewHolder,
                        onBindBindViewHolder = { vh, index, item ->
                            vh.rankView.text = getString(R.string.ranking_entry, index + 1)
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
                                    message(text = Html.fromHtml(
                                        Utils.getFormattedDialogInformation(getString(R.string.ranking_item_dialog_information1), item.amount) +
                                                Utils.getFormattedDialogPriceInformation(getString(R.string.ranking_item_dialog_information2),
                                                    item.turnover) +
                                                Utils.getFormattedDialogPriceInformation(getString(R.string.ranking_item_dialog_information3),
                                                    perItem.toString(), false)
                                    )) {
                                        lineSpacing(1.4f)
                                    }
                                    listItems(items = listOf(
                                        getString(R.string.ranking_item_dialog_button1),
                                        getString(R.string.ranking_item_dialog_button2),
                                        getString(R.string.ranking_item_dialog_button3),
                                        getString(R.string.ranking_item_dialog_button4),
                                        getString(R.string.ranking_item_dialog_button5)
                                    )) { _, index, _ ->
                                        when (index) {
                                            0 -> {
                                                findNavController().navigate(R.id.go_to_item_search, bundleOf("item" to item.name))
                                            }
                                            1 -> {
                                                Utils.storeToClipboard(myContext, item.name)
                                                Utils.showClipboardToast(myContext, getString(R.string.clipboard_category_item))
                                            }
                                            2 -> {
                                                Utils.storeToClipboard(myContext, item.amount)
                                                Utils.showClipboardToast(myContext, getString(R.string.clipboard_category_amount))
                                            }
                                            3 -> {
                                                Utils.storeToClipboard(myContext, item.turnover)
                                                Utils.showClipboardToast(myContext, getString(R.string.clipboard_category_turnover))
                                            }
                                            4 -> {
                                                val text = "$perItem Coins"
                                                Utils.storeToClipboard(myContext, text)
                                                Utils.showClipboardToast(myContext, getString(R.string.clipboard_category_per_item_price))
                                            }
                                        }
                                    }
                                }
                            }
                            Utils.loadItemIcon(myContext, vh.avatarView, item.name, missingIcons)
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
                            val secondPart = if (args.rankingType == 1) {
                                getString(R.string.ranking_player_data1, pair.second)
                            } else {
                                getString(R.string.ranking_player_data2, pair.second)
                            }
                            PlayerViewState.PlayerEntry(
                                pair.first,
                                secondPart
                            )
                        }
                        adapter.submitList(tempList as List<Nothing>?)
                    }
                    3 -> {
                        val tempList = MutableList(limit) {
                            val triple = list[it] as Triple<String, String, String>
                            ItemViewState.ItemEntry(
                                triple.first,
                                getString(R.string.ranking_item_units, triple.second),
                                getString(R.string.ranking_item_turnover, triple.third)
                            )
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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        if (args.rankingType == 3) {
            inflater.inflate(R.menu.topbar_menu2, menu)
            menu.findItem(R.id.missing_icons_report).setOnMenuItemClickListener {
                Utils.showMissingIconsDialog(myContext, missingIcons, mySPR)
                true
            }
        }
    }
}