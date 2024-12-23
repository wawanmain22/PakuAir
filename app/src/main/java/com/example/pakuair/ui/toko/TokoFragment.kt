package com.example.pakuair.ui.toko

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.pakuair.R
import com.example.pakuair.data.FirebaseManager
import com.example.pakuair.data.model.Toko
import com.example.pakuair.databinding.FragmentTokoBinding

class TokoFragment : Fragment() {
    private var _binding: FragmentTokoBinding? = null
    private val binding get() = _binding!!
    private var currentToko: Toko? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTokoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadTokoData()
        setupClickListeners()
    }

    private fun loadTokoData() {
        val currentUser = FirebaseManager.getCurrentUser()
        if (currentUser != null) {
            FirebaseManager.getTokoByUser(currentUser.uid) { tokoList ->
                activity?.runOnUiThread {
                    if (tokoList.isNotEmpty()) {
                        // Simpan toko pertama ke currentToko
                        currentToko = tokoList[0]
                        // Pastikan currentToko tidak null sebelum menampilkan data
                        currentToko?.let { toko ->
                            showTokoData(toko)
                            binding.apply {
                                noTokoState.visibility = View.GONE
                                storeIcon.visibility = View.VISIBLE
                                tokoState.visibility = View.VISIBLE
                                editButton.visibility = View.VISIBLE
                            }
                        }
                    } else {
                        binding.apply {
                            noTokoState.visibility = View.VISIBLE
                            storeIcon.visibility = View.GONE
                            tokoState.visibility = View.GONE
                            editButton.visibility = View.GONE
                        }
                    }
                }
            }
        }
    }

    private fun showTokoData(toko: Toko) {
        binding.apply {
            tokoName.text = toko.namaToko
            tokoAddress.text = toko.alamatToko
            tokoContact.text = toko.kontakToko
            tokoDesc.text = if (toko.deskripsiToko.isNotEmpty()) toko.deskripsiToko else "-"
        }
    }

    private fun setupClickListeners() {
        binding.apply {
            createTokoButton.setOnClickListener {
                findNavController().navigate(
                    R.id.action_nav_toko_to_tokoFormFragment
                )
            }

            editButton.setOnClickListener {
                currentToko?.let { toko ->
                    findNavController().navigate(
                        R.id.action_nav_toko_to_tokoFormFragment,
                        Bundle().apply {
                            putString("tokoId", toko.id)
                        }
                    )
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 