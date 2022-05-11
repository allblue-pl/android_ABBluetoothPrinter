package pl.allblue.abbluetoothprinter;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;


public class BluetoothDevicesActivity extends ListActivity
{

    static public final String Extras_SupportedDeviceClasses =
            "pl.allblue.bluetooth.BluetoothDevicesList.Extras_SupportedDeviceClasses";
    static public final String Result_Device =
            "pl.allblue.bluetooth.BluetoothDevicesList.Result_Device";

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


    /* Activity Overrides */
    @Override
    public void onCreate(Bundle saved_instance_state)
    {
        super.onCreate(saved_instance_state);

        this.devices = new BluetoothDevices(this, this.getIntent().getExtras().getIntArray(
                BluetoothDevicesActivity.Extras_SupportedDeviceClasses));

        this.devicesAdapter = new ArrayAdapter<>(this, R.layout.list_item_black);
        this.setListAdapter(this.devicesAdapter);

        final BluetoothDevicesActivity self = this;
        this.devices.setOnDiscoveredListener(new BluetoothDevices.OnDiscoveredListener()
        {
            @Override
            public void onDiscovered(BluetoothDeviceInfo device_info)
            {
                try {
                    self.devicesAdapter.add(device_info.device.getName());
                } catch (SecurityException e) {
                    Toast.ShowMessage(self, self.getString(R.string.Bluetooth_BluetoothPermissionError));
                }
                self.devicesAdapter.notifyDataSetInvalidated();
            }
        });
        this.devices.discover(this);
        Toast.ShowMessage(this, this.getResources().getString(
                R.string.Bluetooth_GettingDevicesList));
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

}