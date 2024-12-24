package com.example.pakuair.ui.cekair

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import cn.pedant.SweetAlert.SweetAlertDialog
import com.example.pakuair.R
import com.example.pakuair.data.FirebaseManager
import com.example.pakuair.databinding.FragmentHasilCekairBinding
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textview.MaterialTextView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CekAirHasilFragment : Fragment() {
    private var _binding: FragmentHasilCekairBinding? = null
    private val binding get() = _binding!!
    private val args: CekAirHasilFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHasilCekairBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        setupClickListeners()
    }

    private fun setupUI() {
        setupResultIndicator(args.potability)
        setupResultDetails()
        addParameterDetails()
        addSystemInfo()
    }

    private fun setupResultIndicator(potability: Int) {
        binding.apply {
            resultIcon.setImageResource(
                if (potability == 1) R.drawable.ic_check_success
                else R.drawable.ic_check_failed
            )

            resultCard.setCardBackgroundColor(
                resources.getColor(
                    if (potability == 1) R.color.success_background
                    else R.color.error_background,
                    null
                )
            )
        }
    }

    private fun setupResultDetails() {
        binding.apply {
            resultTitle.text = if (args.potability == 1) "Air Layak Minum" else "Air Tidak Layak Minum"
            resultMessage.text = args.message
        }
    }

    private fun addParameterDetails() {
        val parameters = listOf(
            Triple("Derajat Keasaman (pH)", args.cekAir.derajatKeasaman, "6.5 - 8.5"),
            Triple("Kesadahan (mg/L)", args.cekAir.kesadahan, "0 - 300"),
            Triple("Total Padatan Terlarut (mg/L)", args.cekAir.padatan, "0 - 1000"),
            Triple("Kloramin (mg/L)", args.cekAir.kloramin, "0 - 4"),
            Triple("Sulfat (mg/L)", args.cekAir.sulfat, "0 - 250"),
            Triple("Konduktivitas (μS/cm)", args.cekAir.konduktivitas, "0 - 1000"),
            Triple("Karbon Organik (mg/L)", args.cekAir.karbonOrganik, "0 - 15"),
            Triple("Trihalometan (μg/L)", args.cekAir.trihalometan, "0 - 80"),
            Triple("Kekeruhan (NTU)", args.cekAir.kekeruhan, "0 - 5")
        )

        parameters.forEach { (label, value, range) ->
            addParameterCard(label, value, range)
        }
    }

    private fun addParameterCard(label: String, value: Double, normalRange: String) {
        val parameterView = layoutInflater.inflate(
            R.layout.item_parameter_detail,
            binding.parametersContainer,
            false
        )

        parameterView.apply {
            findViewById<MaterialTextView>(R.id.parameterLabel).text = label
            findViewById<MaterialTextView>(R.id.parameterValue).text = String.format("%.2f", value)
            findViewById<MaterialTextView>(R.id.parameterRange).text = "Rentang normal: $normalRange"

            findViewById<MaterialCardView>(R.id.parameterCard).setCardBackgroundColor(
                resources.getColor(
                    if (isValueInNormalRange(label, value)) R.color.normal_parameter_background
                    else R.color.abnormal_parameter_background,
                    null
                )
            )
        }

        binding.parametersContainer.addView(parameterView)
    }

    private fun isValueInNormalRange(parameter: String, value: Double): Boolean {
        return when {
            parameter.contains("pH") -> value in 6.5..8.5
            parameter.contains("Kesadahan") -> value in 0.0..300.0
            parameter.contains("Padatan") -> value in 0.0..1000.0
            parameter.contains("Kloramin") -> value in 0.0..4.0
            parameter.contains("Sulfat") -> value in 0.0..250.0
            parameter.contains("Konduktivitas") -> value in 0.0..1000.0
            parameter.contains("Karbon") -> value in 0.0..15.0
            parameter.contains("Trihalometan") -> value in 0.0..80.0
            parameter.contains("Kekeruhan") -> value in 0.0..5.0
            else -> true
        }
    }

    private fun addSystemInfo() {
        binding.systemInfo.text = """
            Waktu Prediksi: ${args.predictionTime}
            CPU Usage: ${args.cpuUsage}
            Memory Usage: ${args.memoryUsage}
            Timestamp: ${SimpleDateFormat("dd MMMM yyyy, HH:mm:ss", Locale("id")).format(Date())}
        """.trimIndent()
    }

    private fun setupClickListeners() {
        binding.apply {
            backButton.setOnClickListener {
                findNavController().navigateUp()
            }

            saveButton.setOnClickListener {
                // Siapkan parameter
                val parameters = mapOf(
                    "derajatKeasaman" to args.cekAir.derajatKeasaman,
                    "kesadahan" to args.cekAir.kesadahan,
                    "padatan" to args.cekAir.padatan,
                    "kloramin" to args.cekAir.kloramin,
                    "sulfat" to args.cekAir.sulfat,
                    "konduktivitas" to args.cekAir.konduktivitas,
                    "karbonOrganik" to args.cekAir.karbonOrganik,
                    "trihalometan" to args.cekAir.trihalometan,
                    "kekeruhan" to args.cekAir.kekeruhan
                )

                // Siapkan result
                val result = mapOf(
                    "potability" to args.potability,
                    "message" to args.message,
                    "prediction_time" to args.predictionTime
                )

                // Simpan ke Firebase
                FirebaseManager.saveHasilCekAir(parameters, result) { success, error ->
                    if (success) {
                        showSuccessDialog()
                    } else {
                        SweetAlertDialog(requireContext(), SweetAlertDialog.ERROR_TYPE).apply {
                            titleText = "Error"
                            contentText = error ?: "Gagal menyimpan hasil"
                            show()
                        }
                    }
                }
            }
        }
    }

    private fun showSuccessDialog() {
        SweetAlertDialog(requireContext(), SweetAlertDialog.SUCCESS_TYPE).apply {
            titleText = "Berhasil"
            contentText = "Hasil pemeriksaan berhasil disimpan"
            setConfirmClickListener {
                dismissWithAnimation()
                findNavController().navigateUp()
            }
            show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}