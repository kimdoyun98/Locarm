package com.project.locarm.ui.favorite.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.project.locarm.data.room.entitiy.Favorite
import com.project.locarm.databinding.FavoritesItemBinding
import com.project.locarm.ui.favorite.FavoriteViewModel

class FavoritesAdapter(
    private val favoriteViewModel: FavoriteViewModel,
    private val lifecycleOwner: LifecycleOwner,
) : RecyclerView.Adapter<FavoritesAdapter.FavoriteViewHolder>() {
    private var list = listOf<Favorite>()

    inner class FavoriteViewHolder(
        private val binding: FavoritesItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        /**
         * Item을 xml에서 Databinding으로 ViewModel에서 직접 받게 되면 OutOfIndex 오류 발생
         * viewModel에서 직접 받을 시 List의 변경(삭제 등) 시 notifyDataSetChanged 호출보다 xml 변경이 빠름
         * 즉, bind가 호출되기 전에 xml에서 변경이 먼저 일어나기 때문에 xml에서의 list는 3개인데,
         * position은 5까지 있음 (notifyDataSetChanged가 이루어지지 않았기 때문에)
         * 따라서 OutOfIndex 오류가 발생
         */
        fun bind(position: Int) {
            binding.index = position
            binding.favorite = list[position]
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteViewHolder {
        return FavoriteViewHolder(
            FavoritesItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ).apply {
                viewModel = favoriteViewModel
                lifecycleOwner = this@FavoritesAdapter.lifecycleOwner
            }
        )
    }

    override fun onBindViewHolder(holder: FavoriteViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun setData(data: List<Favorite>) {
        list = data
        notifyDataSetChanged()
    }
}
