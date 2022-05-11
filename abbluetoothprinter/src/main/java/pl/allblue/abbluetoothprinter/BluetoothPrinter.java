package pl.allblue.abbluetoothprinter;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.graphics.Bitmap;
import android.graphics.Color;

import java.io.BufferedOutputStream;
import java.io.IOException;

/**
 * Created by SfTd on 02/05/2016.
 */
public class BluetoothPrinter
{

    static public final int BluetoothDevicesClass = 1536;
    static public boolean UseTechnologicWorkaround = false;

    static private byte[] GetBytes_Combine(byte[][] byte_parts)
    {
        int length = 0;
        for (int i = 0; i < byte_parts.length; i++)
            length += byte_parts[i].length;

        byte[] combined_bytes = new byte[length];

        int current_index = 0;
        for (int i = 0; i < byte_parts.length; i++) {
            System.arraycopy(byte_parts[i], 0, combined_bytes, current_index, byte_parts[i].length);
            current_index += byte_parts[i].length;
        }

        return combined_bytes;
    }

    static private byte[] GetBytes_Empty(int width, int emptyWidth, int emptyHeight)
    {
        float f = ((float)width) / ((float)emptyWidth);
        int height = (int)(emptyHeight * f);

        byte[] image = new byte[8 + (width / 8) * height];

        /* Print Format */
        image[0] = 0x1d;
        image[1] = 0x76;
        image[2] = 0x30;
        image[3] = 0;
        image[4] = (byte)((width / 8) % 256);
        image[5] = (byte)((width / 8) / 256);
        image[6] = (byte)(height % 256);
        image[7] = (byte)(height / 256);

        /* Image Data */
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < (width / 8); j++) {
                byte color_byte = 0;
                image[8 + i * (width / 8) + j] = color_byte;
            }
        }

        return image;
    }

    static private byte[] GetBytes_Image(Bitmap bitmap, int width)
    {
        if (bitmap == null)
            return null;

        float f = ((float)width) / ((float)bitmap.getWidth());
        int height = (int)(bitmap.getHeight() * f);

        Bitmap scaled_bitmap = bitmap.createScaledBitmap(bitmap, width, height, false);
        int[] bitmap_pixels = new int[width * height];
        scaled_bitmap.getPixels(bitmap_pixels, 0, width, 0, 0, width, height);

        byte[] image = new byte[8 + (width / 8) * height];

        /* Print Format */
        image[0] = 0x1d;
        image[1] = 0x76;
        image[2] = 0x30;
        image[3] = 0;
        image[4] = (byte)((width / 8) % 256);
        image[5] = (byte)((width / 8) / 256);
        image[6] = (byte)(height % 256);
        image[7] = (byte)(height / 256);

        /* Image Data */
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < (width / 8); j++) {
                byte color_byte = 0;

                for (int k = 0; k < 8; k++) {
                    int bmp_x = i;
                    int bmp_y = j * 8 + k;

                    if (Color.red(bitmap_pixels[bmp_x * width + bmp_y]) < 128)
                        color_byte |= (byte)(1 << (7 - k));
                }

                image[8 + i * (width / 8) + j] = color_byte;
            }
        }

        if (BluetoothPrinter.UseTechnologicWorkaround) {
            /* Technologic printers bug workaround. */
            for (int i = 8; i < image.length - 3; i++) {
                if (image[i] == 16 && image[i + 1] == 5)
                    image[i + 1] = 4;

                if (image[i] == 16 && image[i + 1] == 4 && image[i + 2] == 1)
                    image[i + 2] = 0;
            }
        }

        return image;
    }

    static private byte[] GetBytes_Init()
    {
        byte[] image = new byte[2];

        image[0] = 0x1b;
        image[1] = 0x40;

        return image;
    }

    static private byte[] GetBytes_Line()
    {
        byte[] line = new byte[7];

        String space = " ";

        line[0] = 0x1b;
        line[1] = 0x21;
        line[2] = 0x00;
        line[3] = 0x00;
//        line[3] = space.getBytes()[0];
        line[4] = 0x0D;
        line[5] = 0x0D;
        line[6] = 0x0D;

        return line;
    }


    private BluetoothDevice device = null;
    private BluetoothSocket socket = null;

    public BluetoothPrinter(BluetoothDevice device)
    {
        this.device = device;
    }

    public void connect(Activity activity, final OnConnectedListener listener)
    {
        final BluetoothPrinter self = this;

        Thread connect_thread = new Thread(new Runnable() {
            @Override
            public void run() {
                if (self.socket != null) {
                    if (self.socket.isConnected()) {
                        listener.onConnected();
                        return;
                    }

                    try {
                        self.socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                try {
                    self.socket = self.device.createRfcommSocketToServiceRecord(
                            self.device.getUuids()[0].getUuid());
                } catch (SecurityException e) {
                    Toast.ShowMessage(activity, activity.getString(R.string.Bluetooth_BluetoothPermissionError));
                    return;
                } catch (final IOException e) {
                    listener.onError(e);
                    return;
                }

                try {
                    self.socket.connect();
                } catch (SecurityException e) {
                    Toast.ShowMessage(activity, activity.getString(R.string.Bluetooth_BluetoothPermissionError));
                    return;
                } catch (IOException e) {
                    listener.onError(e);
                    return;
                }

                listener.onConnected();
            }
        });
        connect_thread.start();
    }

    public void disconnect()
    {
        if (this.socket != null) {
            try {
                this.socket.close();
            } catch (IOException e) {
                // Do nothing.
            }
        }
    }

    public boolean isConnected()
    {
        if (this.socket == null)
            return false;

        return this.socket.isConnected();
    }

    public void printImage(Bitmap image, int width) throws IOException
    {
        byte[] init_bytes = BluetoothPrinter.GetBytes_Init();
        byte[] empty_bytes = BluetoothPrinter.GetBytes_Empty(width, 100, 30);
        byte[] image_bytes = BluetoothPrinter.GetBytes_Image(image, width);

        this.sendBytes(init_bytes);
        this.sendBytes(image_bytes);
        this.sendBytes(empty_bytes);
    }

    public void sendBytes(byte[] bytes) throws IOException
    {
//        if (!this.isConnected())
//            throw new AssertionError("Printer not connected.");

        BufferedOutputStream socket_os = new BufferedOutputStream(
                this.socket.getOutputStream());

        socket_os.flush();

        socket_os.write(bytes);
        socket_os.flush();
    }


    public interface OnConnectedListener
    {
        void onConnected();
        void onError(IOException e);
    }

}
