package com.cpti.calino.fragments

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.TextView
import com.cpti.calino.MainActivity

import com.cpti.calino.R
import com.cpti.calino.model.DeviceData
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.utils.ColorTemplate
import io.realm.Realm
import kotlinx.android.synthetic.main.fragment_increase_counter_device.view.*
import kotlinx.android.synthetic.main.fragment_irbar_device.view.*


class IRBarDeviceFragment : Fragment(), MainActivity.OnReceiveDataListener,
        SeekBar.OnSeekBarChangeListener  {

    override fun getDeviceId(): Int {
        return 2
    }

    private lateinit var seekbar: SeekBar
    private lateinit var seekbarValue: TextView
    private lateinit var realm: Realm
    private var chart: BarChart? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater!!.inflate(R.layout.fragment_irbar_device, container, false)

        realm = Realm.getDefaultInstance()

        // create a new chart
        chart = view.irbar_barchart
        chart?.let { c ->
            c.axisLeft.axisMinimum = 0f
            c.axisLeft.axisMaximum = 1000f
            c.axisRight.isEnabled = false
            c.xAxis.isEnabled = false
            c.description.isEnabled = false
        }

        // init the seekbar
        seekbarValue = view.irbar_seekbar_value
        seekbar = view.irbar_seekbar
        seekbar.setOnSeekBarChangeListener(this)
        seekbar.irbar_seekbar.max = 1000
        loadInitialData()

        return view
    }

    private fun loadInitialData() {
        val data = realm.where(DeviceData::class.java).equalTo("id", getDeviceId()).findFirst()
        var value = 0L
        if (data == null) {
            realm.executeTransaction {
                val dbdata = realm.createObject<DeviceData>(DeviceData::class.java, getDeviceId())
                dbdata.value = 0
            }
        } else {
            value = data.value
        }
        seekbar.progress = value.toInt()
        seekbarValue.text = value.toString()
    }

    override fun onReceiveData(data: List<Float>) {
        chart?.data = createData(data)
        chart?.invalidate()
    }

    private fun createData(data: List<Float>): BarData {
        val entries = ArrayList<BarEntry>()

        for (i in data.indices) {
            entries.add(BarEntry(i.toFloat(), data[i]))
        }
        val ds = BarDataSet(entries, "IR Readings")
        val colors = ArrayList<Int>()
        for (color in ColorTemplate.VORDIPLOM_COLORS) {
            colors.add(color)
        }
        ds.colors = colors
        return BarData(ds)
    }

    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        seekbarValue.text = progress.toString()
    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) { }

    override fun onStopTrackingTouch(seekBar: SeekBar?) {
        val data = realm.where(DeviceData::class.java).equalTo("id", getDeviceId()).findFirst()!!
        realm.executeTransaction {
            data.value = seekBar!!.progress.toLong()
            seekbarValue.text = data.value.toString()
        }
    }

    companion object {
        val TAG: String = IRBarDeviceFragment::class.java.simpleName

        fun newInstance(): IRBarDeviceFragment {
            return IRBarDeviceFragment()
        }
    }
}// Required empty public constructor
