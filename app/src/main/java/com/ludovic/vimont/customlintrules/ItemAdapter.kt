package com.ludovic.vimont.customlintrules

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.ludovic.vimont.customlintrules.databinding.FragmentContentRecyclerViewItemBinding

class ItemAdapter(private val items: List<Pair<Int, String>>): RecyclerView.Adapter<ItemAdapter.ItemViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        return ItemViewHolder(FragmentContentRecyclerViewItemBinding.inflate(LayoutInflater.from(parent.context),  parent, false))
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    class ItemViewHolder(private val binding: FragmentContentRecyclerViewItemBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Pair<Int,String>) {
            binding.imageViewAvatar.setImageDrawable(ContextCompat.getDrawable(itemView.context, item.first))
            binding.textViewTitle.text = item.second
        }
    }
}
