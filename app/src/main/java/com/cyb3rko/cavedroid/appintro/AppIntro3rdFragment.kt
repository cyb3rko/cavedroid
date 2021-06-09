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
import com.github.appintro.SlidePolicy

class AppIntro3rdFragment : Fragment(), SlidePolicy {

    private lateinit var button1: Button
    private lateinit var button2: Button
    private lateinit var checkBox1: CheckBox
    private lateinit var checkBox2: CheckBox

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?)
            : View = inflater.inflate(R.layout.fragment_appintro3, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        button1 = view.findViewById(R.id.terms_of_use_button)
        button2 = view.findViewById(R.id.privacy_policy_button)
        checkBox1 = view.findViewById(R.id.terms_of_use_check)
        checkBox2 = view.findViewById(R.id.privacy_policy_check)

        button1.setOnClickListener { Utils.showLicenseDialog(context, TERMS_OF_USE) }
        button2.setOnClickListener { Utils.showLicenseDialog(context, PRIVACY_POLICY) }
    }

    override val isPolicyRespected: Boolean
        get() = (checkBox1.isChecked && checkBox2.isChecked)

    override fun onUserIllegallyRequestedNextPage() {
        Toast.makeText(requireContext(), getString(R.string.intro_fragment3_toast), Toast.LENGTH_LONG).show()
    }

    companion object {
        fun newInstance() : AppIntro3rdFragment {
            return AppIntro3rdFragment()
        }
    }
}