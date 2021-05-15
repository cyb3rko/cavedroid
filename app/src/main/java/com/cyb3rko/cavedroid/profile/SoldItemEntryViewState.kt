package com.cyb3rko.cavedroid.profile

sealed class SoldItemViewState {
    data class SoldItemEntry(
        val item: String,
        val amount: String,
        val price: String,
        val buyer: String
    ) : SoldItemViewState()
}