package com.cyb3rko.cavedroid

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.cyb3rko.cavedroid.databinding.FragmentRankingsBinding

class RankingsFragment : Fragment() {
    private var _binding: FragmentRankingsBinding? = null
    private lateinit var myContext: Context

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentRankingsBinding.inflate(inflater, container, false)
        val root = binding.root
        myContext = requireContext()

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setRankingButtons()
    }

    private fun setRankingButtons() {
        val action1 = RankingsFragmentDirections.openRanking(1, "Richlist")
        val action2 = RankingsFragmentDirections.openRanking(2, "Top Sellers")
        val action3 = RankingsFragmentDirections.openRanking(3, "Top Items")
        binding.richlistCard.setOnClickListener { findNavController().navigate(action1) }
        binding.topSellersCard.setOnClickListener { findNavController().navigate(action2) }
        binding.topItemsCard.setOnClickListener { findNavController().navigate(action3) }
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        menu.clear()
    }
}