package com.cyb3rko.cavedroid.appintro

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.cyb3rko.cavedroid.PRIVACY_POLICY
import com.cyb3rko.cavedroid.R
import com.cyb3rko.cavedroid.TERMS_OF_USE
import com.cyb3rko.cavedroid.Utils
import com.cyb3rko.cavedroid.databinding.FragmentAppintro3Binding
import com.github.appintro.SlidePolicy

class AppIntro3rdFragment : Fragment(), SlidePolicy {
    private var _binding: FragmentAppintro3Binding? = null

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    private lateinit var button1: Button
    private lateinit var button2: Button
    private lateinit var checkBox1: CheckBox
    private lateinit var checkBox2: CheckBox
    private var toast: Toast? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAppintro3Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        binding.apply {
            button1 = termsOfUseButton
            button2 = privacyPolicyButton
            checkBox1 = termsOfUseCheck
            checkBox2 = privacyPolicyCheck
        }

        button1.setOnClickListener { Utils.showLicenseDialog(context, TERMS_OF_USE) }
        button2.setOnClickListener { Utils.showLicenseDialog(context, PRIVACY_POLICY) }
    }

    override val isPolicyRespected: Boolean
        get() = (checkBox1.isChecked && checkBox2.isChecked)

    override fun onUserIllegallyRequestedNextPage() {
        if (toast != null) toast!!.cancel()
        toast = Toast.makeText(
            requireContext(),
            getString(R.string.intro_fragment3_toast),
            Toast.LENGTH_LONG
        )
        toast!!.show()
    }

    companion object {
        fun newInstance() : AppIntro3rdFragment {
            return AppIntro3rdFragment()
        }
    }
}
