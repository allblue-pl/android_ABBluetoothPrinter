package pl.allblue.abbluetoothprinter;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import pl.allblue.abbluetoothprinter.databinding.ActivityBluetoothDevicesBinding;

public class BluetoothDevicesActivity extends AppCompatActivity {
    static public final String Extras_SupportedDeviceClasses =
            "pl.allblue.bluetooth.BluetoothDevicesList.Extras_SupportedDeviceClasses";
    static public final String Result_Device =
            "pl.allblue.bluetooth.BluetoothDevicesList.Result_Device";

    static public void Start(Activity activity, int requestCode,
            int[] deviceClasses) {
        Intent intent = new Intent(activity, BluetoothDevicesActivity.class);
        intent.putExtra(BluetoothDevicesActivity.Extras_SupportedDeviceClasses,
                deviceClasses);

        activity.startActivityForResult(intent, requestCode);
    }

    private ActivityBluetoothDevicesBinding b;
    private BluetoothDevices devices;
    private BluetoothDevicesAdapter listAdapter;

    private void discoverDevices() {
        devices.setOnDeviceDiscoveredListener(deviceInfo -> {
            listAdapter.addItem(deviceInfo);
        });

        if (!devices.init(this)) {
            Toast.showMessage(this, getString(
                    R.string.bluetooth_errors_cannot_start_discovering_devices));
            return;
        }

        devices.startDiscovery(this);
        Toast.showMessage(this, this.getResources().getString(
                R.string.bluetooth_getting_devices_list));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        b = ActivityBluetoothDevicesBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        setSupportActionBar(b.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        setTitle(getString(R.string.bluetooth_devices_title));

        b.list.setLayoutManager(new LinearLayoutManager(this));

        devices = null;

        listAdapter = new BluetoothDevicesAdapter();
        listAdapter.setListeners_OnDeviceSelected(deviceInfo -> {
            if (devices != null) {
                devices.finishDiscovery(this);
                devices = null;
            }

            Intent intent = new Intent();
            intent.putExtra(BluetoothDevicesActivity.Result_Device, deviceInfo);
            setResult(Activity.RESULT_OK, intent);
            finish();
        });
        b.list.setAdapter(listAdapter);

        devices = new BluetoothDevices(this.getIntent().getExtras()
                .getIntArray(BluetoothDevicesActivity
                .Extras_SupportedDeviceClasses));

        discoverDevices();
    }

    @Override
    protected void onDestroy() {
        if (b != null)
            b = null;
        if (devices != null) {
            devices.finishDiscovery(this);
            devices = null;
        }
        if (listAdapter != null)
            listAdapter = null;

        super.onDestroy();
    }

    @Override
    public boolean onSupportNavigateUp() {
        if (devices != null) {
            devices.finishDiscovery(this);
            devices = null;
        }
        finish();

        return true;
    }
}
