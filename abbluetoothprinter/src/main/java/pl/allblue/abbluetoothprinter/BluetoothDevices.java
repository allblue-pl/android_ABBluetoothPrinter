package pl.allblue.abbluetoothprinter;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Required permissions:
 *  - <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
 *  - <uses-permission android:name="android.permission.BLUETOOTH" />
 *  - <uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />
 */
public class BluetoothDevices
{

    private int[] supportedDeviceClasses = null;

    private List<BluetoothDeviceInfo> deviceInfos = new ArrayList<>();
    private BroadcastReceiver receiver = null;
    private BluetoothAdapter adapter = null;

    private OnDiscoveredListener
            listeners_OnBluetoothDeviceDiscovered = null;


    public BluetoothDevices(int[] supported_device_classes)
    {
        this.supportedDeviceClasses = supported_device_classes;

        this.adapter = BluetoothAdapter.getDefaultAdapter();
        for (BluetoothDevice bt_device : this.adapter.getBondedDevices())
            this.devices_Add(bt_device, true);
    }

    public void discover(Activity activity)
    {
        this.finishDiscovery(activity);
        this.createReceiver(activity);
    }

    public void finishDiscovery(Activity activity)
    {
        if (this.receiver != null)
            activity.unregisterReceiver(this.receiver);
    }

    public List<BluetoothDeviceInfo> getDeviceInfos()
    {
        return this.deviceInfos;
    }

    public void setOnDiscoveredListener(OnDiscoveredListener listener)
    {
        this.listeners_OnBluetoothDeviceDiscovered = listener;
        for (BluetoothDeviceInfo device_info : this.deviceInfos)
            listener.onDiscovered(device_info);
    }


    private void createReceiver(Activity activity)
    {
        final BluetoothDevices self = this;

        this.receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                final String action = intent.getAction();
                if (BluetoothDevice.ACTION_FOUND.equals(intent.getAction())) {
                    BluetoothDevice device = intent.getParcelableExtra(
                            BluetoothDevice.EXTRA_DEVICE);

                    self.devices_Add(device, false);
                }
            }
        };
        activity.registerReceiver(this.receiver, new IntentFilter(
                BluetoothDevice.ACTION_FOUND));

        this.adapter.startDiscovery();
    }

    private void devices_Add(BluetoothDevice device, boolean is_paired)
    {
        if (!this.isDeviceClassSupported(device.getBluetoothClass()
                .getMajorDeviceClass()))
            return;

        if (!this.devices_Exists(device)) {
            BluetoothDeviceInfo device_info = new BluetoothDeviceInfo(device,
                    is_paired);
            this.deviceInfos.add(device_info);
            if (this.listeners_OnBluetoothDeviceDiscovered != null) {
                this.listeners_OnBluetoothDeviceDiscovered
                        .onDiscovered(device_info);
            }
        }
    }

    public boolean devices_Exists(BluetoothDevice device)
    {
        for (BluetoothDeviceInfo t_device_info : this.deviceInfos) {
            if (device.getAddress().equals(t_device_info.device.getAddress()))
                return true;
        }

        return false;
    }

    private boolean isDeviceClassSupported(int device_class)
    {
        if (this.supportedDeviceClasses == null)
            return true;

        for (int supported_device_class : this.supportedDeviceClasses) {
            if (device_class == supported_device_class)
                return true;
        }

        return false;
    }



    public interface OnDiscoveredListener
    {

        void onDiscovered(BluetoothDeviceInfo device_info);

    }

}
