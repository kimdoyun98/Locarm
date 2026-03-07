package com.project.locarm.ui.view

import android.app.Activity
import android.view.Gravity
import android.widget.FrameLayout
import androidx.core.view.isVisible
import com.project.locarm.databinding.CustomSnackbarLayoutBinding

class LocarmSnackBar(
    private val rootView: FrameLayout,
    binding: CustomSnackbarLayoutBinding,
    private val message: String,
    private val time: Long,
) {
    private var _binding: CustomSnackbarLayoutBinding? = null
    private val binding get() = _binding!!

    init {
        this._binding = binding
    }

    private val params = FrameLayout.LayoutParams(
        FrameLayout.LayoutParams.MATCH_PARENT,
        binding.root.layoutParams.height
    ).apply {
        gravity = Gravity.BOTTOM
        setMargins(16, 16, 16, 16)
    }

    private var onDismissAction: (() -> Unit?)? = null

    fun setAction(text: String, onClick: () -> Unit): LocarmSnackBar {
        _binding!!.positiveButton.isVisible = true
        _binding!!.positiveButton.text = text
        _binding!!.positiveButton.setOnClickListener {
            onClick()
            onDisMiss()
        }

        return this
    }

    fun setNegativeAction(text: String, onClick: () -> Unit): LocarmSnackBar {
        _binding!!.negativeButton.isVisible = true
        _binding!!.negativeButton.text = text
        _binding!!.negativeButton.setOnClickListener {
            onClick()
            onDisMiss()
        }

        return this
    }

    fun setDisMissAction(action: () -> Unit): LocarmSnackBar {
        this.onDismissAction = action

        return this
    }

    fun show() {
        binding.snackBarContent.text = message
        // 4. 루트 뷰에 추가
        rootView.addView(binding.root, params)

        // (옵션) 애니메이션 추가
        binding.root.translationY = 500f
        binding.root.animate()
            .translationY(0f)
            .setDuration(300)
            .start()

        if (time != INDEFINITE) {
            binding.root.postDelayed({
                // 아직 부모 뷰에 붙어있는지 확인 후 제거 (중복 제거 방지)
                if (_binding != null) {
                    onDisMissAnimation()
                }
            }, time)
        }
    }

    fun onDisMiss() {
        rootView.removeView(binding.root)
        onDismissAction?.invoke()
        _binding = null
        onDismissAction = null
    }

    private fun onDisMissAnimation() {
        binding.root.animate()
            .alpha(0f)                // 투명하게
            .translationY(200f)       // 아래로 200px 이동
            .setDuration(300)         // 0.3초 동안
            .withEndAction {          // 애니메이션이 완전히 끝난 후 실행
                onDisMiss()
            }
            .start()
    }

    companion object {
        const val SHORT = 3000L
        const val LONG = 10000L
        const val INDEFINITE = -1L

        fun make(activity: Activity, message: String, time: Long): LocarmSnackBar {
            val rootView = activity.findViewById<FrameLayout>(android.R.id.content)

            val binding =
                CustomSnackbarLayoutBinding.inflate(activity.layoutInflater, rootView, false)
            binding.snackBarContent.text = message

            return LocarmSnackBar(
                rootView = rootView,
                binding = binding,
                message = message,
                time = time
            )
        }
    }
}
