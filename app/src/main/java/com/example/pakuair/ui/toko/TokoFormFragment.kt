package com.example.pakuair.ui.toko

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import cn.pedant.SweetAlert.SweetAlertDialog
import com.example.pakuair.R
import com.example.pakuair.data.FirebaseManager
import com.example.pakuair.data.model.Toko
import com.example.pakuair.databinding.FragmentTokoFormBinding

class TokoFormFragment : Fragment() {
    private var _binding: FragmentTokoFormBinding? = null
    private val binding get() = _binding!!
    private var existingToko: Toko? = null
    private var loadingDialog: SweetAlertDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTokoFormBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Get existing toko if in edit mode
        arguments?.getString("tokoId")?.let { tokoId ->
            FirebaseManager.getToko(tokoId) { toko ->
                activity?.runOnUiThread {
                    toko?.let {
                        existingToko = it
                        setupEditMode(it)
                    }
                }
            }
        }

        setupClickListeners()
    }

    private fun setupEditMode(toko: Toko) {
        binding.apply {
            formTitle.text = "Edit Toko"
            namaTokoInput.setText(toko.namaToko)
            alamatTokoInput.setText(toko.alamatToko)
            kontakTokoInput.setText(toko.kontakToko)
            deskripsiTokoInput.setText(toko.deskripsiToko)
            submitButton.text = "Update"
        }
    }

    private fun setupClickListeners() {
        binding.submitButton.setOnClickListener {
            val namaToko = binding.namaTokoInput.text.toString()
            val alamatToko = binding.alamatTokoInput.text.toString()
            val kontakToko = binding.kontakTokoInput.text.toString()
            val deskripsiToko = binding.deskripsiTokoInput.text.toString()

            // Validate input
            when {
                namaToko.isEmpty() -> {
                    binding.namaTokoLayout.error = "Nama toko tidak boleh kosong"
                    return@setOnClickListener
                }
                alamatToko.isEmpty() -> {
                    binding.alamatTokoLayout.error = "Alamat toko tidak boleh kosong"
                    return@setOnClickListener
                }
                kontakToko.isEmpty() -> {
                    binding.kontakTokoLayout.error = "Kontak toko tidak boleh kosong"
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

            val toko = Toko(
                id = existingToko?.id ?: "",
                userId = currentUser.uid,
                namaToko = namaToko,
                alamatToko = alamatToko,
                kontakToko = kontakToko,
                deskripsiToko = deskripsiToko
            )

            if (existingToko != null) {
                // Update existing toko
                FirebaseManager.updateToko(toko) { success, error ->
                    activity?.runOnUiThread {
                        hideLoading()
                        if (success) {
                            showSuccess("Toko berhasil diperbarui")
                        } else {
                            showError(error ?: "Gagal memperbarui toko")
                        }
                    }
                }
            } else {
                // Create new toko
                FirebaseManager.saveToko(toko) { success, error ->
                    activity?.runOnUiThread {
                        hideLoading()
                        if (success) {
                            showSuccess("Toko berhasil dibuat")
                        } else {
                            showError(error ?: "Gagal membuat toko")
                        }
                    }
                }
            }
        }
    }

    private fun showLoading() {
        loadingDialog = SweetAlertDialog(requireContext(), SweetAlertDialog.PROGRESS_TYPE).apply {
            progressHelper.barColor = resources.getColor(R.color.primary, null)
            titleText = if (existingToko != null) "Memperbarui Toko" else "Membuat Toko"
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
            setConfirmClickListener {
                dismissWithAnimation()
                findNavController().navigateUp()
            }
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

    companion object {
        fun newInstance(tokoId: String? = null) = TokoFormFragment().apply {
            arguments = bundleOf("tokoId" to tokoId)
        }
    }
} 