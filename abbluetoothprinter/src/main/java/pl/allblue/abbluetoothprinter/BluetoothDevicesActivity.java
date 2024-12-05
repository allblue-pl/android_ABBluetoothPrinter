package pl.allblue.abbluetoothprinter;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;


public class BluetoothDevicesActivity extends ListActivity
{

    static public final String Extras_SupportedDeviceClasses =
            "pl.allblue.bluetooth.BluetoothDevicesList.Extras_SupportedDeviceClasses";
    static public final String Result_Device =
            "pl.allblue.bluetooth.BluetoothDevicesList.Result_Device";
    static public final int PermissionsRequest_DiscoverDevices = 0x01;

    static public void Start(Activity activity, int request_code,
            int[] device_classes)
    {
        Intent intent = new Intent(activity, BluetoothDevicesActivity.class);
        intent.putExtra(BluetoothDevicesActivity.Extras_SupportedDeviceClasses,
                device_classes);

        activity.startActivityForResult(intent, request_code);
    }


    private BluetoothDevices devices = null;
    private ArrayAdapter<String> devicesAdapter = null;


    public void discoverDevices() {
        if (!this.devices.init(this, BluetoothDevicesActivity
                .PermissionsRequest_DiscoverDevices))
            return;

        this.devicesAdapter = new ArrayAdapter<>(this, R.layout.list_item_black);
        this.setListAdapter(this.devicesAdapter);

        final BluetoothDevicesActivity self = this;
        this.devices.setOnDiscoveredListener(device_info -> {
            try {
                self.devicesAdapter.add(device_info.device.getName());
            } catch (SecurityException e) {
                Toast.showMessage(self, self.getString(R.string.bluetooth_bluetooth_permission_error));
            }
            self.devicesAdapter.notifyDataSetInvalidated();
        });
        this.devices.discover(this);
        Toast.showMessage(this, this.getResources().getString(
                R.string.bluetooth_getting_devices_list));
    }

    /* Activity Overrides */
    @Override
    public void onCreate(Bundle saved_instance_state)
    {
        super.onCreate(saved_instance_state);

        this.devices = new BluetoothDevices(this.getIntent().getExtras()
                .getIntArray(BluetoothDevicesActivity
                .Extras_SupportedDeviceClasses));
        this.discoverDevices();
    }

    @Override
    public void onDestroy()
    {
        this.devices.finishDiscovery(this);

        super.onDestroy();
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id)
    {
            Intent intent = new Intent();
            intent.putExtra(BluetoothDevicesActivity.Result_Device,
                    this.devices.getDeviceInfos().get(position));

            this.setResult(Activity.RESULT_OK, intent);
            this.finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
            @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PermissionsRequest_DiscoverDevices:
                for (int i = 0; i < grantResults.length; i++) {
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        Toast.showMessage(this, getString(
                                R.string.bluetooth_bluetooth_permission_error));
                        return;
                    }
                }
                this.discoverDevices();
                break;
            default:
                // Do nothing.
                break;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}