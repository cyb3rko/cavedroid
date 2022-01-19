package com.cyb3rko.cavedroid.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.cyb3rko.cavedroid.databinding.FragmentWikiBinding
import com.google.android.material.button.MaterialButton

class WikiFragment : Fragment(), View.OnClickListener {
    private var _binding: FragmentWikiBinding? = null

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentWikiBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.innerLayout.children.forEach {
            if (it is MaterialButton) {
                it.setOnClickListener(this)
            }
        }
    }

    override fun onClick(card: View?) {
        val text = card?.tag.toString()
        findNavController().navigate(WikiFragmentDirections.openEntry(text))
    }
}