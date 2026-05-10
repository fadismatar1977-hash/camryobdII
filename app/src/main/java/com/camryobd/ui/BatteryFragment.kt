package com.camryobd.ui

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.camryobd.OBD2Manager
import com.camryobd.R
import com.camryobd.databinding.FragmentBatteryBinding
import com.camryobd.models.CellStatus
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class BatteryFragment : Fragment() {
    private var _binding: FragmentBatteryBinding? = null
    private val binding get() = _binding!!
    private var dataLoop: Job? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentBatteryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        startDataLoop()
    }

    private fun startDataLoop() {
        dataLoop = lifecycleScope.launch {
            while (isActive) {
                val mgr = (requireActivity() as? com.camryobd.MainActivity)?.getManager()
                if (mgr != null) {
                    mgr.getHybridBlockVoltages().onSuccess { pack ->
                        updatePackView(pack)
                    }
                }
                delay(3000)
            }
        }
    }

    private fun updatePackView(pack: com.camryobd.models.BatteryPackData) {
        if (pack.blocks.isEmpty()) return

        binding.packMaxValue.text = String.format("%.3f V", pack.maxVoltage)
        binding.packMinValue.text = String.format("%.3f V", pack.minVoltage)
        binding.packDeltaValue.text = String.format("%.0f mV", pack.deltaMv)
        binding.packAvgValue.text = String.format("%.3f V", pack.avgVoltage)
        binding.packTotalValue.text = String.format("%.1f V", pack.totalVoltage)

        binding.packHealthBadge.text = "● ${pack.healthStatus}"
        binding.packHealthBadge.setTextColor(when (pack.healthStatus) {
            "PERFECT" -> Color.rgb(0, 255, 136)
            "GOOD" -> Color.rgb(0, 210, 255)
            "WARNING" -> Color.rgb(255, 214, 0)
            else -> Color.rgb(255, 51, 85)
        })

        binding.balanceAnalysisText.text = pack.analysis

        val cells = pack.blocks
        val cellViews = listOf(
            binding.cell1, binding.cell2, binding.cell3, binding.cell4, binding.cell5, binding.cell6, binding.cell7,
            binding.cell8, binding.cell9, binding.cell10, binding.cell11, binding.cell12, binding.cell13, binding.cell14,
        )

        for (i in cells.indices) {
            if (i >= cellViews.size) break
            val v = cellViews[i]
            val cell = cells[i]

            val bgColor = when (cell.status) {
                CellStatus.OPTIMAL -> Color.rgb(0, 60, 30)
                CellStatus.GOOD -> Color.rgb(0, 40, 70)
                CellStatus.WARN -> Color.rgb(60, 50, 0)
                CellStatus.BAD -> Color.rgb(60, 15, 25)
                CellStatus.CRITICAL -> Color.rgb(80, 10, 20)
            }
            val textColor = when (cell.status) {
                CellStatus.OPTIMAL -> Color.rgb(0, 255, 136)
                CellStatus.GOOD -> Color.rgb(0, 210, 255)
                CellStatus.WARN -> Color.rgb(255, 214, 0)
                CellStatus.BAD -> Color.rgb(255, 51, 85)
                CellStatus.CRITICAL -> Color.WHITE
            }
            v.setBackgroundColor(bgColor)
            v.setTextColor(textColor)
            v.text = String.format("%.2f", cell.voltage)
            v.contentDescription = "Block ${cell.block}: ${String.format("%.3f", cell.voltage)}V"
        }
    }

    override fun onDestroyView() {
        dataLoop?.cancel()
        super.onDestroyView()
        _binding = null
    }
}
