package com.cyb3rko.cavedroid

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
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
                            if (player.name != "The Bank") {
                                Glide.with(myContext).load(api.getAvatarLink(player.name, 100)).into(vh.avatarView)
                            } else {
                                Glide.with(myContext).load(api.getAvatarLink("God", 100)).into(vh.avatarView)
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

                            val itemName = item.name.replace(" ", "_").toLowerCase()
                            val avatarResId = myContext.resources.getIdentifier("_item_$itemName", "drawable", myContext.packageName)
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

    override fun onPrepareOptionsMenu(menu: Menu) {
        menu.clear()
    }
}