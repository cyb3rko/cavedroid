package com.cyb3rko.cavedroid.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.cyb3rko.abouticons.AboutIcons
import com.cyb3rko.cavedroid.R

class AboutIconsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)

        val aboutIcons = AboutIcons(
            requireContext(),
            R.drawable::class.java,
            parentFragmentManager
        ).setTitle(getString(R.string.about_icons_title))
        return aboutIcons.get()
    }
}
