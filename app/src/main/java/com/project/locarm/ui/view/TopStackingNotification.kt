package com.project.locarm.ui.view

import android.app.Activity
import android.view.View
import androidx.core.view.ViewCompat
import com.project.locarm.databinding.TopStackingNotificationLayoutBinding

class TopStackingNotification(
    activity: Activity,
    layoutLocation: LayoutLocation,
    message: String,
) : LocarmNotification<TopStackingNotificationLayoutBinding>(
    inflate = TopStackingNotificationLayoutBinding::inflate,
    activity = activity,
    layoutLocation = layoutLocation
) {
    init {
        binding.notificationMessage.text = message
    }

    override fun show() {
        shiftExistingNotifications()
        super.show()

        notificationStack.add(0, binding.root) // 리스트 맨 앞에 추가
        if (notificationStack.size > 3) {
            val removeView = notificationStack[3]
            rootView.removeView(removeView)
            notificationStack.remove(removeView)
        }

        ViewCompat.setZ(binding.root, 100f)

        // 3. 새 알림 등장 애니메이션 (위에서 아래로)
        binding.root.translationY = -300f
        binding.root.alpha = 0f
        binding.root.animate()
            .translationY(0f)
            .alpha(1f)
            .setDuration(600)
            .start()

        delayDisMissAction {
            if (!notificationStack.contains(binding.root)) return@delayDisMissAction
        }
    }

    override fun dismissAnimation(endAction: () -> Unit) {
        if (!notificationStack.contains(binding.root)) return

        super.dismissAnimation {
            if (bindingIsNotNull()) {
                notificationStack.remove(binding.root)
            }

            realignStack()
        }
    }

    private fun shiftExistingNotifications() {
        val yGap = 40f      // 계단식으로 내려올 간격 (px)
        val scaleGap = 0.05f // 뒤로 갈수록 작아질 비율 (5%)

        // 기존에 떠 있는 알림들을 순회하며 위치 조정
        for (i in 0 until notificationStack.size) {
            val view = notificationStack[i]
            val depth = i + 1 // 1번째 뒤, 2번째 뒤...

            // 뒤로 밀려나는 애니메이션
            view.animate()
                .translationY(yGap * depth)      // 아래로 이동
                .scaleX(1f - (scaleGap * depth)) // 가로 크기 축소
                .scaleY(1f - (scaleGap * depth)) // 세로 크기 축소
                //.alpha(1f - (alphaGap * depth))  // 투명도 조절
                .setDuration(400)
                .start()

            // 너무 많이 겹치면 맨 뒤는 안보이게 처리 (최대 3~4개 권장)
            ViewCompat.setZ(view, (100 - depth).toFloat())
            if (depth >= 3) {
                view.animate().alpha(0f).setDuration(200).start()
            }
        }
    }

    private fun realignStack() {
        // 남은 알림들을 다시 정렬
        for (i in 0 until notificationStack.size) {
            val view = notificationStack[i]
            view.animate()
                .translationY(40f * i)
                .scaleX(1f - (0.05f * i))
                .scaleY(1f - (0.05f * i))
                .alpha(if (i < 3) 1f - (0.2f * i) else 0f)
                .setDuration(300)
                .start()
        }
    }

    companion object {
        private val notificationStack = mutableListOf<View>()
        fun make(activity: Activity, message: String): TopStackingNotification {

            return TopStackingNotification(
                activity = activity,
                layoutLocation = LayoutLocation.TOP,
                message = message
            )
        }
    }
}
