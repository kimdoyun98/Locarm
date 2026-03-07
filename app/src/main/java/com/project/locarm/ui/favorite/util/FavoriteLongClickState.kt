package com.project.locarm.ui.favorite.util

sealed interface FavoriteLongClickState {
    object Idle: FavoriteLongClickState

    object LongClickState: FavoriteLongClickState
}
