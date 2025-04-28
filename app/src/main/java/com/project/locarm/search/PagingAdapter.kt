package com.project.locarm.search

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.project.locarm.data.AddressDTO
import com.project.locarm.databinding.AddressSearchItemBinding

class PagingAdapter : PagingDataAdapter<AddressDTO.Result.Juso, PagingAdapter.ViewHolder>(addressDiffUtil) {

    fun interface OnAddressSelectListener {
        fun onItemClicked(juso: AddressDTO.Result.Juso?)
    }

    private var itemClickListener: OnAddressSelectListener? = null

    fun setOnItemClickListener(listener: OnAddressSelectListener?) {
        itemClickListener = listener
    }

    inner class ViewHolder(
        private val binding: AddressSearchItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            itemView.setOnClickListener {
                itemClickListener?.onItemClicked(getItem(absoluteAdapterPosition))
            }
        }

        fun bind(juso: AddressDTO.Result.Juso) {
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
        private val addressDiffUtil = object : DiffUtil.ItemCallback<AddressDTO.Result.Juso>() {
            override fun areItemsTheSame(
                oldItem: AddressDTO.Result.Juso, newItem: AddressDTO.Result.Juso
            ): Boolean = oldItem.jibunAddr == newItem.jibunAddr

            override fun areContentsTheSame(
                oldItem: AddressDTO.Result.Juso, newItem: AddressDTO.Result.Juso
            ): Boolean = oldItem == newItem
        }
    }
}
