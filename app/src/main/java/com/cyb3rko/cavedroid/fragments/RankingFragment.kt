package com.cyb3rko.cavedroid.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Html
import android.view.*
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import com.cyb3rko.cavedroid.R
import com.cyb3rko.cavedroid.SHARED_PREFERENCE
import com.cyb3rko.cavedroid.Utils
import com.cyb3rko.cavedroid.databinding.FragmentListingBinding
import com.cyb3rko.cavedroid.rankings.ItemEntryViewHolder
import com.cyb3rko.cavedroid.rankings.ItemViewState
import com.cyb3rko.cavedroid.rankings.PlayerEntryViewHolder
import com.cyb3rko.cavedroid.rankings.PlayerViewState
import com.cyb3rko.cavetaleapi.CavetaleAPI
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlin.math.round
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import me.ibrahimyilmaz.kiel.adapterOf
import me.ibrahimyilmaz.kiel.core.RecyclerViewHolder

class RankingFragment : Fragment() {
    private var _binding: FragmentListingBinding? = null
    private lateinit var myContext: Context

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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentListingBinding.inflate(inflater, container, false)
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

        api = CavetaleAPI()
        var limit = 0

        when (args.rankingType) {
            1, 2 -> {
                adapter = adapterOf {
                    register(
                        layoutResource = R.layout.item_recycler_player,
                        viewHolder = ::PlayerEntryViewHolder,
                        onBindViewHolder = { vh, index, player ->
                            vh.rankView.text = getString(R.string.ranking_entry, index + 1)
                            vh.nameView.text = player.name
                            vh.dataView.text = player.data
                            vh.cardView.setOnClickListener {
                                val icon = if (vh.avatarView.drawable != null) {
                                    vh.avatarView.drawable
                                } else null

                                MaterialAlertDialogBuilder(myContext, R.style.Dialog)
                                    .setIcon(icon)
                                    .setTitle(player.name)
                                    .setPositiveButton(
                                        getString(R.string.ranking_player_dialog_button)
                                    ) { _, _ ->
                                        findNavController().navigate(
                                            R.id.go_to_home,
                                            bundleOf("name" to player.name)
                                        )
                                    }
                                    .show()
                            }
                            if (player.name != "The Bank") {
                                Utils.loadAvatar(
                                    myContext,
                                    api,
                                    mySPR,
                                    vh.avatarView,
                                    player.name,
                                    100
                                )
                            } else {
                                Utils.loadAvatar(
                                    myContext,
                                    api,
                                    mySPR,
                                    vh.avatarView,
                                    "God",
                                    100
                                )
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
                            vh.rankView.text = getString(R.string.ranking_entry, index + 1)
                            vh.nameView.text = item.name
                            vh.amountView.text = item.amount
                            vh.turnoverView.text = item.turnover
                            vh.cardView.setOnClickListener {
                                val amount = item.amount.dropLast(11).toInt()
                                val price = item.turnover
                                    .replace(",", "")
                                    .dropLast(6)
                                    .toFloat()
                                val perItem = round(price / amount * 100) / 100
                                val icon = if (vh.avatarView.drawable != null) {
                                    vh.avatarView.drawable
                                } else null

                                MaterialAlertDialogBuilder(myContext, R.style.Dialog)
                                    .setIcon(icon)
                                    .setTitle(item.name)
                                    .setMessage(Html.fromHtml(
                                        Utils.getFormattedDialogInformation(
                                            getString(R.string.ranking_item_dialog_information1),
                                            item.amount
                                        ) +
                                                Utils.getFormattedDialogInformation(
                                                    getString(R.string.ranking_item_dialog_information2),
                                                    item.turnover
                                                ) +
                                                Utils.getFormattedDialogPriceInformation(
                                                    getString(R.string.ranking_item_dialog_information3),
                                                    perItem.toString(), false
                                                )
                                    ))
                                    .setPositiveButton(
                                        getString(R.string.ranking_item_dialog_button)
                                    ) { _, _ ->
                                        findNavController().navigate(
                                            R.id.go_to_item_search,
                                            bundleOf("item" to item.name)
                                        )
                                    }
                                    .show()
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

    private fun fetchData(limit: Int) {
        lifecycleScope.launch {
            val listJob = async(Dispatchers.IO) {
                when (args.rankingType) {
                    1 -> api.getRichlist(limit)
                    2 -> api.getTopSellers(limit)
                    3 -> api.getTopItems(limit)
                    else -> listOf()
                }
            }
            val list = listJob.await()

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
                showAnimation(false)
                binding.recycler.layoutManager = LinearLayoutManager(myContext)
                binding.recycler.adapter = adapter
                showRecycler(true)
            } else {
                showRecycler(false)
                showAnimation(true, false)
            }
        }
    }

    private fun submitList(list: List<Nothing>) = adapter.submitList(list)

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
