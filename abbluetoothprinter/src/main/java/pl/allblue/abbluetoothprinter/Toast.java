package pl.allblue.abbluetoothprinter;

import android.app.Activity;
import android.content.Context;

public class Toast
{

    static public void showMessage(final Activity activity, final CharSequence message) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Context context = activity.getApplicationContext();
                int duration = android.widget.Toast.LENGTH_LONG;

                android.widget.Toast toast = android.widget.Toast.makeText(
                        context, message, duration);
                toast.show();
            }
        });
    }

}
