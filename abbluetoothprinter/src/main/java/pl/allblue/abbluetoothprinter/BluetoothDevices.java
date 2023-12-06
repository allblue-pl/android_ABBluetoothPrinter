package pl.allblue.abbluetoothprinter;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
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


    public BluetoothDevices(Activity activity, int[] supported_device_classes)
    {
        this.supportedDeviceClasses = supported_device_classes;

        String[] permissions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION,android.Manifest.permission.BLUETOOTH, android.Manifest.permission.BLUETOOTH_SCAN, android.Manifest.permission.BLUETOOTH_ADMIN, android.Manifest.permission.BLUETOOTH_CONNECT};
        for (String permission : permissions) {
            if (activity.checkSelfPermission(permission) !=
                    PackageManager.PERMISSION_GRANTED) {
                Log.d("BTDeviceTest", "No Permission.");
                activity.requestPermissions(permissions, 0);
                return;
            } else
                Log.d("BTDeviceTest", "Yes Permission.");
        }

        this.adapter = BluetoothAdapter.getDefaultAdapter();
        try {
            for (BluetoothDevice bt_device : this.adapter.getBondedDevices())
                this.devices_Add(activity, bt_device, true);
        } catch (SecurityException e) {
            Toast.ShowMessage(activity, activity.getString(R.string.Bluetooth_BluetoothPermissionError));
        }
    }

    public void discover(Activity activity)
    {
//        this.finishDiscovery(activity);
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

        Log.d("BTDeviceTest", "Created receiver.");

        this.adapter = BluetoothAdapter.getDefaultAdapter();

        this.receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d("BTDeviceTest", "sth is happening: ");

                final String action = intent.getAction();
                if (BluetoothDevice.ACTION_FOUND.equals(intent.getAction())) {
                    BluetoothDevice device = intent.getParcelableExtra(
                            BluetoothDevice.EXTRA_DEVICE);

                    self.devices_Add(activity, device, false);
                }
            }
        };

        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        activity.registerReceiver(this.receiver, filter);

        try {
            Log.d("BTDeviceTest", "Before");
            this.adapter.startDiscovery();
            Log.d("BTDeviceTest", "After");
        } catch (SecurityException e) {
            Toast.ShowMessage(activity, activity.getString(R.string.Bluetooth_BluetoothPermissionError));
        }
    }

    private void devices_Add(Activity activity, BluetoothDevice device, boolean is_paired)
    {
        try {
            if (!this.isDeviceClassSupported(device.getBluetoothClass()
                    .getMajorDeviceClass()))
                return;
        } catch (SecurityException e) {
            Toast.ShowMessage(activity, activity.getString(R.string.Bluetooth_BluetoothPermissionError));
        }

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
