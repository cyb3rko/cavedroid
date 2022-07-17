package com.cyb3rko.cavedroid.fragments

import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.cyb3rko.cavedroid.R
import com.cyb3rko.cavedroid.openURL

class AnimationCreditsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val myContext = requireContext()
        val information = listOf(
            Triple("Fake 3d coin", "Saim Hayyat", "https://lottiefiles.com/36928-fake-3d-coin"),
            Triple("No connection animation", "Lenny Miranda Jr.", "https://lottiefiles.com/9010-no-connection-animation"),
            Triple("search - in / out", "iconic studios", "https://lottiefiles.com/63061-search-in-out"),
            Triple("Empty Box Animation", "Aadesh Kulkami", "https://lottiefiles.com/67812-empty-box-animation")
        )
        val view = ScrollView(myContext)
        val linearLayout = LinearLayout(myContext)
        linearLayout.orientation = LinearLayout.VERTICAL
        information.forEach {
            val textView = TextView(myContext)
            textView.textSize = 18f
            textView.setPaddingRelative(40, 50, 40, 0)
            val text = getString(R.string.about_animations_description, it.first, it.second)
            val spannableString = SpannableString(text)
            val clickableSpan = object: ClickableSpan() {
                override fun onClick(widget: View) {
                    openURL(it.third)
                }
            }
            spannableString.setSpan(
                clickableSpan,
                0,
                it.first.length,
                Spanned.SPAN_INCLUSIVE_INCLUSIVE
            )
            textView.text = spannableString
            textView.movementMethod = LinkMovementMethod.getInstance()
            linearLayout.addView(textView)
        }
        view.addView(linearLayout)
        return view
    }
}
