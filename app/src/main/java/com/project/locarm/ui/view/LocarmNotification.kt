package com.project.locarm.ui.view

import android.app.Activity
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.view.ViewCompat
import androidx.viewbinding.ViewBinding

typealias Inflate<T> = (LayoutInflater, ViewGroup?, Boolean) -> T

abstract class LocarmNotification<T : ViewBinding>(
    inflate: Inflate<T>,
    activity: Activity,
    private val layoutLocation: LayoutLocation
) {
    protected val rootView: FrameLayout = activity.findViewById<FrameLayout>(android.R.id.content)
    private var _binding: T? = null
    protected val binding get() = _binding!!
    protected val params: FrameLayout.LayoutParams

    init {
        _binding = inflate.invoke(activity.layoutInflater, rootView, false)

        params = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            binding.root.layoutParams.height
        ).apply {
            gravity = when (layoutLocation) {
                LayoutLocation.TOP -> Gravity.TOP
                LayoutLocation.BOTTOM -> Gravity.BOTTOM
            }
            setMargins(
                DEFAULT_PADDING,
                when (layoutLocation) {
                    LayoutLocation.TOP -> TOP_PADDING
                    LayoutLocation.BOTTOM -> DEFAULT_PADDING
                },
                DEFAULT_PADDING,
                DEFAULT_PADDING
            )
        }
    }

    open fun show() {
        rootView.addView(binding.root, params)
    }

    protected open fun dismiss() {
        rootView.removeView(binding.root)

        onDestroy()
    }

    protected open fun dismissAnimation(
        endAction: () -> Unit = {},
    ) {
        binding.root.animate()
            .alpha(0f) // 투명하게
            .translationY(layoutLocation.value)
            .setDuration(300) // 0.3초 동안
            .withEndAction { // 애니메이션이 완전히 끝난 후 실행
                dismiss()
                endAction()
            }
            .start()
    }

    protected fun showAnimation() {
        when (layoutLocation) {
            LayoutLocation.TOP -> {
                ViewCompat.setZ(binding.root, 100f)

                // 3. 새 알림 등장 애니메이션 (위에서 아래로)
                binding.root.translationY = -300f
                binding.root.alpha = 0f
                binding.root.animate()
                    .translationY(0f)
                    .alpha(1f)
                    .setDuration(600)
                    .start()
            }

            LayoutLocation.BOTTOM -> {
                binding.root.translationY = 500f
                binding.root.animate()
                    .translationY(0f)
                    .setDuration(300)
                    .start()
            }
        }
    }

    protected fun bindingIsNotNull(): Boolean {
        return _binding != null
    }

    protected fun delayDisMissAction(
        time: Long = DEFAULT_TIME,
        action: (() -> Unit)? = null,
    ) {
        binding.root.postDelayed({
            if (action != null) action()
            // 아직 부모 뷰에 붙어있는지 확인 후 제거 (중복 제거 방지)
            if (bindingIsNotNull()) {
                dismissAnimation()
            }
        }, time)
    }

    protected open fun onDestroy() {
        _binding = null
    }

    enum class LayoutLocation(val value: Float) {
        TOP(-200f), BOTTOM(200f)
    }

    companion object {
        private const val DEFAULT_PADDING = 16
        private const val TOP_PADDING = 48
        private const val DEFAULT_TIME = 3000L
    }
}
