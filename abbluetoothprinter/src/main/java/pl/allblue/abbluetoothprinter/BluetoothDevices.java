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
import android.os.Build;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Required permissions:
 * <uses-permission android:name="android.permission.BLUETOOTH_CONNECT" />
 * <uses-permission android:name="android.permission.BLUETOOTH_SCAN"
 *         android:usesPermissionFlags="neverForLocation" />
 * <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
 * <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
 * <uses-permission android:name="android.permission.BLUETOOTH" />
 * <uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />
 */
public class BluetoothDevices
{

    private int[] supportedDeviceClasses = null;

    private List<BluetoothDeviceInfo> deviceInfos = new ArrayList<>();
    private BroadcastReceiver receiver = null;
    private BluetoothAdapter adapter = null;

    private OnDiscoveredListener
            listeners_OnBluetoothDeviceDiscovered = null;


    public BluetoothDevices(int[] supported_device_classes) {
        this.supportedDeviceClasses = supported_device_classes;
    }

    public void startDiscovery(Activity activity) {
//        this.finishDiscovery(activity);
        this.createReceiver(activity);
    }

    public void finishDiscovery(Activity activity) {
        if (this.adapter != null) {
            try {
                adapter.cancelDiscovery();
            } catch (SecurityException e) {
                Log.e("BluetoothDevices",
                        "Cannot cancel discovering bluetooth devices.", e);
            }
        }
        if (this.receiver != null)
            activity.unregisterReceiver(this.receiver);
    }

    public List<BluetoothDeviceInfo> getDeviceInfos() {
        return this.deviceInfos;
    }

    public boolean init(Activity activity) {
        String[] requiredPermissions = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            requiredPermissions = new String[] {
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.BLUETOOTH_SCAN,
            };
        } else {
            requiredPermissions = new String[] {
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.BLUETOOTH_ADMIN,

            };
        }

        for (String permission : requiredPermissions) {
            if (activity.checkSelfPermission(permission) !=
                    PackageManager.PERMISSION_GRANTED)
                return false;
        }

        this.adapter = BluetoothAdapter.getDefaultAdapter();
        try {
            for (BluetoothDevice bt_device : this.adapter.getBondedDevices())
                this.devices_Add(activity, bt_device, true);
        } catch (SecurityException e) {
            Log.e("BluetoothDevices", "Cannot get bonded devices.", e);
            return false;
        }

        return true;
    }

    public void setOnDeviceDiscoveredListener(OnDiscoveredListener listener) {
        this.listeners_OnBluetoothDeviceDiscovered = listener;
    }


    private void createReceiver(Activity activity) {
        final BluetoothDevices self = this;

        this.adapter = BluetoothAdapter.getDefaultAdapter();

        this.receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
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
            this.adapter.startDiscovery();
        } catch (SecurityException e) {
            Toast.showMessage(activity, activity.getString(R.string.bluetooth_bluetooth_permission_error));
        }
    }

    private void devices_Add(Activity activity, BluetoothDevice device, boolean is_paired) {
        try {
            if (!this.isDeviceClassSupported(device.getBluetoothClass()
                    .getMajorDeviceClass()))
                return;
        } catch (SecurityException e) {
            Toast.showMessage(activity, activity.getString(R.string.bluetooth_bluetooth_permission_error));
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

    public boolean devices_Exists(BluetoothDevice device) {
        for (BluetoothDeviceInfo t_device_info : this.deviceInfos) {
            if (device.getAddress().equals(t_device_info.device.getAddress()))
                return true;
        }

        return false;
    }

    private boolean isDeviceClassSupported(int device_class) {
        if (this.supportedDeviceClasses == null)
            return true;

        for (int supported_device_class : this.supportedDeviceClasses) {
            if (device_class == supported_device_class)
                return true;
        }

        return false;
    }



    public interface OnDiscoveredListener {
        void onDiscovered(BluetoothDeviceInfo device_info);
    }

}
