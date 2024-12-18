package com.example.pakuair

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class DepotAdapter(private val depotList: List<DepotItem>) : 
    RecyclerView.Adapter<DepotAdapter.DepotViewHolder>() {

    private var expandedPosition = -1

    class DepotViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val namaDepot: TextView = view.findViewById(R.id.tvNamaDepot)
        val kualitasAir: TextView = view.findViewById(R.id.tvKualitasAir)
        val alamat: TextView = view.findViewById(R.id.tvAlamat)
        val kontak: TextView = view.findViewById(R.id.tvKontak)
        val headerLayout: LinearLayout = view.findViewById(R.id.headerLayout)
        val expandableLayout: LinearLayout = view.findViewById(R.id.expandableLayout)
        val expandIcon: ImageView = view.findViewById(R.id.expandIcon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DepotViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_depot, parent, false)
        return DepotViewHolder(view)
    }

    override fun onBindViewHolder(holder: DepotViewHolder, position: Int) {
        val depot = depotList[position]
        holder.namaDepot.text = depot.nama
        holder.kualitasAir.text = "Kualitas Air: ${depot.kualitas}"
        holder.alamat.text = depot.alamat
        holder.kontak.text = depot.kontak

        val isExpanded = position == expandedPosition
        holder.expandableLayout.visibility = if (isExpanded) View.VISIBLE else View.GONE
        holder.expandIcon.rotation = if (isExpanded) 180f else 0f

        holder.headerLayout.setOnClickListener {
            val shouldExpand = expandedPosition != position
            
            // Collapse previously expanded item
            if (expandedPosition >= 0 && expandedPosition != position) {
                notifyItemChanged(expandedPosition)
            }

            expandedPosition = if (shouldExpand) position else -1
            
            // Animate rotation
            val anim = RotateAnimation(
                if (shouldExpand) 0f else 180f,
                if (shouldExpand) 180f else 0f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f
            ).apply {
                duration = 300
                fillAfter = true
            }
            holder.expandIcon.startAnimation(anim)

            // Show/hide content
            holder.expandableLayout.visibility = if (shouldExpand) View.VISIBLE else View.GONE
        }
    }

    override fun getItemCount() = depotList.size
}

data class DepotItem(
    val nama: String,
    val kualitas: String,
    val alamat: String = "Jln Pramuka, No 08",
    val kontak: String = "081978899009"
) 