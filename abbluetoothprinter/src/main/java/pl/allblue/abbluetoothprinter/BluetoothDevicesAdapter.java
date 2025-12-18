package pl.allblue.abbluetoothprinter;

import android.bluetooth.BluetoothDevice;
import android.os.ParcelUuid;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class BluetoothDevicesAdapter extends
        RecyclerView.Adapter<BluetoothDeviceViewHolder> {
    private OnBluetoothDeviceSelected listeners_OnDeviceSelected;
    private List<BluetoothDeviceInfo> list;

    public BluetoothDevicesAdapter() {
        this.listeners_OnDeviceSelected = null;
        this.list = new ArrayList<>();
    }

    public void addItem(BluetoothDeviceInfo deviceInfo) {
        list.add(deviceInfo);
        notifyItemInserted(list.size() - 1);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public void setListeners_OnDeviceSelected(
            OnBluetoothDeviceSelected onDeviceSelected) {
        listeners_OnDeviceSelected = onDeviceSelected;
    }

    /* RecyclerView.Adapter Overrides */
    @NonNull
    @Override
    public BluetoothDeviceViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
            int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.item_bluetooth_device, parent, false);

        return new BluetoothDeviceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BluetoothDeviceViewHolder holder,
            int position) {
        BluetoothDeviceInfo bdi = list.get(position);
        try {
            String deviceName = bdi.device.getName() == null ? "-" : bdi.device.getName();
            String deviceAddress = bdi.device.getAddress() == null ? "" : ("[" +
                    bdi.device.getAddress() + "]");
            String displayName = deviceName + (deviceAddress.isEmpty() ? "" : (" " + deviceAddress));

            holder.b().deviceName.setText(displayName);
        } catch (SecurityException e) {
            holder.b().deviceName.setText("<No Bluetooth Permission>");
        }

        holder.b().deviceName.setOnClickListener(v -> {
            if (listeners_OnDeviceSelected != null)
                listeners_OnDeviceSelected.onDeviceSelected(list.get(position));
        });
    }
    /* / RecyclerView.Adapter Overrides */

    public interface OnBluetoothDeviceSelected {
        void onDeviceSelected(BluetoothDeviceInfo deviceInfo);
    }
}
