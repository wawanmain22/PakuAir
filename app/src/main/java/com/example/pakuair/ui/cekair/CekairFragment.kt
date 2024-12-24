package com.example.pakuair.ui.cekair

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import cn.pedant.SweetAlert.SweetAlertDialog
import com.example.pakuair.R
import com.example.pakuair.api.ApiService
import com.example.pakuair.data.model.CekAir
import com.example.pakuair.databinding.FragmentCekairBinding
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CekAirFragment : Fragment() {
    private var _binding: FragmentCekairBinding? = null
    private val binding get() = _binding!!
    private val apiService = ApiService()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCekairBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupCheckButton()
    }

    private fun setupCheckButton() {
        binding.checkButton.setOnClickListener {
            if (validateInputs()) {
                checkWaterQuality()
            }
        }
    }

    private fun validateInputs(): Boolean {
        val validations = listOf(
            validateField(binding.phLayout, binding.phInput.text.toString(), 0.0..14.0),
            validateField(binding.hardnessLayout, binding.hardnessInput.text.toString(), 0.0..1000.0),
            validateField(binding.solidsLayout, binding.solidsInput.text.toString(), 0.0..2000.0),
            validateField(binding.chloraminesLayout, binding.chloraminesInput.text.toString(), 0.0..10.0),
            validateField(binding.sulfateLayout, binding.sulfateInput.text.toString(), 0.0..1000.0),
            validateField(binding.conductivityLayout, binding.conductivityInput.text.toString(), 0.0..2000.0),
            validateField(binding.organicCarbonLayout, binding.organicCarbonInput.text.toString(), 0.0..30.0),
            validateField(binding.trihalomethanesLayout, binding.trihalomethanesInput.text.toString(), 0.0..200.0),
            validateField(binding.turbidityLayout, binding.turbidityInput.text.toString(), 0.0..10.0)
        )

        return validations.all { it }
    }

    private fun validateField(layout: TextInputLayout, value: String, range: ClosedRange<Double>): Boolean {
        val numValue = value.toDoubleOrNull()
        return when {
            value.isEmpty() -> {
                layout.error = "Field ini harus diisi"
                false
            }
            numValue == null -> {
                layout.error = "Masukkan angka yang valid"
                false
            }
            !range.contains(numValue) -> {
                layout.error = "Nilai harus antara ${range.start} dan ${range.endInclusive}"
                false
            }
            else -> {
                layout.error = null
                true
            }
        }
    }

    private fun getInputValues(): CekAir {
        return CekAir(
            derajatKeasaman = binding.phInput.text.toString().toDoubleOrNull() ?: 0.0,
            kesadahan = binding.hardnessInput.text.toString().toDoubleOrNull() ?: 0.0,
            padatan = binding.solidsInput.text.toString().toDoubleOrNull() ?: 0.0,
            kloramin = binding.chloraminesInput.text.toString().toDoubleOrNull() ?: 0.0,
            sulfat = binding.sulfateInput.text.toString().toDoubleOrNull() ?: 0.0,
            konduktivitas = binding.conductivityInput.text.toString().toDoubleOrNull() ?: 0.0,
            karbonOrganik = binding.organicCarbonInput.text.toString().toDoubleOrNull() ?: 0.0,
            trihalometan = binding.trihalomethanesInput.text.toString().toDoubleOrNull() ?: 0.0,
            kekeruhan = binding.turbidityInput.text.toString().toDoubleOrNull() ?: 0.0
        )
    }

    private fun checkWaterQuality() {
        val loadingDialog = SweetAlertDialog(requireContext(), SweetAlertDialog.PROGRESS_TYPE).apply {
            titleText = "Memproses"
            contentText = "Sedang menganalisis kualitas air..."
            setCancelable(false)
        }
        loadingDialog.show()

        lifecycleScope.launch {
            try {
                val cekAir = getInputValues()
                val params = mapOf(
                    "derajatKeasaman" to cekAir.derajatKeasaman,
                    "kesadahan" to cekAir.kesadahan,
                    "padatan" to cekAir.padatan,
                    "kloramin" to cekAir.kloramin,
                    "sulfat" to cekAir.sulfat,
                    "konduktivitas" to cekAir.konduktivitas,
                    "karbonOrganik" to cekAir.karbonOrganik,
                    "trihalometan" to cekAir.trihalometan,
                    "kekeruhan" to cekAir.kekeruhan
                )

                val result = withContext(Dispatchers.IO) {
                    apiService.checkWaterQuality(params)
                }

                loadingDialog.dismissWithAnimation()

                // Navigate to result fragment with data
                val action = CekAirFragmentDirections.actionCekAirFragmentToHasilCekAirFragment(
                    cekAir = cekAir,
                    potability = result.getInt("potability"),
                    message = result.getString("message"),
                    predictionTime = result.getString("prediction_time"),
                    cpuUsage = result.getJSONObject("system_info").getString("cpu"),
                    memoryUsage = result.getJSONObject("system_info").getString("memory")
                )
                findNavController().navigate(action)

            } catch (e: Exception) {
                loadingDialog.dismissWithAnimation()

                SweetAlertDialog(requireContext(), SweetAlertDialog.ERROR_TYPE).apply {
                    titleText = "Error"
                    contentText = "Terjadi kesalahan: ${e.message}"
                    show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val TAG = "CekAirFragment"
    }
}