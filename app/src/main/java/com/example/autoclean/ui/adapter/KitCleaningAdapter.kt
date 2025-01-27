package com.example.autoclean.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.autoclean.R
import com.example.autoclean.data.model.ItemKitCleaning
import com.example.autoclean.databinding.ItemKitCleaningBinding


class KitCleaningAdapter(
    private val itemList: List<ItemKitCleaning>
): RecyclerView.Adapter<KitCleaningAdapter.MyViewHolder>() {

    private val expandedItems = mutableSetOf<Int>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(ItemKitCleaningBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        )
    }

    override fun getItemCount() = itemList.size


    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val item = itemList[position]

        holder.binding.textTitle.text = item.title

        val columnText = item.description.chunked(6) { it.joinToString("\n") }

        holder.binding.textDescription.text = columnText.joinToString("     ")

        val isExpanded = expandedItems.contains(position)
        holder.binding.textDescription.visibility = if (isExpanded) View.VISIBLE else View.GONE
        holder.binding.btnState.setImageResource(
            if (isExpanded) R.drawable.ic_arrow_down else R.drawable.ic_arrow_up
        )


        holder.binding.btnState.setOnClickListener {
            if (isExpanded) {
                expandedItems.remove(position)
            } else {
                expandedItems.add(position)
            }
            notifyItemChanged(position)
        }
    }

    inner class MyViewHolder(val binding: ItemKitCleaningBinding) :
        RecyclerView.ViewHolder(binding.root)

}