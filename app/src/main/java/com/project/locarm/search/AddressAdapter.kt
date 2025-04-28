package com.project.locarm.search

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.project.locarm.data.AddressDTO
import com.project.locarm.databinding.AddressSearchItemBinding

class AddressAdapter : RecyclerView.Adapter<AddressAdapter.ViewHolder>() {
    private var addressArray = ArrayList<AddressDTO.Result.Juso>()

    interface OnItemClickListener {
        fun onItemClicked(data: AddressDTO.Result.Juso)
    }

    private var itemClickListener: OnItemClickListener? = null

    fun setOnItemClickListener(listener: OnItemClickListener?) {
        itemClickListener = listener
    }

    inner class ViewHolder(
        private val binding: AddressSearchItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                val pos: Int = absoluteAdapterPosition
                itemClickListener?.onItemClicked(addressArray[pos])
            }
        }


        fun bind(data: AddressDTO.Result.Juso) {
            binding.address.text = data.name
            binding.address2.text = data.jibunAddr
        }
    }

    fun setAddress(addressData: ArrayList<AddressDTO.Result.Juso>?) {
        addressArray = addressData ?: return
        notifyDataSetChanged()
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

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(addressArray[position])
    }

    override fun getItemCount(): Int {
        return addressArray.size
    }
}
