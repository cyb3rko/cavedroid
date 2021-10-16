package com.cyb3rko.cavedroid.rankings

sealed class OfferEntryViewState {
    data class OfferEntry(
        val item: String,
        val amount: String,
        val price: String,
        val perItem: String,
        val marketAvg: String
    ) : OfferEntryViewState()
}