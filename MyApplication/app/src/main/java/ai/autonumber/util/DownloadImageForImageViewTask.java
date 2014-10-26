package ai.autonumber.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import ai.autonumber.cache.FileCache;
import ai.autonumber.gcm.ServerUtilities;

public class DownloadImageForImageViewTask extends AsyncTask<String, Void, Bitmap> {
    private ImageView bmImage;
    private FileCache fileCache;

    public DownloadImageForImageViewTask(ImageView bmImage, FileCache fileCache) {
        this.bmImage = bmImage;
        this.fileCache = fileCache;
    }

    protected Bitmap doInBackground(String... imageIds) {
        String imageId = imageIds[0];
        try {
            File file = fileCache.getFile(Integer.parseInt(imageId));
            if (file.exists()) {
                return decodeFile(file);
            } else {
                Bitmap bitmap;
                String url = ServerUtilities.SERVER_URL + "/getimage?id=" + imageId;
                URL imageUrl = new URL(url);
                HttpURLConnection conn = (HttpURLConnection) imageUrl.openConnection();
                conn.setConnectTimeout(30000);
                conn.setReadTimeout(30000);
                conn.setInstanceFollowRedirects(true);
                InputStream is = conn.getInputStream();
                // Constructs a new FileOutputStream that writes to file
                // if file not exist then it will create file
                OutputStream os = new FileOutputStream(file);
                // See Utils class CopyStream method
                // It will each pixel from input stream and
                // write pixels to output stream (file)
                StreamUtils.copyStream(is, os);
                os.close();
                conn.disconnect();
                //Now file created and going to resize file with defined height
                // Decodes image and scales it to reduce memory consumption
                bitmap = decodeFile(file);
                return bitmap;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private Bitmap decodeFile(File file) throws IOException {
        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inSampleSize = 1;
        FileInputStream stream2 = new FileInputStream(file);
        Bitmap bitmap = BitmapFactory.decodeStream(stream2, null, o2);
        stream2.close();
        return bitmap;
    }

    protected void onPostExecute(Bitmap result) {
        bmImage.setImageBitmap(result);
    }
}
