package com.example.pakuair.ui.depot

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.pakuair.R
import com.example.pakuair.data.FirebaseManager
import com.example.pakuair.data.HasilCekAir
import com.example.pakuair.data.model.Toko
import com.example.pakuair.databinding.FragmentDepotBinding
import com.example.pakuair.databinding.ItemDepotBinding
import java.text.SimpleDateFormat
import java.util.*

class DepotFragment : Fragment() {
    private var _binding: FragmentDepotBinding? = null
    private val binding get() = _binding!!
    private val depotAdapter = DepotAdapter { toko -> navigateToDetail(toko) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDepotBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
    }

    override fun onResume() {
        super.onResume()
        loadDepots()
    }

    private fun setupRecyclerView() {
        binding.depotRecyclerView.apply {
            adapter = depotAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun loadDepots() {
        binding.progressBar.visibility = View.VISIBLE
        binding.emptyText.visibility = View.GONE

        FirebaseManager.getDepotWithGoodWater { depotList ->
            if (!isAdded) return@getDepotWithGoodWater

            binding.progressBar.visibility = View.GONE

            if (depotList.isEmpty()) {
                binding.emptyText.visibility = View.VISIBLE
            } else {
                depotAdapter.submitList(depotList)
            }
        }
    }

    private fun navigateToDetail(toko: Toko) {
        findNavController().navigate(
            DepotFragmentDirections.actionNavDepotToDepotDetailFragment(toko.id)
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

class DepotAdapter(
    private val onClick: (Toko) -> Unit
) : ListAdapter<Pair<Toko, HasilCekAir>, DepotAdapter.ViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemDepotBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding, onClick)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(
        private val binding: ItemDepotBinding,
        private val onClick: (Toko) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(pair: Pair<Toko, HasilCekAir>) {
            val (toko, hasil) = pair
            binding.apply {
                depotName.text = toko.namaToko
                depotAddress.text = toko.alamatToko
                lastCheckChip.text = "Terakhir diperiksa: ${
                    SimpleDateFormat("dd MMM yyyy", Locale("id"))
                        .format(Date(hasil.timestamp))
                }"

                root.setOnClickListener { onClick(toko) }
            }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<Pair<Toko, HasilCekAir>>() {
        override fun areItemsTheSame(oldItem: Pair<Toko, HasilCekAir>, newItem: Pair<Toko, HasilCekAir>): Boolean {
            return oldItem.first.id == newItem.first.id
        }

        override fun areContentsTheSame(oldItem: Pair<Toko, HasilCekAir>, newItem: Pair<Toko, HasilCekAir>): Boolean {
            return oldItem == newItem
        }
    }
}