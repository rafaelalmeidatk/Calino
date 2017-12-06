package com.cpti.calino.fragments.dummy.data

import com.cpti.calino.enums.DeviceTypeEnum
import java.util.ArrayList

/**
 * Created by rafael on 01/12/17.
 */

object DeviceContent {

    val items: MutableList<DeviceItem> = ArrayList()

    init {
        addItem(DeviceItem("1", DeviceTypeEnum.INCREASE_COUNTER_DEVICE))
    }

    private fun addItem(item: DeviceItem) {
        items.add(item)
    }

    class DeviceItem(val id: String, val type: DeviceTypeEnum) {
        override fun toString(): String {
            return when(type) {
                DeviceTypeEnum.INCREASE_COUNTER_DEVICE -> "Increase Counter Device"
            }
        }
    }
}
