package pl.allblue.abbluetoothprinter;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Insets;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowInsets;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import pl.allblue.abbluetoothprinter.databinding.ActivityBluetoothDevicesBinding;

public class BluetoothDevicesActivity extends AppCompatActivity {
    static public final String Extras_SupportedDeviceClasses =
            "pl.allblue.bluetooth.BluetoothDevicesList.Extras_SupportedDeviceClasses";
    static public final String Extras_ThemeColor =
            "pl.allblue.bluetooth.BluetoothDevicesList.Extras_ThemeColor";
    static public final String Extras_ThemeColorDark =
            "pl.allblue.bluetooth.BluetoothDevicesList.Extras_ThemeColorDark";
    static public final String Result_Device =
            "pl.allblue.bluetooth.BluetoothDevicesList.Result_Device";


    static public void Start(Activity activity, int requestCode,
                             int[] deviceClasses, int themeColor, int themeColorDark) {
        Intent intent = new Intent(activity, BluetoothDevicesActivity.class);
        intent.putExtra(BluetoothDevicesActivity.Extras_SupportedDeviceClasses,
                deviceClasses);
        intent.putExtra(BluetoothDevicesActivity.Extras_ThemeColor, themeColor);
        intent.putExtra(BluetoothDevicesActivity.Extras_ThemeColorDark, themeColorDark);

        activity.startActivityForResult(intent, requestCode);
    }


    private ActivityBluetoothDevicesBinding b;
    private BluetoothDevices devices;
    private BluetoothDevicesAdapter listAdapter;
    private int colors_Theme;
    private int colors_ThemeDark;

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

        colors_Theme = getIntent().getExtras().getInt(Extras_ThemeColor);
        colors_ThemeDark = getIntent().getExtras().getInt(Extras_ThemeColorDark);

        b = ActivityBluetoothDevicesBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        initWindow();
//        b.window.setBackgroundColor(colors_ThemeDark);
//        b.content.setBackgroundColor(colors_Theme);

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

        devices = new BluetoothDevices(getIntent().getExtras()
                .getIntArray(BluetoothDevicesActivity
                .Extras_SupportedDeviceClasses));

        discoverDevices();
    }

    private void initWindow() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            getWindow().getDecorView().setOnApplyWindowInsetsListener(
                    new View.OnApplyWindowInsetsListener() {
                @NonNull
                @Override
                public WindowInsets onApplyWindowInsets(@NonNull View v,
                                                        @NonNull WindowInsets insets) {
                    Insets insets_StatusBar = insets.getInsets(WindowInsets.Type.statusBars());
                    Insets insets_NavigationBar = insets.getInsets(WindowInsets.Type.navigationBars());

                    v.setPadding(0, insets_StatusBar.top, 0, 0);
//                    v.setBackgroundColor(colors_ThemeDark);

                    b.window.setPadding(0, 0, 0, insets_NavigationBar.bottom);

                    return insets;
                }
            });
        }
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
