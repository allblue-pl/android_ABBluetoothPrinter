package pl.allblue.abbluetoothprinter;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.IOException;
import java.io.InputStream;

public class Images
{

    static public Bitmap GetBitmapFromAssets(Context context, String image_path)
            throws IOException {
        AssetManager asset_manager = context.getAssets();

        InputStream istr = asset_manager.open(image_path);
        return BitmapFactory.decodeStream(istr);
    }

}
