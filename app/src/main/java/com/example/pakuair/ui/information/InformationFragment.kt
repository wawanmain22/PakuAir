package com.example.pakuair.ui.information

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.DividerItemDecoration
import com.example.pakuair.R
import com.example.pakuair.databinding.FragmentInformationBinding
import androidx.core.os.bundleOf

class InformationFragment : Fragment() {
    private var _binding: FragmentInformationBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Binding sekarang akan menggunakan ConstraintLayout sesuai dengan layout XML
        _binding = FragmentInformationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.titleHeader.visibility = View.GONE  // Sembunyikan title
        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        val adapter = InformationAdapter(InformationData.informationList) { information ->
            findNavController().navigate(
                R.id.action_informationFragment_to_informationDetailFragment,
                bundleOf("informationId" to information.id)
            )
        }

        binding.recyclerView.apply {
            this.adapter = adapter
            layoutManager = LinearLayoutManager(context)
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}