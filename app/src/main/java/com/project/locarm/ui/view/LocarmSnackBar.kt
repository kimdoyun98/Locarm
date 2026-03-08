package com.project.locarm.ui.view

import android.app.Activity
import android.util.Log
import androidx.core.view.isVisible
import com.project.locarm.databinding.CustomSnackbarLayoutBinding

class LocarmSnackBar(
    activity: Activity,
    layoutLocation: LayoutLocation,
    private val message: String,
    private val time: Long,
) : LocarmAlarm<CustomSnackbarLayoutBinding>(
    inflate = CustomSnackbarLayoutBinding::inflate,
    activity = activity,
    layoutLocation = layoutLocation
) {
    init {
        _binding!!.snackBarContent.text = message
    }

    private var onDismissAction: (() -> Unit?)? = null

    fun setAction(text: String, onClick: () -> Unit): LocarmSnackBar {
        _binding!!.positiveButton.isVisible = true
        _binding!!.positiveButton.text = text
        _binding!!.positiveButton.setOnClickListener {
            onClick()
            dismiss()
        }

        return this
    }

    fun setNegativeAction(text: String, onClick: () -> Unit): LocarmSnackBar {
        _binding!!.negativeButton.isVisible = true
        _binding!!.negativeButton.text = text
        _binding!!.negativeButton.setOnClickListener {
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

        if (time != INDEFINITE) {
            delayDisMissAction(time = time)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        onDismissAction?.invoke()
        onDismissAction = null
        Log.e("Test", "onDestroy")
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
