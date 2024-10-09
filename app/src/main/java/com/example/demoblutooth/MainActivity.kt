package com.example.demoblutooth

import DeviceAdapter
import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.IOException
import java.util.UUID

class MainActivity : AppCompatActivity() {
    private val NAME="Bluetooth"
    private val MY_UUID: UUID = UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66")
    private var acceptThread: AcceptThead? = null
    private lateinit var btnOn: Button
    private lateinit var btnListDevice: Button
    private lateinit var btnScan: Button
    private lateinit var btnScanBLE: Button
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var btEnablingIntent: Intent
    private var requestCodeForEnable: Int = 1
    private lateinit var recyclerView: RecyclerView
    private val device = mutableListOf<BluetoothDevice>()  // Danh sách thiết bị
    private lateinit var deviceAdapter: DeviceAdapter // Adapter để hiển thị thiết bị

    private val receiver = object : BroadcastReceiver() {
        @SuppressLint("MissingPermission", "NotifyDataSetChanged")
        override fun onReceive(context: Context?, intent: Intent) {
            val action: String = intent.action ?: return

            when (action) {
                BluetoothDevice.ACTION_FOUND -> {
                    val deviceFound: BluetoothDevice =
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE) ?: return
                    val deviceName = deviceFound.name
                    val deviceAddress = deviceFound.address

                    if(!deviceName.isNullOrEmpty()) {
                        // Thêm thiết bị vào danh sách
                        // Kiểm tra nếu thiết bị đã có trong danh sách chưa
                        if (!device.contains(deviceFound)) {
                            device.add(deviceFound)
                            deviceAdapter.notifyDataSetChanged()  // Cập nhật danh sách
                        }
                    }
                }

                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    Toast.makeText(context, "Discovery finished", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    private val requestBluetoothPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Quyền được cấp, kích hoạt Bluetooth
            bluetoothOnMethod()
        } else {
            // Quyền bị từ chối
            Toast.makeText(applicationContext, "Permission Denied", Toast.LENGTH_LONG).show()
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

        btnListDevice = findViewById(R.id.btnShowdevice) // Đảm bảo ID này chính xác
        btnScan = findViewById(R.id.btnScan)
        btnScanBLE = findViewById(R.id.btnScanBLE)

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        btEnablingIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        deviceAdapter = DeviceAdapter(device) { device ->
            // Xử lý khi nhấn nút kết nối (nếu có)
            connectToDevice(device)
        }
        recyclerView.adapter = deviceAdapter


        // Kiểm tra và yêu cầu quyền BLUETOOTH_CONNECT
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
            == PackageManager.PERMISSION_GRANTED
        ) {
            // Quyền đã được cấp, kích hoạt Bluetooth
            bluetoothOnMethod()
        } else {
            // Yêu cầu quyền
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

    private fun startBLEscan() {
        //val bluetoothManager = getSystemService()
    }

    private fun connectToDevice(device: BluetoothDevice) {
        acceptThread = AcceptThead(device)
        acceptThread?.start()
    }


//    @SuppressLint("MissingPermission")
//    private inner class AcceptThead(device: BluetoothDevice): Thread() {
//        private val mmServerSocket: BluetoothServerSocket? by lazy (LazyThreadSafetyMode.NONE) {
//            bluetoothAdapter.listenUsingRfcommWithServiceRecord(NAME, MY_UUID)
//        }
//
//        override fun run(){
//            // Keep listening until exception occurs or a socket is returned.
//            var shouldLoop = true
//            while (shouldLoop) {
//                val socket: BluetoothSocket? = try {
//                    mmServerSocket?.accept()
//                } catch (e: IOException) {
//                    Log.e(TAG, "Socket's accept() method failed", e)
//                    shouldLoop = false
//                    null
//                }
//                socket?.also {
//                    manageMyConnectedSocket(it)
//                    mmServerSocket?.close()
//                    shouldLoop = false
//                }
//            }
//        }
//
//        // Closes the connect socket and causes the thread to finish.
//        fun cancel() {
//            try {
//                mmServerSocket?.close()
//            } catch (e: IOException) {
//                Log.e(TAG, "Could not close the connect socket", e)
//            }
//        }
//    }



    @SuppressLint("MissingPermission")
    private inner class AcceptThead(device: BluetoothDevice) : Thread() {

        private val mmSocket: BluetoothSocket? by lazy(LazyThreadSafetyMode.NONE) {
            device.createRfcommSocketToServiceRecord(MY_UUID)
        }

        public override fun run() {
            bluetoothAdapter.cancelDiscovery()

            mmSocket?.let { socket ->
                socket.connect()
                manageMyConnectedSocket(socket)
            }
        }

        // Closes the client socket and causes the thread to finish.
        fun cancel() {
            try {
                mmSocket?.close()
            } catch (e: IOException) {
                Log.e(TAG, "Could not close the client socket", e)
            }
        }
    }

    // xu ly thanh cong tu thiet bị khac
    @SuppressLint("MissingPermission")
    private fun manageMyConnectedSocket(it: BluetoothSocket) {
        runOnUiThread {
            Toast.makeText(this, "Device connect : ${it.remoteDevice.name}",Toast.LENGTH_SHORT).show()
        }
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
        Toast.makeText(this, "Scanning for devices...", Toast.LENGTH_SHORT).show()
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


    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)  // Hủy đăng ký Receiver khi không cần thiết
    }
}