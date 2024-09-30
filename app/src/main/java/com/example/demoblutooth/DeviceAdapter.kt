import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

class DeviceAdapter(context: Context, devices: MutableList<String>) : ArrayAdapter<String>(context, 0, devices) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val device = getItem(position)
        val view = convertView ?: LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_1, parent, false)
        val textView = view.findViewById<TextView>(android.R.id.text1)
        textView.text = device
        return view


    }
}
