package com.example.pakuair.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import cn.pedant.SweetAlert.SweetAlertDialog
import com.example.pakuair.MainActivity
import com.example.pakuair.R
import com.example.pakuair.databinding.FragmentSigninBinding

class SignInFragment : Fragment() {
    private var _binding: FragmentSigninBinding? = null
    private val binding get() = _binding!!
    private val viewModel by viewModels<SigninViewModel>()
    private var loadingDialog: SweetAlertDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSigninBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupListeners()
        observeViewModel()
    }

    private fun setupListeners() {
        binding.signInButton.setOnClickListener {
            val email = binding.emailInput.text.toString()
            val password = binding.passwordInput.text.toString()
            
            showLoading()
            viewModel.signIn(email, password)
        }

        binding.signUpLink.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .setCustomAnimations(
                    R.anim.slide_in_right,
                    R.anim.slide_out_left,
                    R.anim.slide_in_left,
                    R.anim.slide_out_right
                )
                .replace(R.id.auth_container, SignUpFragment())
                .addToBackStack(null)
                .commit()
        }
    }

    private fun showLoading() {
        loadingDialog = SweetAlertDialog(requireContext(), SweetAlertDialog.PROGRESS_TYPE).apply {
            progressHelper.barColor = resources.getColor(R.color.primary, null)
            titleText = "Masuk"
            setCancelable(false)
            show()
        }
    }

    private fun hideLoading() {
        loadingDialog?.dismissWithAnimation()
        loadingDialog = null
    }

    private fun navigateToMain() {
        val intent = Intent(requireContext(), MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
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
        viewModel.signInResult.observe(viewLifecycleOwner) { result ->
            hideLoading()
            when (result) {
                is SigninViewModel.Result.Success -> {
                    navigateToMain()
                }
                is SigninViewModel.Result.Error -> {
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