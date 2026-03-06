package com.project.locarm.ui.main.adapter

import android.animation.AnimatorInflater
import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.graphics.PorterDuff
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.appcompat.widget.AppCompatButton
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.graphics.toColorInt
import androidx.databinding.BindingAdapter
import com.google.android.material.slider.Slider
import com.project.locarm.R
import com.project.locarm.common.MyApplication
import com.project.locarm.ui.main.ServiceState

object BindingAdapter {
    private var _animator: ObjectAnimator? = null
    private val animator get() = _animator!!

    @JvmStatic
    @BindingAdapter("app:state")
    fun trackingStateButton(view: AppCompatButton, serviceState: ServiceState) {
        when (serviceState) {
            is ServiceState.Idle -> Unit

            is ServiceState.RunService -> {
                view.text = MyApplication.instance.getString(R.string.turn_off)
                view.background = getDrawable(view.context, R.drawable.off_round_bg)
            }

            is ServiceState.StopService -> {
                view.text = MyApplication.instance.getString(R.string.turn_on)
                view.background = getDrawable(view.context, R.drawable.run_round_bg)
            }
        }
    }

    @JvmStatic
    @BindingAdapter("app:state")
    fun trackingStateLayout(view: ConstraintLayout, serviceState: ServiceState) {
        when (serviceState) {
            is ServiceState.Idle -> Unit

            is ServiceState.RunService -> {
                view.background = getDrawable(view.context, R.drawable.tracking_bg)
            }

            is ServiceState.StopService -> {
                view.background = getDrawable(view.context, R.drawable.stop_tracking_bg)
            }
        }
    }

    @JvmStatic
    @BindingAdapter("app:state")
    fun trackingStateTextView(view: TextView, serviceState: ServiceState) {
        when (serviceState) {
            is ServiceState.Idle -> Unit

            is ServiceState.RunService -> {
                view.setTextColor(ContextCompat.getColor(view.context, R.color.green))
            }

            is ServiceState.StopService -> {
                view.setTextColor(ContextCompat.getColor(view.context, R.color.black))
            }
        }
    }

    @JvmStatic
    @BindingAdapter("app:state")
    fun trackingStateImageView(view: ImageView, serviceState: ServiceState) {
        if (_animator == null) {
            _animator =
                AnimatorInflater.loadAnimator(
                    view.context,
                    R.animator.color_change
                ) as ObjectAnimator
        }

        animator.target = view
        animator.setEvaluator(ArgbEvaluator()) // 색상 보간을 위해 필수

        when (serviceState) {
            is ServiceState.Idle -> Unit

            is ServiceState.RunService -> {
                animator.start()
            }

            is ServiceState.StopService -> {
                animator.cancel()
                view.setColorFilter("#6A6A6C".toColorInt(), PorterDuff.Mode.SRC_IN)
            }
        }
    }

    @JvmStatic
    @BindingAdapter("app:change_slider", "app:state", requireAll = true)
    fun changeSlider(view: Slider, update: (Float) -> Unit, state: ServiceState) {
        view.addOnChangeListener { slider, value, fromUser ->
            update(value)
        }

        when (state) {
            is ServiceState.Idle -> Unit

            is ServiceState.StopService -> {
                view.isEnabled = true
            }

            is ServiceState.RunService -> {
                view.isEnabled = false
            }
        }
    }

    @JvmStatic
    @BindingAdapter("app:isEnabled")
    fun textEnabled(view: TextView, state: ServiceState) {
        view.isEnabled = state !is ServiceState.RunService
    }
}
