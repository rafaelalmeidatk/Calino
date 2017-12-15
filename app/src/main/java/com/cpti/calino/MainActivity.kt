package com.cpti.calino

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.util.Log
import android.widget.Toast
import com.cpti.calino.enums.ConnectionStateEnum
import com.cpti.calino.enums.DeviceTypeEnum
import com.cpti.calino.fragments.IRBarDeviceFragment
import com.cpti.calino.fragments.DeviceListFragment
import com.cpti.calino.fragments.IncreaseCounterDeviceFragment
import com.cpti.calino.fragments.data.DeviceContent
import com.cpti.calino.model.DeviceData
import com.felhr.usbserial.UsbSerialDevice
import com.felhr.usbserial.UsbSerialInterface
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_main.*
import java.io.UnsupportedEncodingException
import java.nio.charset.Charset

private const val ACTION_USB_PERMISSION = "com.cpti.calino.USB_PERMISSION"
private const val BAUD_RATE = 57600

class MainActivity : AppCompatActivity(), DeviceListFragment.OnDeviceChosenListener {
    private lateinit var usbManager     : UsbManager
    private var usbDevice               : UsbDevice? = null
    private var serialPort              : UsbSerialDevice? = null
    private var usbDeviceConnection     : UsbDeviceConnection? = null
    private var connectionState         : ConnectionStateEnum = ConnectionStateEnum.CLOSED

    private var mListener: OnReceiveDataListener? = null
    private var currentFragment : Fragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Usb Manager
        usbManager = getSystemService(Context.USB_SERVICE) as UsbManager

        openConnectionBtn.setOnClickListener({ startCommunication() })
        closeConnectionBtn.setOnClickListener({ stopCommunication() })
        resetConnectionBtn.setOnClickListener({ resetCommunication() })

        setConnectionState(ConnectionStateEnum.CLOSED)

        val initialFragment = DeviceListFragment.newInstance()

        supportFragmentManager.beginTransaction()
                .add(R.id.fragment_container, initialFragment).commit()

        currentFragment = initialFragment
    }

    override fun onResume() {
        super.onResume()
        val filter = IntentFilter()
        filter.addAction(ACTION_USB_PERMISSION)
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED)
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED)
        registerReceiver(broadcastReceiver, filter)
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(broadcastReceiver)
    }

    private fun setConnectionState(state: ConnectionStateEnum) {

        connectionState = state
        val connected = state == ConnectionStateEnum.OPEN

        val statusString = getString(
            if (connected)
                R.string.open_connection_status
            else
                R.string.closed_connection_status
        )
        connectionStatusLabel.text = String.format(
            getString(R.string.connection_status_label) + statusString
        )

        connectionStatusLabel.setTextColor(ContextCompat.getColor(
            this,
            if (connected) R.color.green else R.color.red
        ))

        openConnectionBtn.isEnabled = !connected
        closeConnectionBtn.isEnabled = connected
    }

    private fun changeDeviceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        currentFragment = fragment
        mListener = currentFragment as OnReceiveDataListener
    }

    override fun onDeviceChosen(item: DeviceContent.DeviceItem) {
        when(item.type) {
            DeviceTypeEnum.INCREASE_COUNTER_DEVICE ->
                changeDeviceFragment(IncreaseCounterDeviceFragment.newInstance())
            DeviceTypeEnum.IRBAR_DEVICE ->
                changeDeviceFragment(IRBarDeviceFragment.newInstance())
        }
    }

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent == null) {
                return
            }
            catLog("on receive, " + intent.action)
            if (intent.action == ACTION_USB_PERMISSION) {
                catLog("usb permission request")
                val granted = intent.extras.getBoolean(UsbManager.EXTRA_PERMISSION_GRANTED)
                if (granted) {
                    usbDeviceConnection = usbManager.openDevice(usbDevice)
                    serialPort = UsbSerialDevice.createUsbSerialDevice(usbDevice, usbDeviceConnection)
                    serialPort?.let { sp ->
                        if (sp.open()) {
                            sp.setBaudRate(BAUD_RATE)
                            sp.setDataBits(UsbSerialInterface.DATA_BITS_8)
                            sp.setStopBits(UsbSerialInterface.STOP_BITS_1)
                            sp.setParity(UsbSerialInterface.PARITY_NONE)
                            sp.setFlowControl(UsbSerialInterface.FLOW_CONTROL_OFF)
                            sp.read(receiveDataCallback)
                            catLog("Serial connection opened!")
                            setConnectionState(ConnectionStateEnum.OPEN)
                        } else {
                            catLog("Port not open!")
                        }
                    }
                } else {
                    Toast.makeText(applicationContext, getString(R.string.permission_not_granted), Toast.LENGTH_SHORT).show()
                }
            } else if (intent.action == UsbManager.ACTION_USB_DEVICE_ATTACHED) {
                catLog("broadcastReceive attach")
                Handler().postDelayed({
                    runOnUiThread { startCommunication() }
                }, 100)
            } else if (intent.action == UsbManager.ACTION_USB_DEVICE_DETACHED) {
                catLog("broad detached")
                stopCommunication()
            }
        }
    }

    private fun startCommunication() {
        catLog("startCommunication")
        usbDeviceConnection = null
        usbDevice = null
        val usbDevices = usbManager.deviceList
        if (!usbDevices.isEmpty()) {
            for (entry in usbDevices.entries) {
                val device = entry.value
                val deviceVendorId = device.vendorId
                if (deviceVendorId == 0x2341) {
                    usbDevice = device
                    val pendingIntent = PendingIntent.getBroadcast(this, 0, Intent(ACTION_USB_PERMISSION), 0)
                    usbManager.requestPermission(usbDevice, pendingIntent)
                    break
                }
            }
        } else {
            Toast.makeText(this, getString(R.string.no_device_found), Toast.LENGTH_SHORT).show()
        }
    }

    private fun stopCommunication() {
        catLog("stopCommunication")
        if (serialPort == null) {
            catLog("Serial port is null")
        }
        serialPort?.let { sp ->
            setConnectionState(ConnectionStateEnum.CLOSED)
            sp.close()
            catLog("Serial connection closed!")
        }
    }

    private fun resetCommunication() {
        catLog("resetCommunication")
        stopCommunication()
        startCommunication()
    }

    private fun sendDataToArduino() {
        val realm = Realm.getDefaultInstance()
        realm.refresh()
        var data = "{"
        val devicesData = realm.where(DeviceData::class.java).findAll()
        for (deviceData in devicesData) {
            data += "[${deviceData.id},${deviceData.value}]"
        }
        data += "}"
        catLog("sending to arduino: $data")
        serialPort?.write(data.toByteArray())
    }

    var datastream = ""

    internal val receiveDataCallback = UsbSerialInterface.UsbReadCallback { arg0 ->
        val data: String?
        try {
            data = String(arg0, Charset.defaultCharset())
            datastream += data
            for (byt in arg0) {
                if (byt == (0xA).toByte()) {
                    if (isDataRequest(datastream)) {
                        catLog("datastream: $datastream")
                        sendDataToArduino()
                    } else {
                        parseStream(datastream)
                        sendDataToFragmentListeners()
                    }
                    datastream = ""
                    continue
                }
            }
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }
    }

    private fun isDataRequest(stream: String): Boolean {
        val rg = Regex(".?\\{data\\}.?")
        val match = rg.findAll(stream)
        return match.any()
    }

    private var streamData: HashMap<Int, MutableList<Float>> = HashMap()

    private fun parseStream(stream: String) {
        catLog("parseStream: $stream")
        streamData.clear()
        val rg = "\\{\\{((?:\\[\\d+,\\d+\\])+)\\}\\}".toRegex()
        val match = rg.findAll(stream)
        for (res in match.iterator()) {
            res.groups[1]?.let { group ->
                val pairsRg = "\\[(\\d+),(\\d+)\\]".toRegex()
                val pairs = group.value
                val pairsMatch = pairsRg.findAll(pairs)
                for (pairsRes in pairsMatch) {
                    val id = pairsRes.groupValues[1].toInt()
                    val value = pairsRes.groupValues[2].toFloat()
                    streamData.getOrPut(id, {mutableListOf()}).add(value)
                }
            }
        }
        catLog(streamData.toString())
    }

    private fun sendDataToFragmentListeners() {
        mListener?.let { listener ->
            val deviceId = listener.getDeviceId()
            if (streamData.containsKey(deviceId)) {
                catLog("device id: $deviceId stream data: ${streamData[deviceId]} listener: $mListener")
                val finalStreamData = streamData[deviceId]!!.toList()
                runOnUiThread {
                    mListener?.onReceiveData(
                            finalStreamData
                    )
                }
            }
        }
    }

    private fun catLog(text: String) {
        Log.d("Calino", text)
    }

    interface OnReceiveDataListener {
        fun getDeviceId(): Int
        fun onReceiveData(data: List<Float>)
    }
}
