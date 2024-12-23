package com.example.pakuair.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.pakuair.databinding.FragmentHomeBinding
import com.example.pakuair.data.FirebaseManager
import androidx.navigation.fragment.findNavController
import com.example.pakuair.R

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        setupClickListeners()
    }

    private fun setupUI() {
        FirebaseManager.getCurrentUser()?.let { firebaseUser ->
            // Get user data from database using the UID
            FirebaseManager.getUser(firebaseUser.uid) { user ->
                // Update UI on main thread
                activity?.runOnUiThread {
                    if (user != null) {
                        binding.welcomeText.text = "Halo, ${user.username}"
                    }
                }
            }
        }
    }

    private fun setupClickListeners() {
        binding.apply {
            checkQualityCard.setOnClickListener {
                findNavController().navigate(R.id.action_nav_home_to_cekAirFragment)
            }

            historyCard.setOnClickListener {
                findNavController().navigate(R.id.action_nav_home_to_historyFragment)
            }

            infoCard.setOnClickListener {
                findNavController().navigate(R.id.action_nav_home_to_informationFragment)
            }

            depotCard.setOnClickListener {
                findNavController().navigate(R.id.action_nav_home_to_nav_depot)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}