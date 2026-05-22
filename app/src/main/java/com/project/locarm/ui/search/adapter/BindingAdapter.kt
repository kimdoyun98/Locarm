package com.project.locarm.ui.search.adapter

import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.databinding.BindingAdapter
import com.project.locarm.R
import com.project.locarm.ui.search.util.SelectDestinationState

object BindingAdapter {

    @JvmStatic
    @BindingAdapter("app:isVisible")
    fun editDestinationButton(button: AppCompatButton, state: SelectDestinationState) {
        when (state) {
            is SelectDestinationState.SelectOnMap -> button.isVisible = true
            else -> button.isGone = true
        }
    }

    @JvmStatic
    @BindingAdapter("app:isVisible")
    fun editDestinationLayout(layout: ConstraintLayout, state: SelectDestinationState) {
        when (state) {
            is SelectDestinationState.SelectSearchResult -> layout.isVisible = true
            else -> layout.isGone = true
        }
    }

    @JvmStatic
    @BindingAdapter("app:destination_info")
    fun editDestinationText(text: TextView, state: SelectDestinationState) {
        if (state is SelectDestinationState.SelectSearchResult) {
            when (text.id) {
                R.id.destination -> text.text = state.result.name
                R.id.selected_address -> text.text = state.juso.jibunAddr
            }
        }
    }
}
