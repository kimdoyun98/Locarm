package com.project.locarm.ui.main.adapter

import androidx.appcompat.widget.AppCompatButton
import androidx.databinding.BindingAdapter
import com.project.locarm.R
import com.project.locarm.common.MyApplication
import com.project.locarm.ui.main.ServiceState

object BindingAdapter {

    @JvmStatic
    @BindingAdapter("alarmText")
    fun alarmButtonText(view: AppCompatButton, serviceState: ServiceState) {
        when (serviceState) {
            is ServiceState.Idle -> {

            }

            is ServiceState.RunService -> {
                view.text = MyApplication.instance.getString(R.string.turn_off)
            }

            is ServiceState.StopService -> {
                view.text = MyApplication.instance.getString(R.string.turn_on)
            }
        }
    }
}
