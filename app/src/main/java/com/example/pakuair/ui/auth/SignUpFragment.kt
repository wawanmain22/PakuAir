package com.example.pakuair.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import cn.pedant.SweetAlert.SweetAlertDialog
import com.example.pakuair.R
import com.example.pakuair.databinding.FragmentSignupBinding

class SignUpFragment : Fragment() {
    private var _binding: FragmentSignupBinding? = null
    private val binding get() = _binding!!
    private val viewModel by viewModels<SignupViewModel>()
    private var loadingDialog: SweetAlertDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSignupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupListeners()
        observeViewModel()
    }

    private fun setupListeners() {
        binding.signInLink.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.signUpButton.setOnClickListener {
            val email = binding.emailInput.text.toString()
            val username = binding.usernameInput.text.toString()
            val password = binding.passwordInput.text.toString()
            val confirmPassword = binding.confirmPasswordInput.text.toString()
            
            showLoading()
            viewModel.signUp(email, username, password, confirmPassword)
        }
    }

    private fun showLoading() {
        loadingDialog = SweetAlertDialog(requireContext(), SweetAlertDialog.PROGRESS_TYPE).apply {
            progressHelper.barColor = resources.getColor(R.color.primary, null)
            titleText = "Mendaftarkan Akun"
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
                parentFragmentManager.popBackStack()
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

    private fun observeViewModel() {
        viewModel.signUpResult.observe(viewLifecycleOwner) { result ->
            hideLoading()
            when (result) {
                is SignupViewModel.Result.Success -> {
                    showSuccess("Registrasi berhasil")
                }
                is SignupViewModel.Result.Error -> {
                    showError(result.message)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        loadingDialog?.dismiss()
        loadingDialog = null
        _binding = null
    }
} 