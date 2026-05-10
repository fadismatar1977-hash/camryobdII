package com.camryobd.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.camryobd.OBD2Manager
import com.camryobd.R
import com.camryobd.databinding.FragmentDashboardBinding
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class DashboardFragment : Fragment() {
    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private var manager: OBD2Manager? = null
    private var dataLoop: Job? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        manager = (requireActivity() as? com.camryobd.MainActivity)?.getManager()
        if (manager != null) startDataLoop()
    }

    private fun startDataLoop() {
        dataLoop = lifecycleScope.launch {
            while (isActive) {
                if (manager == null) break
                updateAllData()
                delay(1000)
            }
        }
    }

    private suspend fun updateAllData() {
        val mgr = manager ?: return

        mgr.getEngineRpm().onSuccess {
            binding.rpmValue.text = "${it.value} ${it.unit}"
        }
        mgr.getSpeed().onSuccess {
            binding.speedValue.text = "${it.value} ${it.unit}"
        }
        mgr.getCoolantTemp().onSuccess {
            binding.coolantValue.text = "${it.value} ${it.unit}"
        }
        mgr.getBatteryVoltage().onSuccess {
            binding.batteryValue.text = "${it.value} ${it.unit}"
        }
        mgr.getFuelLevel().onSuccess {
            binding.fuelValue.text = it.value
        }
        mgr.getEngineLoad().onSuccess {
            binding.loadValue.text = it.value
        }
        mgr.getHybridData().onSuccess {
            binding.hvSocValue.text = it.hvBatterySoc
            binding.hvVoltageValue.text = it.hvBatteryVoltage
        }
    }

    override fun onDestroyView() {
        dataLoop?.cancel()
        super.onDestroyView()
        _binding = null
    }
}
