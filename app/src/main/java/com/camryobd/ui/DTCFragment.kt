package com.camryobd.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.camryobd.OBD2Manager
import com.camryobd.R
import com.camryobd.databinding.FragmentDtcBinding
import kotlinx.coroutines.launch

class DTCFragment : Fragment() {
    private var _binding: FragmentDtcBinding? = null
    private val binding get() = _binding!!
    private var manager: OBD2Manager? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentDtcBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        manager = (requireActivity() as? com.camryobd.MainActivity)?.getManager()

        binding.scanDtcButton.setOnClickListener { readDTCs() }
        binding.clearDtcButton.setOnClickListener { clearDTCs() }

        readDTCs()
    }

    private fun readDTCs() {
        val mgr = manager ?: return
        binding.dtcResultText.text = "Scanning..."
        binding.dtcResultText.visibility = View.VISIBLE

        lifecycleScope.launch {
            mgr.getDiagnosticTroubleCodes().onSuccess { codes ->
                if (codes.isEmpty()) {
                    binding.dtcResultText.text = "No fault codes found ✓"
                } else {
                    val sb = StringBuilder()
                    for (code in codes) {
                        sb.appendLine(code.code)
                        val desc = OBD2Manager.DTC_DESCRIPTIONS[code.code]
                        if (desc != null) sb.appendLine("  → $desc")
                    }
                    binding.dtcResultText.text = sb.toString()
                }
            }.onFailure {
                binding.dtcResultText.text = "Error: ${it.message}"
            }
        }
    }

    private fun clearDTCs() {
        val mgr = manager ?: return
        binding.dtcResultText.text = "Clearing codes..."
        binding.dtcResultText.visibility = View.VISIBLE

        lifecycleScope.launch {
            mgr.clearDiagnosticTroubleCodes().onSuccess {
                binding.dtcResultText.text = "Codes cleared ✓"
            }.onFailure {
                binding.dtcResultText.text = "Clear failed: ${it.message}"
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
