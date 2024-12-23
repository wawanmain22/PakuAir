package com.example.pakuair.ui.information

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.pakuair.databinding.FragmentInformationDetailBinding
import android.text.SpannableStringBuilder

class InformationDetailFragment : Fragment() {
    private var _binding: FragmentInformationDetailBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInformationDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val informationId = arguments?.getInt("informationId") ?: return
        val information = InformationData.informationList.find { it.id == informationId } ?: return

        binding.apply {
            // Format content dengan indentasi yang benar
            val formattedContent = formatContent(information.content)
            contentText.text = formattedContent
        }
    }

    private fun formatContent(content: String): CharSequence {
        val spannableString = SpannableStringBuilder()

        content.split("\n").forEach { line ->
            when {
                // Untuk item utama (1., 2., dst)
                line.trim().matches(Regex("^\\d+\\..*")) -> {
                    spannableString.append("\n${line.trim()}\n")
                }
                // Untuk sub-items dengan dash (-)
                line.trim().startsWith("-") -> {
                    spannableString.append("    ${line.trim()}\n")
                }
                // Untuk text biasa
                else -> {
                    spannableString.append("${line.trim()}\n")
                }
            }
        }

        return spannableString
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}