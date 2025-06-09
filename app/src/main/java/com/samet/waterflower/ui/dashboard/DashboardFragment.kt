package com.samet.waterflower.ui.dashboard

import android.graphics.Color
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.charts.BarLineChartBase
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.firebase.database.*
import com.samet.waterflower.R
import com.samet.waterflower.databinding.FragmentDashboardBinding
import com.google.android.material.R as MaterialR
import java.text.SimpleDateFormat
import java.util.*

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private val db          = FirebaseDatabase.getInstance()
    private val moistureRef = db.getReference("soil_moisture")
    private val motorRef    = db.getReference("motor")

    override fun onCreateView(inflater: LayoutInflater, c: ViewGroup?, s: Bundle?) =
        FragmentDashboardBinding.inflate(inflater, c, false).also { _binding = it }.root

    override fun onViewCreated(v: View, s: Bundle?) {
        super.onViewCreated(v, s)

        // metric kartlarını güncelle
        fetchLatestMoisture()
        fetchLatestMotor()

        // butonlar
        binding.btnShowMoistureChart.setOnClickListener {
            binding.cardMoistureChart.visibility = View.VISIBLE
            binding.cardMotorChart  .visibility = View.GONE
            setupMoistureChart()
        }
        binding.btnShowMotorChart.setOnClickListener {
            binding.cardMoistureChart.visibility = View.GONE
            binding.cardMotorChart  .visibility = View.VISIBLE
            setupMotorChart()
        }
    }

    private fun fetchLatestMoisture() {
        moistureRef.orderByChild("timestamp").limitToLast(1)
            .addListenerForSingleValueEvent(object: ValueEventListener {
                override fun onDataChange(ds: DataSnapshot) {
                    ds.children.firstOrNull()?.let {
                        val map = it.value as? Map<*,*> ?: return
                        val v   = (map["moisture_value"] as? Long)?.toInt()?:0
                        binding.tvMoistureValue.apply {
                            text = if(v==1) "OK" else "Need Water!"
                            setTextColor(ContextCompat.getColor(
                                requireContext(),
                                if(v==1) R.color.green_500 else R.color.red_500
                            ))
                        }
                    }
                }
                override fun onCancelled(e: DatabaseError) {}
            })
    }

    private fun fetchLatestMotor() {
        motorRef.orderByChild("timestamp").limitToLast(1)
            .addListenerForSingleValueEvent(object: ValueEventListener {
                override fun onDataChange(ds: DataSnapshot) {
                    ds.children.firstOrNull()?.let {
                        val map = it.value as? Map<*,*> ?: return
                        val v   = (map["running_value"] as? Long)?.toInt()?:0
                        binding.tvMotorValue.apply {
                            text = if(v==1) "Running" else "Stopped"
                            setTextColor(ContextCompat.getColor(
                                requireContext(),
                                if(v==1) R.color.green_500 else R.color.red_500
                            ))
                        }
                    }
                }
                override fun onCancelled(e: DatabaseError) {}
            })
    }

    private fun setupMoistureChart() {
        val chart = binding.chartMoisture
        chart.applyTheme()
        chart.clear()

        moistureRef.orderByChild("timestamp").limitToLast(20)
            .addListenerForSingleValueEvent(object: ValueEventListener {
                override fun onDataChange(ds: DataSnapshot) {
                    val entries = mutableListOf<Entry>()
                    val labels  = mutableListOf<String>()
                    val fmt     = SimpleDateFormat("HH:mm", Locale.getDefault())

                    ds.children.forEachIndexed { i, node ->
                        val map = node.value as? Map<*,*>?:return@forEachIndexed
                        val ts  = (map["timestamp"] as? Long)?:0L
                        val v   = (map["moisture_value"] as? Long)?.toFloat()?:0f
                        entries.add(Entry(i.toFloat(), v))
                        labels.add(fmt.format(Date(ts)))
                    }

                    val set = LineDataSet(entries, "").apply {
                        color          = resolveAttr(MaterialR.attr.colorPrimary)
                        setCircleColor(color)
                        setDrawValues(false)
                        lineWidth      = 2f
                    }
                    chart.data = LineData(set)
                    chart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
                    chart.invalidate()
                }
                override fun onCancelled(e: DatabaseError) {}
            })
    }

    private fun setupMotorChart() {
        val chart = binding.chartMotor
        chart.applyTheme()
        chart.clear()

        motorRef.orderByChild("timestamp").limitToLast(20)
            .addListenerForSingleValueEvent(object: ValueEventListener {
                override fun onDataChange(ds: DataSnapshot) {
                    val entries = mutableListOf<BarEntry>()
                    val labels  = mutableListOf<String>()
                    val fmt     = SimpleDateFormat("HH:mm", Locale.getDefault())

                    ds.children.forEachIndexed { i, node ->
                        val map = node.value as? Map<*,*>?:return@forEachIndexed
                        val ts  = (map["timestamp"] as? Long)?:0L
                        val v   = (map["running_value"] as? Long)?.toFloat()?:0f
                        entries.add(BarEntry(i.toFloat(), v))
                        labels.add(fmt.format(Date(ts)))
                    }

                    val set = BarDataSet(entries, "").apply {
                        color          = resolveAttr(MaterialR.attr.colorPrimary)
                        setDrawValues(false)
                    }
                    chart.data = BarData(set)
                    chart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
                    chart.invalidate()
                }
                override fun onCancelled(e: DatabaseError) {}
            })
    }

    // Tema’dan renk çeker
    private fun resolveAttr(attr: Int): Int {
        val tv = TypedValue()
        requireContext().theme.resolveAttribute(attr, tv, true)
        return tv.data
    }

    // Ortak styling
    private fun BarLineChartBase<*>.applyTheme() {
        setBackgroundColor(resolveAttr(MaterialR.attr.colorSurface))
        setDrawGridBackground(false)
        legend.isEnabled      = false
        description.isEnabled = false

        xAxis.run {
            position      = XAxis.XAxisPosition.BOTTOM
            textColor     = resolveAttr(MaterialR.attr.colorOnSurface)
            gridColor     = Color.TRANSPARENT
            axisLineColor = resolveAttr(MaterialR.attr.colorOnSurface)
        }
        axisLeft.run {
            axisMinimum   = 0f
            axisMaximum   = 1f
            setLabelCount(2, true)
            textColor     = resolveAttr(MaterialR.attr.colorOnSurface)
            gridColor     = Color.TRANSPARENT
            axisLineColor = resolveAttr(MaterialR.attr.colorOnSurface)
        }
        axisRight.isEnabled = false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
