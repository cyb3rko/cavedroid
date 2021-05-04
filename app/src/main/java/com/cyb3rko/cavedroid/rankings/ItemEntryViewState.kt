package com.cyb3rko.cavedroid.rankings

sealed class ItemViewState {
    data class ItemEntry(
        val name: String,
        val amount: String,
        val turnover: String
    ) : ItemViewState()
}