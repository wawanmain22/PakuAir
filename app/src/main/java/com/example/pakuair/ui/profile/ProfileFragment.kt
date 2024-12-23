package com.example.pakuair.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import cn.pedant.SweetAlert.SweetAlertDialog
import com.example.pakuair.R
import com.example.pakuair.data.FirebaseManager
import com.example.pakuair.databinding.FragmentProfileBinding
import androidx.core.view.MenuProvider
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem

class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private var loadingDialog: SweetAlertDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Tambahkan MenuProvider
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                // Jika perlu inflate menu
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    android.R.id.home -> {
                        findNavController().navigateUp()
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner)

        loadUserData()
        setupUpdateButton()
        setupBackNavigation()
    }


    private fun setupBackNavigation() {
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    findNavController().navigateUp()
                }
            }
        )
    }

    private fun loadUserData() {
        val currentUser = FirebaseManager.getCurrentUser()
        if (currentUser != null) {
            binding.emailText.text = currentUser.email

            FirebaseManager.getUser(currentUser.uid) { user ->
                activity?.runOnUiThread {
                    user?.let {
                        binding.usernameInput.setText(it.username)
                        binding.usernameInput.tag = it.username // Simpan username awal untuk perbandingan
                    }
                }
            }
        }
    }

    private fun setupUpdateButton() {
        binding.updateButton.setOnClickListener {
            val username = binding.usernameInput.text.toString()
            val currentPassword = binding.currentPasswordInput.text.toString()
            val newPassword = binding.passwordInput.text.toString()
            val confirmPassword = binding.confirmPasswordInput.text.toString()

            // Reset error states
            binding.usernameLayout.error = null
            binding.currentPasswordLayout.error = null
            binding.passwordLayout.error = null
            binding.confirmPasswordLayout.error = null

            // Validasi input
            when {
                username.isEmpty() -> {
                    binding.usernameLayout.error = "Username tidak boleh kosong"
                    return@setOnClickListener
                }
                username.length < 3 -> {
                    binding.usernameLayout.error = "Username minimal 3 karakter"
                    return@setOnClickListener
                }
                newPassword.isNotEmpty() && currentPassword.isEmpty() -> {
                    binding.currentPasswordLayout.error = "Masukkan password saat ini"
                    return@setOnClickListener
                }
                newPassword.isNotEmpty() && newPassword.length < 6 -> {
                    binding.passwordLayout.error = "Password minimal 6 karakter"
                    return@setOnClickListener
                }
                newPassword.isNotEmpty() && newPassword != confirmPassword -> {
                    binding.confirmPasswordLayout.error = "Konfirmasi password tidak sesuai"
                    return@setOnClickListener
                }
            }

            showLoading()

            val currentUser = FirebaseManager.getCurrentUser()
            if (currentUser == null) {
                hideLoading()
                showError("Sesi telah berakhir")
                return@setOnClickListener
            }

            var updateCount = 0
            var successCount = 0

            // Update username jika berubah
            if (username != binding.usernameInput.tag) {
                updateCount++
                FirebaseManager.updateUsername(currentUser.uid, username) { success, error ->
                    activity?.runOnUiThread {
                        if (success) {
                            binding.usernameInput.tag = username
                            successCount++
                            checkUpdateCompletion(updateCount, successCount)
                        } else {
                            hideLoading()
                            showError(error ?: "Gagal mengupdate username")
                        }
                    }
                }
            }

            // Update password jika diisi
            if (newPassword.isNotEmpty()) {
                updateCount++
                FirebaseManager.updatePassword(currentPassword, newPassword) { success, error ->
                    activity?.runOnUiThread {
                        if (success) {
                            binding.currentPasswordInput.text?.clear()
                            binding.passwordInput.text?.clear()
                            binding.confirmPasswordInput.text?.clear()
                            successCount++
                            checkUpdateCompletion(updateCount, successCount)
                        } else {
                            hideLoading()
                            when (error) {
                                "Password saat ini salah" -> binding.currentPasswordLayout.error = error
                                else -> showError(error ?: "Gagal mengupdate password")
                            }
                        }
                    }
                }
            }

            // Jika tidak ada yang diupdate, sembunyikan loading
            if (updateCount == 0) {
                hideLoading()
                showError("Tidak ada perubahan yang dilakukan")
            }
        }
    }

    private fun checkUpdateCompletion(totalUpdates: Int, successUpdates: Int) {
        if (totalUpdates == successUpdates) {
            hideLoading()
            showSuccess("Data berhasil diupdate")
        }
    }

    private fun showLoading() {
        loadingDialog = SweetAlertDialog(requireContext(), SweetAlertDialog.PROGRESS_TYPE).apply {
            progressHelper.barColor = resources.getColor(R.color.primary, null)
            titleText = "Mengupdate Data"
            setCancelable(false)
            show()
        }
    }

    private fun hideLoading() {
        loadingDialog?.dismissWithAnimation()
        loadingDialog = null
    }

    private fun showSuccess(message: String) {
        SweetAlertDialog(requireContext(), SweetAlertDialog.SUCCESS_TYPE).apply {
            titleText = "Berhasil!"
            contentText = message
            confirmText = "OK"
            setConfirmClickListener { dismissWithAnimation() }
            show()
        }
    }

    private fun showError(message: String) {
        SweetAlertDialog(requireContext(), SweetAlertDialog.ERROR_TYPE).apply {
            titleText = "Gagal!"
            contentText = message
            confirmText = "OK"
            setConfirmClickListener { dismissWithAnimation() }
            show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        loadingDialog?.dismiss()
        loadingDialog = null
        _binding = null
    }
} 