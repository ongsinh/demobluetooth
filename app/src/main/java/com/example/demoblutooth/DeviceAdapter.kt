import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.demoblutooth.R

class DeviceAdapter(
    private val deviceList: List<BluetoothDevice>,
    private val onConnectClick : (BluetoothDevice) ->Unit
): RecyclerView.Adapter<DeviceAdapter.DeviceViewHolder>() {
    inner class DeviceViewHolder(view: View) : RecyclerView.ViewHolder(view){
        private val deviceTextview: TextView = view.findViewById(R.id.tv_device_bluetooth)
        private val btnConncet : Button = view.findViewById(R.id.btnConnect)

        @SuppressLint("MissingPermission")
        fun bind(device: BluetoothDevice){
            deviceTextview.text = device.name ?: "Unknow"
            btnConncet.setOnClickListener {
                onConnectClick(device)
            }
        }

    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item_text,parent,false)
        return DeviceViewHolder(view)
    }

    override fun getItemCount(): Int {
        return deviceList.size
    }

    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
        holder.bind(deviceList[position])
    }

}
