package com.cpti.calino.fragments.dummy


import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.cpti.calino.MainActivity

import com.cpti.calino.R
import android.widget.FrameLayout
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.utils.ColorTemplate


class IncreaseCounterDeviceFragment : Fragment(), MainActivity.OnReceiveDataListener {

    private var chart: BarChart? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater!!.inflate(R.layout.fragment_increase_counter_device, container, false)

        // create a new chart
        chart = BarChart(activity)
        chart?.let { c ->
            c.axisLeft.axisMinimum = 0f
            c.axisLeft.axisMaximum = 1000f
            c.axisRight.isEnabled = false
            c.xAxis.isEnabled = false
            c.description.isEnabled = false
        }

        view as FrameLayout
        view.addView(chart)

        return view
    }

    override fun onReceiveData(data: String) {
        chart?.data = createData(data.toFloat())
        chart?.invalidate()
    }

    private fun createData(data: Float): BarData {
        val entries = ArrayList<BarEntry>()
        entries.add(BarEntry(0f, data))
        val ds = BarDataSet(entries, "Counter value")
        val colors = ArrayList<Int>()
        for (color in ColorTemplate.VORDIPLOM_COLORS) {
            colors.add(color)
        }
        ds.colors = colors
        return BarData(ds)
    }

    companion object {
        fun newInstance(): IncreaseCounterDeviceFragment {
            return IncreaseCounterDeviceFragment()
        }
    }
}
