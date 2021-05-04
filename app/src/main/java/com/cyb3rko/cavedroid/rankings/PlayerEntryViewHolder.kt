package com.cyb3rko.cavedroid.rankings

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.cyb3rko.cavedroid.R
import me.ibrahimyilmaz.kiel.core.RecyclerViewHolder

class PlayerEntryViewHolder(view: View) : RecyclerViewHolder<PlayerViewState.PlayerEntry>(view) {
    val rankView: TextView = view.findViewById(R.id.item_rank)
    val nameView: TextView = view.findViewById(R.id.item_name)
    val dataView: TextView = view.findViewById(R.id.item_data)
    val avatarView: ImageView = view.findViewById(R.id.item_avatar)
}