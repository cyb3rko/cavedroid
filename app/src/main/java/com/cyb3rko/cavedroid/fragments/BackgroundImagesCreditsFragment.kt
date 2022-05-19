package com.cyb3rko.cavedroid.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.cyb3rko.cavedroid.databinding.FragmentBackgroundImagesBinding

class BackgroundImagesCreditsFragment : Fragment() {
    private var _binding: FragmentBackgroundImagesBinding? = null

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBackgroundImagesBinding.inflate(inflater, container, false)
        return binding.root
    }
}
