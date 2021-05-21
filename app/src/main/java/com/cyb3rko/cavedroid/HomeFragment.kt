package com.cyb3rko.cavedroid

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.forEach
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.callbacks.onPreShow
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.cyb3rko.cavedroid.databinding.FragmentHomeBinding
import com.cyb3rko.cavetaleapi.CavetaleAPI
import com.mikepenz.aboutlibraries.LibsBuilder
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private lateinit var myContext: Context

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    private val api = CavetaleAPI()
    private val args: HomeFragmentArgs by navArgs()
    private var currentName = ""
    private lateinit var topMenu: Menu
    private lateinit var myPrefs: SharedPreferences
    private lateinit var myPrefsEditor: SharedPreferences.Editor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root = binding.root
        myContext = requireContext()

        myPrefs = requireActivity().getSharedPreferences("Safe", Context.MODE_PRIVATE)
        myPrefsEditor = myPrefs.edit()

        currentName = myPrefs.getString("name", "")!!
        if (currentName != "" && args.name == "") {
            loadProfile(currentName)
        } else if (currentName != "" && args.name != "") {
            if (args.name != currentName) {
                currentName = args.name
                GlobalScope.launch {
                    while (!this@HomeFragment::topMenu.isInitialized) {}
                    activity?.runOnUiThread {
                        topMenu.forEach {
                            it.isVisible = false
                        }
                        val searchView = topMenu.findItem(R.id.search)
                        searchView.expandActionView()
                        (searchView.actionView as SearchView).setQuery(args.name, true)
                    }
                }
            } else {
                currentName = args.name
                loadProfile(currentName)
            }
        } else {
            showNameDialog()
        }

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.refreshLayout.apply {
            setProgressBackgroundColorSchemeResource(R.color.refreshLayoutBackground)
            setColorSchemeResources(R.color.refreshLayoutArrow)
            setOnRefreshListener {
                loadProfile(myPrefs.getString("name", "")!!)
            }
        }

        binding.soldContainer.setOnClickListener {
            val action = HomeFragmentDirections.openProfileCategory(1, currentName, "Items sold (highest amount)")
            findNavController().navigate(action)
        }

        binding.boughtContainer.setOnClickListener {
            val action = HomeFragmentDirections.openProfileCategory(2, currentName, "Items bought (highest amount)")
            findNavController().navigate(action)
        }

        binding.offersContainer.setOnClickListener {
            val action = HomeFragmentDirections.openProfileCategory(3, currentName, "Current offers")
            findNavController().navigate(action)
        }
    }

    private fun loadProfile(name: String) {
        val formattedName = if (!name.contains(" ")) name else name.replace(" ", "%20")
        val avatarName = if (name != "The Bank") name else "God"
        GlobalScope.launch {
            try {
                val user = api.getUser(formattedName)
                requireActivity().runOnUiThread {
                    Glide.with(this@HomeFragment)
                        .load(api.getAvatarLink(avatarName, 500))
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .listener(object: RequestListener<Drawable> {
                            override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                                Log.d("Cavedroid.Avatar", e?.message!!)
                                return false
                            }

                            override fun onResourceReady(
                                resource: Drawable?,
                                model: Any?,
                                target: Target<Drawable>?,
                                dataSource: DataSource?,
                                isFirstResource: Boolean
                            ): Boolean {
                                binding.animationView.pauseAnimation()
                                binding.animationView.visibility = View.GONE
                                binding.nameView.visibility = View.VISIBLE
                                binding.apply {
                                    nameView.text = name
                                    balanceView.text = Html.fromHtml("<b>Coins in account:</b><br/>${user.balance}")
                                    earningsView.text = Html.fromHtml("<b>Total earnings in market:</b><br/>${user.marketEarnings}")
                                    spendingView.text = Html.fromHtml("<b>Total spending in market:</b><br/>${user.marketSpendings}")
                                    soldView.text = Html.fromHtml("<b>Items sold:</b><br/>${user.itemsSold}")
                                    boughtView.text = Html.fromHtml("<b>Items bought:</b><br/>${user.itemsBought}")
                                    offersView.text = Html.fromHtml("<b>Current offers:</b><br/>${user.currentOffers}")
                                    refreshLayout.isRefreshing = false
                                }
                                showInformation(true)
                                return false
                            }
                        })
                        .into(binding.avatarView)
                }
            } catch (e: Exception) {
                Log.w("Cavedroid.Data", "${e.cause}, ${e.message!!}")
            }
        }
    }

    private fun showInformation(show: Boolean) {
        val visibility = if (show) View.VISIBLE else View.INVISIBLE
        binding.apply {
            header.visibility = visibility
            balanceContainer.visibility = visibility
            earningsContainer.visibility = visibility
            spendingContainer.visibility = visibility
            soldContainer.visibility = visibility
            boughtContainer.visibility = visibility
            offersContainer.visibility = visibility
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.topbar_menu, menu)
        topMenu = menu
        val menuSearchItem = menu.findItem(R.id.search)
        val searchView = menuSearchItem.actionView as SearchView
        searchView.queryHint = "Player Name"
        searchView.isIconifiedByDefault = false
        searchView.setOnQueryTextListener(object: SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                hideKeyboard()
                binding.animationView.playAnimation()
                binding.animationView.visibility = View.VISIBLE
                showInformation(false)
                if (query != null) {
                    loadProfile(query)
                    currentName = query
                }
                return true
            }
            override fun onQueryTextChange(newText: String?): Boolean { return false }
        })

        menuSearchItem.setOnActionExpandListener(object: MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(item: MenuItem?): Boolean {
                menu.forEach {
                    it.isVisible = false
                }
                return true
            }

            override fun onMenuItemActionCollapse(item: MenuItem?): Boolean {
                hideKeyboard()
                menu.forEach {
                    it.isVisible = true
                }
                binding.animationView.playAnimation()
                binding.animationView.visibility = View.VISIBLE
                showInformation(false)
                val oldName = myPrefs.getString("name", "")!!
                loadProfile(oldName)
                currentName = oldName
                return true
            }
        })

        menu.findItem(R.id.profile_name_dialog).setOnMenuItemClickListener {
            showNameDialog()
            true
        }

        menu.findItem(R.id.icon_credits).setOnMenuItemClickListener {
            findNavController().navigate(R.id.navigation_about_icons)
            true
        }

        menu.findItem(R.id.library_credits).setOnMenuItemClickListener {
            LibsBuilder()
                .withShowLoadingProgress(true)
                .withAboutVersionShownCode(false)
                .withAboutVersionShownName(false)
                .withAutoDetect(true)
                .withAboutIconShown(false)
                .withAboutVersionShown(false)
                .withVersionShown(true)
                .withLicenseDialog(true)
                .withLicenseShown(true)
                .withCheckCachedDetection(true)
                .withSortEnabled(true)
                .start(requireContext())
            true
        }

        menu.findItem(R.id.animation_credits).setOnMenuItemClickListener {
            findNavController().navigate(R.id.navigation_about_animations)
            true
        }

        menu.findItem(R.id.feedback).setOnMenuItemClickListener {
            MaterialDialog(requireActivity())
                .title(text = "Have some feedback?")
                .message(text = "If you have some feedback or you found a bug, feel free to report it on the GitHub repository.")
                .positiveButton(text = "Open Repo") { it2 ->
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/cyb3rko/cavedroid/issues")))
                }
                .show()
            true
        }
    }

    private fun showNameDialog() {
        val myPrefs = requireActivity().getSharedPreferences("Safe", Context.MODE_PRIVATE)
        MaterialDialog(requireActivity())
            .noAutoDismiss()
            .onPreShow { it2 ->
                val input = it2.getCustomView().findViewById<EditText>(R.id.md_input)
                input.setText(myPrefs.getString("name", ""))
            }
            .customView(R.layout.dialog_view)
            .cancelable(true)
            .title(text = "Put in your ingame name")
            .positiveButton(text = "Ok") { it3 ->
                val input = it3.getCustomView().findViewById<EditText>(R.id.md_input)
                val newName = input.text.toString()
                if (newName != "") {
                    myPrefsEditor.putString("name", newName).apply()
                    binding.animationView.playAnimation()
                    binding.animationView.visibility = View.VISIBLE
                    showInformation(false)
                    loadProfile(newName)
                    it3.cancel()
                } else {
                    input.error = "Input missing"
                }
            }
            .show()
    }

    private fun hideKeyboard() {
        val imm = requireActivity().getSystemService(AppCompatActivity.INPUT_METHOD_SERVICE) as InputMethodManager
        var view = requireActivity().currentFocus
        if (view == null) {
            view = View(requireActivity())
        }
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }
}