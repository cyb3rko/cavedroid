package com.cyb3rko.cavedroid.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Html
import android.view.*
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
import com.cyb3rko.cavedroid.ADAPTIVE_THEMING
import com.cyb3rko.cavedroid.SHARED_PREFERENCE
import com.cyb3rko.cavedroid.THEME
import com.cyb3rko.cavedroid.databinding.FragmentListingBinding
import com.cyb3rko.cavedroid.rankings.ItemEntryViewHolder
import com.cyb3rko.cavedroid.rankings.ItemViewState
import com.cyb3rko.cavedroid.rankings.PlayerEntryViewHolder
import com.cyb3rko.cavedroid.rankings.PlayerViewState
import com.cyb3rko.cavetaleapi.CavetaleAPI
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import me.ibrahimyilmaz.kiel.adapterOf
import me.ibrahimyilmaz.kiel.core.RecyclerViewHolder
import kotlin.math.round

class RankingFragment : Fragment() {
    private var _binding: FragmentListingBinding? = null
    private lateinit var myContext: Context

    @ColorInt
    var accentColor = 0
    private val args: RankingFragmentArgs by navArgs()
    private lateinit var adapter: ListAdapter<*, RecyclerViewHolder<*>>
    private lateinit var api: CavetaleAPI
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

        val drawableId = Utils.getBackgroundDrawableId(resources, mySPR)
        if (drawableId != -1) {
            root.background = ResourcesCompat.getDrawable(resources, drawableId, myContext.theme)
        }

        return root
    }

    @SuppressLint("CheckResult")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        api = CavetaleAPI()
        var limit = 0

        retrieveAccentColor()

        when (args.rankingType) {
            1, 2 -> {
                adapter = adapterOf {
                    register(
                        layoutResource = R.layout.item_recycler_player,
                        viewHolder = ::PlayerEntryViewHolder,
                        onBindViewHolder = { vh, index, player ->
                            if (accentColor != 0) {
                                vh.cardView.setCardBackgroundColor(ResourcesCompat.getColor(resources, accentColor, myContext.theme))
                            }
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
                adapter = adapterOf {
                    register(
                        layoutResource = R.layout.item_recycler_item,
                        viewHolder = ::ItemEntryViewHolder,
                        onBindViewHolder = { vh, index, item ->
                            if (accentColor != 0) {
                                vh.cardView.setCardBackgroundColor(ResourcesCompat.getColor(resources, accentColor, myContext.theme))
                            }
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

        fetchData(limit)

        binding.refreshLayout.apply {
            setProgressBackgroundColorSchemeResource(R.color.refreshLayoutBackground)
            setColorSchemeResources(R.color.refreshLayoutArrow)
            setOnRefreshListener {
                isRefreshing = false
                showRecycler(false)
                showAnimation(true)
                fetchData(limit)
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

    private fun fetchData(limit: Int) {
        if (context != null) {
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
                            submitList(tempList as List<Nothing>)
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
                            submitList(tempList as List<Nothing>)
                        }
                    }
                    requireActivity().runOnUiThread {
                        showAnimation(false)
                        binding.recycler.layoutManager = LinearLayoutManager(myContext)
                        binding.recycler.adapter = adapter
                        showRecycler(true)
                    }
                } else {
                    requireActivity().runOnUiThread {
                        showRecycler(false)
                        showAnimation(true, false)
                    }
                }
            }
        }
    }

    private fun submitList(list: List<Nothing>) {
        requireActivity().runOnUiThread {
            adapter.submitList(list)
        }
    }

    private fun showRecycler(show: Boolean) {
        val visibility = if (show) View.VISIBLE else View.INVISIBLE
        binding.recycler.visibility = visibility
    }

    private fun showAnimation(show: Boolean, connected: Boolean = true) {
        val viewVisibility = if (show) View.VISIBLE else View.INVISIBLE
        val infoVisibility = if (show && !connected) View.VISIBLE else View.INVISIBLE
        val newSpeed = if (!connected) 1.2f else 3f
        val animation = if (connected) "coin-spin.json" else "no-connection.json"
        binding.apply {
            animationView.apply {
                setAnimation(animation)
                speed = newSpeed
                visibility = viewVisibility
                if (show) playAnimation() else pauseAnimation()
            }
            animationInfo.visibility = infoVisibility
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
