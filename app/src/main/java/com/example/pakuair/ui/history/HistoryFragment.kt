package com.example.pakuair.ui.history

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
import com.example.pakuair.data.model.HasilCekAir
import com.example.pakuair.databinding.FragmentHistoryBinding
import com.example.pakuair.databinding.ItemHistoryBinding
import java.text.SimpleDateFormat
import java.util.*

class HistoryFragment : Fragment() {
    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!
    private val historyAdapter = HistoryAdapter { hasil -> navigateToDetail(hasil) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
    }
   
    // Tambahkan onResume
    override fun onResume() {
        super.onResume()
        loadHistory() // Reload data setiap kali Fragment muncul
    }

    private fun setupRecyclerView() {
        binding.historyRecyclerView.apply {
            adapter = historyAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun loadHistory() {
        binding.progressBar.visibility = View.VISIBLE
        binding.emptyText.visibility = View.GONE

        FirebaseManager.getHasilCekAir { hasilList ->
            // Pastikan Fragment masih attached ke Activity
            if (isAdded) {
                binding.progressBar.visibility = View.GONE
                
                if (hasilList.isEmpty()) {
                    binding.emptyText.visibility = View.VISIBLE
                } else {
                    historyAdapter.submitList(hasilList)
                }
            }
        }
    }

    private fun navigateToDetail(hasil: HasilCekAir) {
        findNavController().navigate(
            HistoryFragmentDirections.actionHistoryFragmentToHistoryDetailFragment(hasil.id)
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

// Adapter untuk RecyclerView
class HistoryAdapter(
    private val onClick: (HasilCekAir) -> Unit
) : ListAdapter<HasilCekAir, HistoryAdapter.ViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemHistoryBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding, onClick)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(
        private val binding: ItemHistoryBinding,
        private val onClick: (HasilCekAir) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(hasil: HasilCekAir) {
            binding.apply {
                statusIcon.setImageResource(
                    if (hasil.potability == 1) R.drawable.ic_check_success
                    else R.drawable.ic_check_failed
                )
                statusText.text = if (hasil.potability == 1) "Air Layak Minum" else "Air Tidak Layak Minum"
                timestamp.text = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale("id"))
                    .format(Date(hasil.timestamp))
                
                root.setOnClickListener { onClick(hasil) }
            }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<HasilCekAir>() {
        override fun areItemsTheSame(oldItem: HasilCekAir, newItem: HasilCekAir): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: HasilCekAir, newItem: HasilCekAir): Boolean {
            return oldItem == newItem
        }
    }
}