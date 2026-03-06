package com.project.locarm.ui.search.adapter

import androidx.appcompat.widget.AppCompatButton
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.databinding.BindingAdapter
import com.project.locarm.ui.search.util.SelectDestinationState

object BindingAdapter {

    @JvmStatic
    @BindingAdapter("app:isVisible")
    fun editDestinationButton(button: AppCompatButton, state: SelectDestinationState) {
        when (state) {
            is SelectDestinationState.Idle -> {
                button.isGone = true
            }

            else -> {
                button.isVisible = true
            }
        }
    }
}
