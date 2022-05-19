package com.cyb3rko.cavedroid.fragments

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Html
import android.text.method.LinkMovementMethod
import android.view.*
import android.webkit.*
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.cyb3rko.cavedroid.R
import com.cyb3rko.cavedroid.SHARED_PREFERENCE
import com.cyb3rko.cavedroid.THEME
import com.cyb3rko.cavedroid.databinding.*
import com.github.rjeschke.txtmark.Processor

private const val COLORFALL_MAP_MAKING = "colorfall_map_making"
private const val VERTIGO_MAP_MAKING = "vertigo_map_making"
private const val LANDMARKS = "landmarks"
private const val LINK_PORTALS = "link_portals"
private const val CHEST_SHOPS = "chest_shops"
private const val COLORFALL = "colorfall"
private const val RAID_HUB = "raid_hub"

class WikiEntryFragment : Fragment() {
    private val args: WikiEntryFragmentArgs by navArgs()
    private lateinit var myContext: Context
    private var link = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        myContext = requireContext()
        val name = args.name.toLowerCase().replace(" ", "_")
        try {
            val arrayId = myContext.resources.getIdentifier(
                name,
                "array",
                myContext.packageName
            )
            val info = myContext.resources.getStringArray(arrayId)
            val binding = FragmentWikiEntryBinding.inflate(inflater, container, false)
            binding.text.movementMethod = LinkMovementMethod.getInstance()
            link = info[0]
            binding.hint.text = info[1]
            binding.title.text = info[2]
            binding.text.text = Html.fromHtml(Processor.process(info[3]))

            if (info.size > 4) {
                loadImage(info[4], binding.image)

                if (info.size > 5) {
                    loadImage(info[5], binding.image2)
                }
            }
            return binding.root
        } catch (e: Exception) {
            val linkStringId = myContext.resources.getIdentifier(
                "wiki_$name",
                "string",
                myContext.packageName
            )
            link = myContext.resources.getString(linkStringId)

            val binding = when (name) {
                COLORFALL_MAP_MAKING -> {
                    val tempBinding = FragmentWikiEntryColorfallMapMakingBinding
                        .inflate(inflater, container, false)
                    val textViews = listOf(
                        tempBinding.text1,
                        tempBinding.text2,
                        tempBinding.text3,
                        tempBinding.text4,
                        tempBinding.text5,
                        tempBinding.text6,
                        tempBinding.text7,
                        tempBinding.text8
                    )
                    loadStringsIntoViews(textViews, "wiki_$COLORFALL_MAP_MAKING")

                    val imageViews = listOf(
                        tempBinding.image1,
                        tempBinding.image2,
                        tempBinding.image3,
                        tempBinding.image4,
                        tempBinding.image5,
                        tempBinding.image6,
                        tempBinding.image7
                    )
                    loadImagesIntoViews(imageViews, "wiki_${COLORFALL_MAP_MAKING}_link")
                    tempBinding
                }
                VERTIGO_MAP_MAKING -> {
                    val tempBinding = FragmentWikiEntryVertigoMapMakingBinding
                        .inflate(inflater, container, false)
                    val textViews = listOf(
                        tempBinding.text1,
                        tempBinding.text2,
                        tempBinding.text3,
                        tempBinding.text4,
                        tempBinding.text5,
                        tempBinding.text6
                    )
                    loadStringsIntoViews(textViews, "wiki_$VERTIGO_MAP_MAKING")

                    val imageViews = listOf(
                        tempBinding.image1,
                        tempBinding.image2,
                        tempBinding.image3,
                        tempBinding.image4,
                        tempBinding.image5
                    )
                    loadImagesIntoViews(imageViews, "wiki_${VERTIGO_MAP_MAKING}_link")
                    tempBinding
                }
                LANDMARKS -> {
                    val tempBinding = FragmentWikiEntryLandmarksBinding
                        .inflate(inflater, container, false)
                    val textViews = listOf(
                        tempBinding.text1,
                        tempBinding.text2,
                        tempBinding.text3,
                        tempBinding.text4,
                        tempBinding.text5,
                        tempBinding.text6
                    )
                    loadStringsIntoViews(textViews, "wiki_$LANDMARKS")

                    val imageViews = listOf(
                        tempBinding.image1,
                        tempBinding.image2,
                        tempBinding.image3,
                        tempBinding.image4,
                        tempBinding.image5
                    )
                    loadImagesIntoViews(imageViews, "wiki_${LANDMARKS}_link")
                    tempBinding
                }
                LINK_PORTALS -> {
                    val tempBinding = FragmentWikiEntryLinkPortalsBinding
                        .inflate(inflater, container, false)
                    val textViews = listOf(
                        tempBinding.text1,
                        tempBinding.text2,
                        tempBinding.text3,
                        tempBinding.text4
                    )
                    loadStringsIntoViews(textViews, "wiki_$LINK_PORTALS")

                    val imageViews = listOf(
                        tempBinding.image1,
                        tempBinding.image2,
                        tempBinding.image3,
                        tempBinding.image4,
                        tempBinding.image5
                    )
                    loadImagesIntoViews(imageViews, "wiki_${LINK_PORTALS}_link")
                    tempBinding
                }
                CHEST_SHOPS -> {
                    val tempBinding = FragmentWikiEntryChestShopsBinding
                        .inflate(inflater, container, false)
                    val textViews = listOf(
                        tempBinding.text1,
                        tempBinding.text2,
                        tempBinding.text3
                    )
                    loadStringsIntoViews(textViews, "wiki_$CHEST_SHOPS")

                    val imageViews = listOf(
                        tempBinding.image1,
                        tempBinding.image2,
                        tempBinding.image3,
                        tempBinding.image4
                    )
                    loadImagesIntoViews(imageViews, "wiki_${CHEST_SHOPS}_link")
                    tempBinding
                }
                COLORFALL -> {
                    val tempBinding = FragmentWikiEntryColorfallBinding
                        .inflate(inflater, container, false)
                    val textViews = listOf(
                        tempBinding.text1,
                        tempBinding.text2
                    )
                    loadStringsIntoViews(textViews, "wiki_$COLORFALL")

                    val imageViews = listOf(
                        tempBinding.image1,
                        tempBinding.image2,
                        tempBinding.image3
                    )
                    loadImagesIntoViews(imageViews, "wiki_${COLORFALL}_link")
                    tempBinding
                }
                RAID_HUB -> {
                    val tempBinding = FragmentWikiEntryRaidHubBinding
                        .inflate(inflater, container, false)
                    val textViews = listOf(
                        tempBinding.text1,
                        tempBinding.text2,
                        tempBinding.text3,
                        tempBinding.text4,
                        tempBinding.text5,
                        tempBinding.text6,
                        tempBinding.text7,
                        tempBinding.text8,
                        tempBinding.text9
                    )
                    loadStringsIntoViews(textViews, "wiki_$RAID_HUB")

                    val imageViews = listOf(
                        tempBinding.image1,
                        tempBinding.image2,
                        tempBinding.image3,
                        tempBinding.image4,
                        tempBinding.image5,
                        tempBinding.image6,
                        tempBinding.image7,
                        tempBinding.image8,
                        tempBinding.image9
                    )
                    loadImagesIntoViews(imageViews, "wiki_${RAID_HUB}_link")
                    tempBinding
                }
                else -> {
                    val stringId = myContext.resources.getIdentifier(
                        "wiki_$name",
                        "string",
                        myContext.packageName
                    )
                    val link = getString(stringId)
                    val tempBinding = FragmentWikiWebviewBinding
                        .inflate(inflater, container, false)
                    tempBinding.swipeRefreshLayout.apply {
                        setProgressBackgroundColorSchemeResource(R.color.refreshLayoutBackground)
                        setColorSchemeResources(R.color.refreshLayoutArrow)
                        viewTreeObserver.addOnScrollChangedListener {
                            tempBinding.swipeRefreshLayout.isEnabled = tempBinding.webview.scrollY == 0
                        }
                        setOnRefreshListener {
                            isRefreshing = false
                            tempBinding.webview.loadUrl(link)
                            tempBinding.animationView.visibility = View.INVISIBLE
                            tempBinding.webview.visibility = View.VISIBLE
                        }
                    }
                    tempBinding.webview.webViewClient = object : WebViewClient() {
                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            tempBinding.webview.hidePageElements()
                        }

                        override fun onReceivedError(
                            view: WebView?,
                            request: WebResourceRequest?,
                            error: WebResourceError?
                        ) {
                            super.onReceivedError(view, request, error)
                            tempBinding.webview.visibility = View.INVISIBLE
                            tempBinding.animationView.visibility = View.VISIBLE
                            tempBinding.animationView.setAnimation("no-connection.json")
                        }
                    }
                    val mySPR = requireActivity().getSharedPreferences(
                        SHARED_PREFERENCE,
                        Context.MODE_PRIVATE
                    )
                    val progressColor = when (mySPR.getString(THEME, "0")!!.toInt()) {
                        R.style.Theme_Cavedroid_Standard -> R.color.colorPrimaryInverse
                        R.style.Theme_Cavedroid_Green -> R.color.colorPrimaryInverseGreen
                        else -> R.color.colorPrimaryInverse
                    }
                    tempBinding.progressBar.setIndicatorColor(resources.getColor(progressColor))
                    tempBinding.webview.webChromeClient = object : WebChromeClient() {
                        override fun onProgressChanged(view: WebView?, newProgress: Int) {
                            tempBinding.progressBar.visibility = View.VISIBLE
                            tempBinding.progressBar.progress = newProgress

                            if (newProgress == 100) {
                                tempBinding.progressBar.visibility = View.INVISIBLE
                            }
                        }
                    }
                    tempBinding.webview.loadUrl(link)
                    tempBinding
                }
            }
            return binding.root
        }
    }

    private fun loadImage(link: String, imageView: ImageView) {
        Glide.with(myContext)
            .load(link)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .into(imageView)
    }

    private fun loadStringsIntoViews(textViews: List<TextView>, stringName: String) {
        textViews.forEachIndexed { index, textView ->
            val stringId = myContext.resources.getIdentifier(
                "$stringName${index + 1}",
                "string",
                myContext.packageName
            )
            val string = getString(stringId)
            textView.text = Html.fromHtml(Processor.process(string))
        }
    }

    private fun loadImagesIntoViews(imageViews: List<ImageView>, stringName: String) {
        imageViews.forEachIndexed { index, imageView ->
            val stringId = myContext.resources.getIdentifier(
                "$stringName${index + 1}",
                "string",
                myContext.packageName
            )
            val string = getString(stringId)
            loadImage(string, imageView)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.topbar_menu3, menu)
        menu.findItem(R.id.open_in_browser).setOnMenuItemClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(link)))
            true
        }
    }
}
