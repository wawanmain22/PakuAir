package com.example.pakuair.ui.depot

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.pakuair.R
import com.example.pakuair.data.FirebaseManager
import com.example.pakuair.data.model.HasilCekAir
import com.example.pakuair.data.model.Toko
import com.example.pakuair.databinding.FragmentDepotDetailBinding
import java.text.SimpleDateFormat
import java.util.*

class DepotDetailFragment : Fragment() {
    private var _binding: FragmentDepotDetailBinding? = null
    private val binding get() = _binding!!
    
    // Explicitly define the type
    private val safeArgs: DepotDetailFragmentArgs by navArgs<DepotDetailFragmentArgs>()
    private val tokoId: String by lazy { safeArgs.tokoId }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDepotDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadDepotDetail()
    }

    override fun onResume() {
        super.onResume()
        loadDepotDetail() // Reload data setiap kali Fragment muncul
    }

    private fun loadDepotDetail() {
        Log.d("DepotDetailFragment", "Loading detail for toko: $tokoId")
        
        FirebaseManager.getToko(tokoId) { toko ->
            if (!isAdded) return@getToko
            
            Log.d("DepotDetailFragment", "Got toko data: ${toko?.namaToko}")
            
            if (toko != null) {
                setupDepotInfo(toko)
                loadLastCheck(toko.userId)
            } else {
                Log.e("DepotDetailFragment", "Failed to load toko data")
                // Optional: Show error message to user
                findNavController().navigateUp()
            }
        }
    }

    private fun setupDepotInfo(toko: Toko) {
        binding.apply {
            depotName.text = toko.namaToko
            depotAddress.text = toko.alamatToko
            depotPhone.text = "Telp: ${toko.kontakToko}"

        }
    }

    private fun loadLastCheck(userId: String) {
        Log.d("DepotDetailFragment", "Loading last check for user: $userId")
        
        FirebaseManager.addHasilCekAirListener(userId) { hasil ->
            if (!isAdded) return@addHasilCekAirListener
            
            Log.d("DepotDetailFragment", "Got hasil: ${hasil?.potability}")
            
            if (hasil != null) {
                setupLastCheck(hasil)
            } else {
                Log.e("DepotDetailFragment", "No hasil found")
                // Handle no hasil case
            }
        }
    }

    private fun setupLastCheck(hasil: HasilCekAir) {
        binding.apply {
            lastCheckDate.text = SimpleDateFormat(
                "dd MMMM yyyy, HH:mm",
                Locale("id")
            ).format(Date(hasil.timestamp))
            
            lastCheckResult.text = if (hasil.potability == 1) {
                "Air Layak Minum"
            } else {
                "Air Tidak Layak Minum"
            }
            lastCheckResult.setTextColor(
                resources.getColor(
                    if (hasil.potability == 1) R.color.success_text
                    else R.color.error_text,
                    null
                )
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}