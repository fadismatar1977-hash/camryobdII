package com.camryobd.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.camryobd.OBD2Manager
import com.camryobd.OBDService
import com.camryobd.R
import com.camryobd.databinding.FragmentConnectionBinding
import kotlinx.coroutines.launch

class ConnectionFragment : Fragment() {
    private var _binding: FragmentConnectionBinding? = null
    private val binding get() = _binding!!
    private val obd = OBDService()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentConnectionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.connectButton.setOnClickListener { connectToAdapter() }
    }

    private fun connectToAdapter() {
        val address = binding.ipInput.text.toString().trim()
        if (address.isEmpty()) {
            binding.statusText.text = "Enter IP address (e.g. 192.168.0.10)"
            binding.statusText.visibility = View.VISIBLE
            return
        }

        binding.statusText.text = "Connecting to $address..."
        binding.statusText.visibility = View.VISIBLE
        binding.connectButton.isEnabled = false

        lifecycleScope.launch {
            val result = obd.connect(address)
            if (result.isSuccess) {
                binding.statusText.text = "Initializing ELM327..."
                val manager = OBD2Manager(obd)
                val initResult = manager.initialize()
                if (initResult.isSuccess) {
                    binding.statusText.text = "Connected ✓ Switching to dashboard..."
                    val activity = requireActivity() as? com.camryobd.MainActivity
                    activity?.onConnected(obd, manager)
                    activity?.switchToDashboard()
                } else {
                    binding.statusText.text = "Init failed: ${initResult.exceptionOrNull()?.message}"
                    binding.connectButton.isEnabled = true
                }
            } else {
                binding.statusText.text = "Failed: ${result.exceptionOrNull()?.message}"
                binding.connectButton.isEnabled = true
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
