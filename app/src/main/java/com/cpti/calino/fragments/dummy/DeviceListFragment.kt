package com.cpti.calino.fragments.dummy

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.cpti.calino.R
import com.cpti.calino.fragments.dummy.data.DeviceContent

class DeviceListFragment : Fragment() {
    private var mListener: OnDeviceChosenListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater!!.inflate(R.layout.fragment_device_list, container, false)

        if (view is RecyclerView) {
            val context = view.getContext()
            view.layoutManager = LinearLayoutManager(context)
            view.adapter = DeviceListRecyclerViewAdapter(DeviceContent.items, mListener)
        }
        return view
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context is OnDeviceChosenListener) {
            mListener = context
        } else {
            throw RuntimeException(context!!.toString() + " must implement OnListFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
    }

    interface OnDeviceChosenListener {
        fun onDeviceChosen(item: DeviceContent.DeviceItem)
    }

    companion object {
        fun newInstance(): DeviceListFragment {
            return DeviceListFragment()
        }
    }
}
