package com.cyb3rko.cavedroid.rankings

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.cyb3rko.cavedroid.R
import me.ibrahimyilmaz.kiel.core.RecyclerViewHolder

class OfferEntryViewHolder(view: View) : RecyclerViewHolder<OfferEntryViewState.OfferEntry>(view) {
    val cardView: CardView = view.findViewById(R.id.item_card)
    val amountView: TextView = view.findViewById(R.id.item_amount)
    val priceView: TextView = view.findViewById(R.id.item_price)
    val iconView: ImageView = view.findViewById(R.id.item_icon)
}