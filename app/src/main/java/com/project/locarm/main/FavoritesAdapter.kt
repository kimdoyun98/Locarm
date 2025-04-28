package com.project.locarm.main

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.project.locarm.databinding.FavoritesItemBinding
import com.project.locarm.data.room.Favorite

class FavoritesAdapter : RecyclerView.Adapter<FavoritesAdapter.ViewHolder>() {
    private var list = ArrayList<Favorite>()

    interface OnItemClickListener {
        fun onItemClicked(data: Favorite)
        fun onDeleteClicked(data: Favorite)
    }

    private var itemClickListener: FavoritesAdapter.OnItemClickListener? = null

    inner class ViewHolder(
        private val binding: FavoritesItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.favoriteChip.setOnClickListener {
                itemClickListener?.onItemClicked(list[absoluteAdapterPosition])
            }

            binding.favoriteChip.setOnCloseIconClickListener {
                itemClickListener?.onDeleteClicked(list[absoluteAdapterPosition])
            }
        }

        fun bind(data: Favorite) {
            binding.favoriteChip.text = data.name
        }
    }

    fun setOnItemClickListener(listener: FavoritesAdapter.OnItemClickListener?) {
        itemClickListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoritesAdapter.ViewHolder {
        return ViewHolder(
            FavoritesItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun setData(data: List<Favorite>) {
        list = data.toCollection(ArrayList<Favorite>())
        notifyDataSetChanged()
    }
}
