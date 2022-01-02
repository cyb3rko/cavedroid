package com.cyb3rko.cavedroid.appintro

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.fragment.app.Fragment
import com.cyb3rko.cavedroid.ANALYTICS_COLLECTION
import com.cyb3rko.cavedroid.CRASHLYTICS_COLLECTION
import com.cyb3rko.cavedroid.SHARED_PREFERENCE
import com.cyb3rko.cavedroid.databinding.FragmentAppintro4Binding

class AppIntro4thFragment : Fragment() {
    private var _binding: FragmentAppintro4Binding? = null

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    private lateinit var checkBox1: CheckBox
    private lateinit var checkBox2: CheckBox

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View{
        _binding = FragmentAppintro4Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        binding.apply {
            checkBox1 = analyticsCheck
            checkBox2 = crashlyticsCheck
        }

        val mySPR = requireContext().getSharedPreferences(SHARED_PREFERENCE, Context.MODE_PRIVATE)
        val editor = mySPR.edit()

        checkBox1.setOnCheckedChangeListener { _, b ->
            editor.putBoolean(ANALYTICS_COLLECTION, b).apply()
        }

        checkBox2.setOnCheckedChangeListener { _, b ->
            editor.putBoolean(CRASHLYTICS_COLLECTION, b).apply()
        }
    }

    companion object {
        fun newInstance() : AppIntro4thFragment {
            return AppIntro4thFragment()
        }
    }
}