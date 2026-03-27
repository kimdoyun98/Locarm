package com.project.locarm.ui.search.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.project.locarm.data.model.Juso
import com.project.locarm.databinding.AddressSearchItemBinding

class PagingAdapter(
    private val onItemClick: (Juso?) -> Unit
) : PagingDataAdapter<Juso, PagingAdapter.ViewHolder>(addressDiffUtil) {

    inner class ViewHolder(
        private val binding: AddressSearchItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            itemView.setOnClickListener {
                onItemClick(getItem(absoluteAdapterPosition))
            }
        }

        fun bind(juso: Juso) {
            binding.address.text = juso.name
            binding.address2.text = juso.jibunAddr
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position)!!)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            AddressSearchItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    companion object {
        private val addressDiffUtil = object : DiffUtil.ItemCallback<Juso>() {
            override fun areItemsTheSame(
                oldItem: Juso, newItem: Juso
            ): Boolean = oldItem.jibunAddr == newItem.jibunAddr

            override fun areContentsTheSame(
                oldItem: Juso, newItem: Juso
            ): Boolean = oldItem == newItem
        }
    }
}
