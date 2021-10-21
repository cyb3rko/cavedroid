package com.cyb3rko.cavedroid.fragments

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorInt
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.cyb3rko.cavedroid.*
import com.cyb3rko.cavedroid.ADAPTIVE_THEMING
import com.cyb3rko.cavedroid.SHARED_PREFERENCE
import com.cyb3rko.cavedroid.THEME
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentRankingsBinding.inflate(inflater, container, false)
        val root = binding.root
        myContext = requireContext()

        mySPR = requireActivity().getSharedPreferences(SHARED_PREFERENCE, Context.MODE_PRIVATE)

        val drawableId = Utils.getBackgroundDrawableId(resources, mySPR)
        if (drawableId != -1) {
            root.background = ResourcesCompat.getDrawable(resources, drawableId, myContext.theme)
        }

        if (mySPR.getBoolean(ADAPTIVE_THEMING, true)) setElementAccentColor()

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setRankingButtons()
    }

    private fun setElementAccentColor() {
        @ColorInt val accentColor = when (mySPR.getString(THEME, "0")!!.toInt()) {
            R.style.Theme_Cavedroid_BlueLight, R.style.Theme_Cavedroid_BlueDark -> R.color.forest_accent
            R.style.Theme_Cavedroid_GreenLight, R.style.Theme_Cavedroid_GreenDark -> R.color.house_accent
            else -> 0
        }
        if (accentColor == 0) return

        binding.apply {
            listOf(
                richlistCard,
                topSellersCard,
                topItemsCard
            ).forEach {
                it.setCardBackgroundColor(ResourcesCompat.getColor(resources, accentColor, myContext.theme))
            }
        }
    }

    private fun setRankingButtons() {
        val action1 = RankingsFragmentDirections.openRanking(1, getString(R.string.topbar_title_richlist))
        val action2 = RankingsFragmentDirections.openRanking(2, getString(R.string.topbar_title_top_sellers))
        val action3 = RankingsFragmentDirections.openRanking(3, getString(R.string.topbar_title_top_items))
        binding.richlistCard.setOnClickListener { findNavController().navigate(action1) }
        binding.topSellersCard.setOnClickListener { findNavController().navigate(action2) }
        binding.topItemsCard.setOnClickListener { findNavController().navigate(action3) }
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        menu.clear()
    }
}