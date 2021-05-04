package com.cyb3rko.cavedroid.rankings

sealed class PlayerViewState {
    data class PlayerEntry(
        val name: String,
        val data: String
    ) : PlayerViewState()
}