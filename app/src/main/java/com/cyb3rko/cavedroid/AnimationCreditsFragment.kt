package com.cyb3rko.cavedroid

import android.content.Intent
import android.net.Uri
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

class AnimationCreditsFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val information = listOf(
            Triple("Fake 3d coin", "Saim Hayyat", "https://lottiefiles.com/36928-fake-3d-coin")
        )
        val view = ScrollView(requireContext())
        val linearLayout = LinearLayout(requireContext())
        linearLayout.orientation = LinearLayout.VERTICAL
        information.forEach {
            val textView = TextView(requireContext())
            textView.textSize = 18f
            textView.setPaddingRelative(40, 50, 40, 0)
            val text = "\'${it.first}\' animation by ${it.second}"
            val spannableString = SpannableString(text)
            val clickableSpan = object: ClickableSpan() {
                override fun onClick(widget: View) {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(it.third)))
                }
            }
            spannableString.setSpan(clickableSpan, 1, 1 + it.first.length, Spanned.SPAN_INCLUSIVE_INCLUSIVE)
            textView.text = spannableString
            textView.movementMethod = LinkMovementMethod.getInstance()
            linearLayout.addView(textView)
        }
        view.addView(linearLayout)
        return view
    }
}