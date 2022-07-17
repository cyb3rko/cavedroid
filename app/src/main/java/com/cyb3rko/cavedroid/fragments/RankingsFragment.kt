package com.cyb3rko.cavedroid.fragments

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.cyb3rko.cavedroid.*
import com.cyb3rko.cavedroid.SHARED_PREFERENCE
import com.cyb3rko.cavedroid.databinding.FragmentRankingsBinding

class RankingsFragment : Fragment() {
    private var _binding: FragmentRankingsBinding? = null
    private lateinit var myContext: Context

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!
    private lateinit var mySPR: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRankingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        myContext = requireContext()
        mySPR = requireActivity().getSharedPreferences(SHARED_PREFERENCE, Context.MODE_PRIVATE)

        val drawableId = Utils.getBackgroundDrawableId(resources, mySPR)
        if (drawableId != -1) {
            view.background = ResourcesCompat.getDrawable(resources, drawableId, myContext.theme)
        }

        setRankingButtons()
    }

    private fun setRankingButtons() {
        val action1 = RankingsFragmentDirections.openRanking(getString(R.string.topbar_title_richlist), 1)
        val action2 = RankingsFragmentDirections.openRanking(getString(R.string.topbar_title_top_sellers), 2)
        val action3 = RankingsFragmentDirections.openRanking(getString(R.string.topbar_title_top_items), 3)
        binding.richlistCard.setOnClickListener { findNavController().navigate(action1) }
        binding.topSellersCard.setOnClickListener { findNavController().navigate(action2) }
        binding.topItemsCard.setOnClickListener { findNavController().navigate(action3) }
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        menu.clear()
    }
}
