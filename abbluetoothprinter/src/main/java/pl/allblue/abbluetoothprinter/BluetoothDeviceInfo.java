package pl.allblue.abbluetoothprinter;

import android.os.Parcel;
import android.os.Parcelable;

public class BluetoothDeviceInfo implements Parcelable
{

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<BluetoothDeviceInfo> CREATOR =
            new Parcelable.Creator<BluetoothDeviceInfo>() {
        @Override
        public BluetoothDeviceInfo createFromParcel(Parcel in) {
            return new BluetoothDeviceInfo(in);
        }

        @Override
        public BluetoothDeviceInfo[] newArray(int size) {
            return new BluetoothDeviceInfo[size];
        }
    };


    public android.bluetooth.BluetoothDevice device = null;
    public boolean isPaired = false;

    public BluetoothDeviceInfo(android.bluetooth.BluetoothDevice device, boolean is_paired)
    {
        this.device = device;
        this.isPaired = is_paired;
    }

    public BluetoothDeviceInfo(Parcel in)
    {
        this.device = (android.bluetooth.BluetoothDevice)in.readValue(
                android.bluetooth.BluetoothDevice.class.getClassLoader());
        this.isPaired = in.readByte() != 0x00;
    }


    /* Parcelable Overrides */
    @Override
    public int describeContents()
    {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeValue(this.device);
        dest.writeByte((byte)(isPaired ? 0x01 : 0x00));
    }
}
