package com.project.locarm.ui.search.util

import com.project.locarm.data.model.Juso
import com.project.locarm.data.model.Loc
import com.project.locarm.data.model.SelectDestination

sealed interface SelectDestinationState {
    object Idle : SelectDestinationState

    data class SelectSearchResult(
        val result: SelectDestination,
        val juso: Juso
    ) : SelectDestinationState

    data class SelectOnMap(val location: Loc) : SelectDestinationState
}
