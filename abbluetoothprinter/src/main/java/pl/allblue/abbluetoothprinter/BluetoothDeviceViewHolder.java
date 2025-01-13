package pl.allblue.abbluetoothprinter;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import pl.allblue.abbluetoothprinter.databinding.ItemBluetoothDeviceBinding;

public class BluetoothDeviceViewHolder extends RecyclerView.ViewHolder {
    private ItemBluetoothDeviceBinding b;

    public BluetoothDeviceViewHolder(@NonNull View itemView) {
        super(itemView);

        b = ItemBluetoothDeviceBinding.bind(itemView);
    }

    public ItemBluetoothDeviceBinding b() {
        return b;
    }
}
