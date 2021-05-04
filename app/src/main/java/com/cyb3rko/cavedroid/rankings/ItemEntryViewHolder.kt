package com.cyb3rko.cavedroid.rankings

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.cyb3rko.cavedroid.R
import me.ibrahimyilmaz.kiel.core.RecyclerViewHolder

class ItemEntryViewHolder(view: View) : RecyclerViewHolder<ItemViewState.ItemEntry>(view) {
    val rankView: TextView = view.findViewById(R.id.item_rank)
    val nameView: TextView = view.findViewById(R.id.item_name)
    val amountView: TextView = view.findViewById(R.id.item_amount)
    val turnoverView: TextView = view.findViewById(R.id.item_turnover)
    val avatarView: ImageView = view.findViewById(R.id.item_avatar)
}