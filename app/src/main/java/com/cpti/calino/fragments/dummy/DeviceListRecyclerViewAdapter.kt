package com.cpti.calino.fragments.dummy

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.cpti.calino.R

import com.cpti.calino.fragments.dummy.data.DeviceContent

class DeviceListRecyclerViewAdapter(private val mValues: List<DeviceContent.DeviceItem>, private val mListener: DeviceListFragment.OnDeviceChosenListener?) : RecyclerView.Adapter<DeviceListRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.fragment_device, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.mItem = mValues[position]
        holder.mIdView.text = mValues[position].id
        holder.mContentView.text = mValues[position].toString()

        holder.mView.setOnClickListener {
            if (mListener != null) {
                holder.mItem?.let { item -> mListener.onDeviceChosen(item) }
            }
        }
    }

    override fun getItemCount(): Int {
        return mValues.size
    }

    inner class ViewHolder(val mView: View) : RecyclerView.ViewHolder(mView) {
        val mIdView: TextView = mView.findViewById<TextView>(R.id.id) as TextView
        val mContentView: TextView = mView.findViewById<TextView>(R.id.content) as TextView
        var mItem: DeviceContent.DeviceItem? = null

        override fun toString(): String {
            return super.toString() + " '" + mContentView.text + "'"
        }
    }
}
