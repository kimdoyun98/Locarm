package com.project.locarm.main

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.project.locarm.databinding.FavoritesItemBinding
import com.project.locarm.room.Favorite

class FavoritesAdapter : RecyclerView.Adapter<FavoritesAdapter.ViewHolder>(){
    private lateinit var binding : FavoritesItemBinding
    private var list = ArrayList<Favorite>()

    interface OnItemClickListener {
        fun onItemClicked(data: Favorite)
        fun onDeleteClicked(data: Favorite)
    }

    // OnItemClickListener 참조 변수 선언
    private var itemClickListener: FavoritesAdapter.OnItemClickListener? = null

    // OnItemClickListener 전달 메소드
    fun setOnItemClickListener(listener: FavoritesAdapter.OnItemClickListener?) {
        itemClickListener = listener
    }

    inner class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        init {
            binding.name.setOnClickListener {
                itemClickListener?.onItemClicked(list[adapterPosition])
            }
            binding.delete.setOnClickListener {
                itemClickListener?.onDeleteClicked(list[adapterPosition])
            }
        }

        fun bind(data: Favorite){
            binding.name.text = data.name
        }
    }

    fun setData(data: List<Favorite>){
        list = data.toCollection(ArrayList<Favorite>())
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoritesAdapter.ViewHolder {
        binding = FavoritesItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding.root)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount(): Int {
        return list.size
    }
}