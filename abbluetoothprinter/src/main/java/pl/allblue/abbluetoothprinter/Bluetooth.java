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

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.os.Build;
import android.util.Log;

import java.lang.reflect.InvocationTargetException;

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

public class Bluetooth
{

    static public boolean Enable(final AppCompatActivity activity, int request_code,
            final OnEnabled listener)
    {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();

        /* Bluetooth Not Supported */
        if (adapter == null) {
            listener.onEnabled(EnableResult.NotSupported);
            return false;
        }

        /* Request Required Permissions */
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

        boolean hasRequiredPermissions = true;
        for (int i = 0; i < requiredPermissions.length; i++) {
            if (ContextCompat.checkSelfPermission(activity, requiredPermissions[i]) !=
                    PackageManager.PERMISSION_GRANTED) {
                hasRequiredPermissions = false;
                break;
            }
        }
        if (!hasRequiredPermissions) {
            ActivityCompat.requestPermissions(activity,
                    requiredPermissions,
                    request_code);

            return false;
        }

        /* Bluetooth Turned Off */
        if (!adapter.isEnabled()) {
            if (adapter.getState() == BluetoothAdapter.STATE_OFF) {
                Intent i = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                activity.startActivityForResult(i, request_code);

                return false;
            } else if (adapter.getState() == BluetoothAdapter.STATE_TURNING_ON) {
                activity.registerReceiver(new BroadcastReceiver()
                {
                    @Override
                    public void onReceive(Context context, Intent intent)
                    {
                        if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(intent.getAction())) {
                            if (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1)
                                    == BluetoothAdapter.STATE_ON) {
                                listener.onEnabled(EnableResult.Enabled);
                                activity.unregisterReceiver(this);
                            }
                        }
                    }
                }, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));

                return false;
            }
        }

        return true;
    }

    static public void PairDevice(final AppCompatActivity activity,
            final BluetoothDevice device, final String pin,
            final OnDevicePairedListener listener)
    {
        activity.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int bond_state = intent.getExtras().getInt(BluetoothDevice.EXTRA_BOND_STATE);
                if (bond_state == BluetoothDevice.BOND_BONDING)
                    return;

                activity.unregisterReceiver(this);

                if (bond_state == BluetoothDevice.BOND_BONDED) {
                    if (listener != null)
                        listener.onPaired(device);
                }
            }
        }, new IntentFilter("android.bluetooth.device.action.BOND_STATE_CHANGED"));

        try {
            activity.registerReceiver(new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    try {
                        if (pin != null) {
                            byte[] pin_bytes = String.valueOf(pin).getBytes();

                            device.getClass().getMethod("setPin", byte[].class)
                                    .invoke(device, pin_bytes);
                            device.getClass().getMethod("setPairingConfirmation",
                                    boolean.class).invoke(device, false);
                        }
                    } catch (InvocationTargetException e) {
                        // Do nothing.
                    } catch (Exception e) {
                        Toast.ShowMessage(activity,
                                activity.getResources().getString(
                                R.string.Bluetooth_Errors_CannotPairDevice));
                        Log.e("Bluetooth", "Cannot pair bluetooth printer.", e);
                        return;
                    }

                    activity.unregisterReceiver(this);
                }
            }, new IntentFilter("android.bluetooth.device.action.PAIRING_REQUEST"));

            device.getClass().getMethod("createBond").invoke(device);
        } catch (Exception e) {
            Toast.ShowMessage(activity,
                    activity.getResources().getString(R.string.Bluetooth_Errors_CannotPairDevice));
            Log.d("Bluetooth", "Cannot pair bluetooth printer.", e);
            return;
        }
    }


    public enum EnableResult
    {
        Enabled,
        Failure,
        NotSupported
    }

    public interface OnEnabled
    {
        void onEnabled(EnableResult result);
    }

    public interface OnDevicePairedListener
    {
        void onPaired(BluetoothDevice device);
    }

}
