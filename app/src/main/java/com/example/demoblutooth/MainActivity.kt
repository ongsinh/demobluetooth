package com.example.demoblutooth

import DeviceAdapter
import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.IOException
import java.util.UUID

@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity() {
    private lateinit var btnOn: Button
    private lateinit var btnListDevice: Button
    private lateinit var btnScan: Button
    private lateinit var btnScanBLE: Button
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var btEnablingIntent: Intent
    private var requestCodeForEnable: Int = 1
    private lateinit var recyclerView: RecyclerView
    private val device = mutableListOf<BluetoothDevice>()
    private lateinit var deviceAdapter: DeviceAdapter
    private var bluetoothService : BluetoothService ?= null

    private val receiver = object : BroadcastReceiver() {
        @SuppressLint("MissingPermission", "NotifyDataSetChanged")
        override fun onReceive(context: Context?, intent: Intent) {
            val action: String = intent.action ?: return

            when (action) {
                BluetoothDevice.ACTION_FOUND -> {
                    val deviceFound: BluetoothDevice =
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE) ?: return
                    val deviceName = deviceFound.name

                    if(!deviceName.isNullOrEmpty() && !device.contains(deviceFound)) {
                        if (!device.contains(deviceFound)) {
                            device.add(deviceFound)
                            deviceAdapter.notifyItemInserted(device.size -1)
                        }
                    }
                }

                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    bluetoothAdapter.startDiscovery()
                }
            }
        }
    }


    private val requestBluetoothPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            startBLEscan()
        } else {
            Toast.makeText(applicationContext, "Permission Denied", Toast.LENGTH_LONG).show()
        }

    }

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(componentName: ComponentName, service: IBinder) {
            bluetoothService = (service as BluetoothService.LocalBinder).getService()
            if (!bluetoothService?.initialize()!!) {
                Log.e("DeviceControlActivity", "Unable to initialize Bluetooth")
                finish()
            }
        }

        override fun onServiceDisconnected(componentName: ComponentName) {
            bluetoothService = null
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activitymain)

        btnOn = findViewById(R.id.btnOnBluetooth)
        recyclerView = findViewById(R.id.recycalview)
        recyclerView.layoutManager = LinearLayoutManager(this)

        btnListDevice = findViewById(R.id.btnShowdevice)
        btnScan = findViewById(R.id.btnScan)
        btnScanBLE = findViewById(R.id.btnScanBLE)

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        btEnablingIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        deviceAdapter = DeviceAdapter(device) { device ->
            val deviceAdress = device.address
            connectToDevice(deviceAdress)
        }
        recyclerView.adapter = deviceAdapter
        val gattServiceIntent = Intent(this, BluetoothService::class.java)
        bindService(gattServiceIntent, serviceConnection, Context.BIND_AUTO_CREATE)



        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
            == PackageManager.PERMISSION_GRANTED
        ) {
            bluetoothOnMethod()
        } else {
            requestBluetoothPermissionLauncher.launch(Manifest.permission.BLUETOOTH_CONNECT)
        }

        btnOn.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
                == PackageManager.PERMISSION_GRANTED
            ) {
                bluetoothOnMethod()
            } else {
                requestBluetoothPermissionLauncher.launch(Manifest.permission.BLUETOOTH_CONNECT)
            }
        }

        btnListDevice.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
                == PackageManager.PERMISSION_GRANTED
            ) {
                listDevice()
            } else {
                requestBluetoothPermissionLauncher.launch(Manifest.permission.BLUETOOTH_CONNECT)
            }
        }

        btnScan.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN)
                == PackageManager.PERMISSION_GRANTED
            ) {
                startBluetoothScan()
            } else {
                requestBluetoothPermissionLauncher.launch(Manifest.permission.BLUETOOTH_SCAN)
            }
        }


        btnScanBLE.setOnClickListener {
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN)
                == PackageManager.PERMISSION_GRANTED){
                startBLEscan()
            }else{
                requestBluetoothPermissionLauncher.launch(Manifest.permission.BLUETOOTH_SCAN)
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun startBLEscan() {
        val bluetoothManager = getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter

        if (bluetoothAdapter.isEnabled) {
            val scanner = bluetoothAdapter.bluetoothLeScanner
            scanner?.startScan(leScanCallBack)
        } else {
            Log.d("BLE Scan", "Bluetooth is not enabled.")
        }

    }

    private fun connectToDevice(deviceAdress : String) {
        bluetoothService?.connect(deviceAdress)
    }




    @SuppressLint("MissingPermission", "NotifyDataSetChanged")
    private fun startBluetoothScan() {
        if (bluetoothAdapter.isDiscovering) {
            bluetoothAdapter.cancelDiscovery()
        }

        device.clear()
        deviceAdapter.notifyDataSetChanged()

        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        registerReceiver(receiver, filter)

        bluetoothAdapter.startDiscovery()
    }

    @SuppressLint("MissingPermission", "NotifyDataSetChanged")
    private fun listDevice() {
        val bondedDevices: Set<BluetoothDevice> = bluetoothAdapter.bondedDevices
        device.clear()

        if (bondedDevices.isEmpty()) {
            Toast.makeText(this, "No bonded devices found", Toast.LENGTH_SHORT).show()
        } else {
            for (bluetoothDevice in bondedDevices) {
                device.add(bluetoothDevice)
            }
        }
        deviceAdapter.notifyDataSetChanged()
    }


    private fun bluetoothOnMethod() {
        if (!bluetoothAdapter.isEnabled) {
            startActivityForResult(btEnablingIntent, requestCodeForEnable)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == requestCodeForEnable) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(applicationContext, "Bluetooth is Enabled", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(
                    applicationContext,
                    "Bluetooth enabling cancelled",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private val leScanCallBack = object : ScanCallback(){
        @SuppressLint("MissingPermission")
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            super.onScanResult(callbackType, result)
            val device = result?.device
            if(!device?.name.isNullOrEmpty()){
                device?.let { deviceAdapter.addDevice(it) }
            }
        }

        @SuppressLint("MissingPermission")
        override fun onBatchScanResults(results: MutableList<ScanResult>?) {
            super.onBatchScanResults(results)
            if (results != null) {
                for(result in results){
                    val device = result?.device
                    if(!device?.name.isNullOrEmpty()){
                        device?.let { deviceAdapter.addDevice(it) }
                    }
                }
            }
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            Log.d("BLE Scan", "Scan error")

        }
    }


    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
        unbindService(serviceConnection)
        bluetoothService = null
    }



}