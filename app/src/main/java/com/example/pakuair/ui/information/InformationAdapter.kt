package com.example.pakuair.ui.information

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.pakuair.data.model.Information
import com.example.pakuair.databinding.ItemInformationBinding

class InformationAdapter(
    private val items: List<Information>,
    private val onItemClick: (Information) -> Unit
) : RecyclerView.Adapter<InformationAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemInformationBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemInformationBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.binding.apply {
            titleText.text = item.title
            previewText.text = item.preview
            root.setOnClickListener { onItemClick(item) }
        }
    }

    override fun getItemCount() = items.size
}