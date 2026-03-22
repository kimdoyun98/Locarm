package com.project.locarm.ui.view

import android.app.Activity
import androidx.core.view.isVisible
import com.project.locarm.databinding.CustomSnackbarLayoutBinding

class LocarmSnackBar(
    activity: Activity,
    layoutLocation: LayoutLocation,
    private val message: String,
    private val time: Long,
) : LocarmNotification<CustomSnackbarLayoutBinding>(
    inflate = CustomSnackbarLayoutBinding::inflate,
    activity = activity,
    layoutLocation = layoutLocation
) {
    init {
        binding.snackBarContent.text = message
    }

    private var onDismissAction: (() -> Unit?)? = null

    fun setAction(text: String, onClick: () -> Unit): LocarmSnackBar {
        binding.positiveButton.isVisible = true
        binding.positiveButton.text = text
        binding.positiveButton.setOnClickListener {
            onClick()
            dismiss()
        }

        return this
    }

    fun setNegativeAction(text: String, onClick: () -> Unit): LocarmSnackBar {
        binding.negativeButton.isVisible = true
        binding.negativeButton.text = text
        binding.negativeButton.setOnClickListener {
            onClick()
            dismiss()
        }

        return this
    }

    fun setDisMissAction(action: () -> Unit): LocarmSnackBar {
        this.onDismissAction = action

        return this
    }

    override fun show() {
        super.show()

        showAnimation()

        if (time != INDEFINITE) {
            delayDisMissAction(time = time)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        onDismissAction?.invoke()
        onDismissAction = null
    }

    companion object {
        const val SHORT = 3000L
        const val LONG = 10000L
        const val INDEFINITE = -1L

        fun make(activity: Activity, message: String, time: Long): LocarmSnackBar {

            return LocarmSnackBar(
                activity = activity,
                layoutLocation = LayoutLocation.BOTTOM,
                message = message,
                time = time
            )
        }
    }
}
