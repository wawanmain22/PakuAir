package com.example.pakuair.ui.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.pakuair.R
import com.example.pakuair.data.FirebaseManager
import com.example.pakuair.data.HasilCekAir
import com.example.pakuair.databinding.FragmentHistoryDetailBinding
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textview.MaterialTextView
import java.text.SimpleDateFormat
import java.util.*

class HistoryDetailFragment : Fragment() {
    private var _binding: FragmentHistoryDetailBinding? = null
    private val binding get() = _binding!!
    private val args: HistoryDetailFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadDetail()
    }

    override fun onResume() {
        super.onResume()
        loadDetail() // Reload data setiap kali Fragment muncul
    }

    private fun loadDetail() {
        FirebaseManager.getHasilCekAirById(args.hasilId) { hasil ->
            if (!isAdded) return@getHasilCekAirById
            
            if (hasil == null) {
                findNavController().navigateUp()
                return@getHasilCekAirById
            }

            setupResultCard(hasil)
            addParameterDetails(hasil.parameters)
            setupSystemInfo(hasil)
        }
    }
    private fun setupResultCard(hasil: HasilCekAir) {
        binding.apply {
            resultIcon.setImageResource(
                if (hasil.potability == 1) R.drawable.ic_check_success
                else R.drawable.ic_check_failed
            )
            resultCard.setCardBackgroundColor(
                resources.getColor(
                    if (hasil.potability == 1) R.color.success_background
                    else R.color.error_background,
                    null
                )
            )
            resultTitle.text = if (hasil.potability == 1) "Air Layak Minum" else "Air Tidak Layak Minum"
            resultMessage.text = hasil.message
        }
    }

    private fun addParameterDetails(parameters: Map<String, Double>) {
        val parameterRanges = mapOf(
            "derajatKeasaman" to Triple("Derajat Keasaman (pH)", "6.5 - 8.5", 6.5..8.5),
            "kesadahan" to Triple("Kesadahan (mg/L)", "0 - 300", 0.0..300.0),
            "padatan" to Triple("Total Padatan Terlarut (mg/L)", "0 - 1000", 0.0..1000.0),
            "kloramin" to Triple("Kloramin (mg/L)", "0 - 4", 0.0..4.0),
            "sulfat" to Triple("Sulfat (mg/L)", "0 - 250", 0.0..250.0),
            "konduktivitas" to Triple("Konduktivitas (μS/cm)", "0 - 1000", 0.0..1000.0),
            "karbonOrganik" to Triple("Karbon Organik (mg/L)", "0 - 15", 0.0..15.0),
            "trihalometan" to Triple("Trihalometan (μg/L)", "0 - 80", 0.0..80.0),
            "kekeruhan" to Triple("Kekeruhan (NTU)", "0 - 5", 0.0..5.0)
        )

        binding.parametersContainer.removeAllViews()
        parameters.forEach { (key, value) ->
            parameterRanges[key]?.let { (label, range, validRange) ->
                addParameterCard(label, value, range, value in validRange)
            }
        }
    }

    private fun addParameterCard(label: String, value: Double, normalRange: String, isNormal: Boolean) {
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
                    if (isNormal) R.color.normal_parameter_background
                    else R.color.abnormal_parameter_background,
                    null
                )
            )
        }

        binding.parametersContainer.addView(parameterView)
    }

    private fun setupSystemInfo(hasil: HasilCekAir) {
        binding.systemInfo.text = """
            Waktu Prediksi: ${hasil.predictionTime}
            Timestamp: ${SimpleDateFormat("dd MMMM yyyy, HH:mm:ss", Locale("id"))
                .format(Date(hasil.timestamp))}
        """.trimIndent()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}