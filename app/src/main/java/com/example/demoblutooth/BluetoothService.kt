package com.example.demoblutooth

import android.annotation.SuppressLint
import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothProfile
import android.content.Intent
import android.os.Binder
import android.os.IBinder

class BluetoothService : Service() {
    private var bluetoothGatt: BluetoothGatt? = null
    private var bluetoothAdapter: BluetoothAdapter ? = null

    private val binder = LocalBinder()

    override fun onBind(intent: Intent): IBinder? {
        return binder
    }

    inner class LocalBinder : Binder() {
        fun getService() : BluetoothService {
            return this@BluetoothService
        }
    }

    fun initialize():Boolean{
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        return bluetoothAdapter !=null
    }

    @SuppressLint("MissingPermission")
    fun connect (deviceAdress : String): Boolean {
        val device = bluetoothAdapter?.getRemoteDevice(deviceAdress) ?: return false
        bluetoothGatt = device.connectGatt(this, false, gattCallback)
        return true

    }

    private val gattCallback = object : BluetoothGattCallback() {
        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            if(newState == BluetoothProfile.STATE_CONNECTED){
                gatt?.discoverServices()
            }else if (newState == BluetoothProfile.STATE_CONNECTED){

            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            if(status == BluetoothGatt.GATT_SUCCESS){

            }
        }
        
    }
}