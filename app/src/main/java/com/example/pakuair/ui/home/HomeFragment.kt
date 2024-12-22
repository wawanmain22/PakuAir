package com.example.pakuair.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.pakuair.databinding.FragmentHomeBinding
import com.example.pakuair.data.FirebaseManager

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
                // TODO: Navigate to check quality screen
            }

            historyCard.setOnClickListener {
                // TODO: Navigate to history screen
            }

            infoCard.setOnClickListener {
                // TODO: Navigate to information screen
            }

            depotCard.setOnClickListener {
                // TODO: Navigate to depot screen
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}