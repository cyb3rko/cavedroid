package com.cyb3rko.cavedroid.rankings

sealed class MarketViewState {
    data class MarketEntry(
        val player: String,
        val item: String,
        val amount: String,
        val price: String,
        val perItem: String,
        val marketAvg: String
    ) : MarketViewState()
}