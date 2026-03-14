package com.project.locarm.ui.favorite.adapter

import android.view.View
import android.widget.RadioButton
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.databinding.BindingAdapter
import com.project.locarm.R
import com.project.locarm.common.MyApplication
import com.project.locarm.common.permission.LocarmPermission
import com.project.locarm.data.model.SelectDestination
import com.project.locarm.data.room.Favorite
import com.project.locarm.location.GeoCoder
import com.project.locarm.ui.favorite.util.FavoriteLongClickState

object BindingAdapter {

    @JvmStatic
    @BindingAdapter("app:favorite_state")
    fun longClickState(view: View, state: FavoriteLongClickState) {
        view.visibility = when (state) {
            is FavoriteLongClickState.Idle -> if (view is RadioButton) View.GONE else View.INVISIBLE

            is FavoriteLongClickState.LongClickState -> View.VISIBLE
        }
    }

    @JvmStatic
    @BindingAdapter("app:distance")
    fun calculateDistance(view: TextView, favorite: Favorite?) {
        val appContainer = MyApplication.instance.container
        if ((!LocarmPermission.checkLocationPermission(view.context) ||
                    !appContainer.locationObserver.isLocationEnabled()) || favorite == null
        ) {
            return
        }

        val realTimeLocation = appContainer.realTimeLocation

        realTimeLocation.currentLocation()?.addOnSuccessListener {
            view.text = MyApplication.instance.getString(
                R.string.remaining_distance_value,
                GeoCoder.getDistanceKmToString(
                    realTimeLocation.getDistance(
                        it.latitude,
                        it.longitude,
                        SelectDestination(
                            name = favorite.name,
                            latitude = favorite.latitude,
                            longitude = favorite.longitude
                        )
                    )
                )
            )
        }
    }

    @JvmStatic
    @BindingAdapter("app:is_visible")
    fun deleteButtonVisible(view: AppCompatButton, list: List<Int>) {
        view.visibility = if (list.isEmpty()) View.INVISIBLE else View.VISIBLE
    }
}
