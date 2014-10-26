package ai.autonumber.util;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.widget.ImageView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ai.autonumber.R;
import ai.autonumber.cache.FileCache;
import ai.autonumber.cache.MemoryCache;
import ai.autonumber.gcm.ServerUtilities;


public class ImageLoader {

    // Initialize Memo ryCache
    private final MemoryCache memoryCache = new MemoryCache();

    private final FileCache fileCache;

    //Create Map (collection) to store image and image url in key value pair
    private Map<ImageView, Integer> imageViews = Collections.synchronizedMap(new WeakHashMap<ImageView, Integer>());
    private ExecutorService executorService;

    //handler to display images in UI thread            
    private Handler handler = new Handler();
    private Activity context;

    public ImageLoader(Activity activity) {
        this.context = activity;
        fileCache = new FileCache(activity.getApplicationContext());
        // Creates a thread pool that reuses a fixed number of 
        // threads operating off a shared unbounded queue.
        executorService = Executors.newFixedThreadPool(5);
    }

    public Bitmap getBitmapFromCache(Integer imageId) {
        File file = fileCache.getFile(imageId);
        return decodeFile(file, true);
    }


    // default image show in list (Before online image download)    

    public void displayImage(Integer imageId, ImageView imageView) {
        //Store image and url in Map
        imageViews.put(imageView, imageId);

        //Check image is stored in MemoryCache Map or not (see MemoryCache.java)
        Bitmap bitmap = memoryCache.get(imageId);

        if (bitmap != null) {
            // if image is stored in MemoryCache Map then
            // Show image in listview row
            imageView.setImageBitmap(bitmap);
        } else {
            //queue Photo to download from url
            queuePhoto(imageId, imageView);
            //Before downloading image show default image 
            imageView.setImageResource(R.drawable.ic_stub);
        }
    }

    private void queuePhoto(Integer imageId, ImageView imageView) {
        // Store image and url in PhotoToLoad object
        PhotoToLoad p = new PhotoToLoad(imageId, imageView);

        // pass PhotoToLoad object to PhotosLoader runnable class
        // and submit PhotosLoader runnable to executers to run runnable
        // Submits a PhotosLoader runnable task for execution 
        executorService.submit(new PhotosLoader(p), context);
    }


    //Task for the queue
    private class PhotoToLoad {
        public Integer imageId;
        public ImageView imageView;

        public PhotoToLoad(Integer imageId, ImageView i) {
            this.imageId = imageId;
            imageView = i;
        }
    }

    class PhotosLoader implements Runnable {

        PhotoToLoad photoToLoad;

        PhotosLoader(PhotoToLoad photoToLoad) {
            this.photoToLoad = photoToLoad;
        }

        @Override
        public void run() {
            try {
                //Check if image already downloaded
                if (imageViewReused(photoToLoad))
                    return;
                // download image from web url
                Bitmap bmp = getBitmap(photoToLoad.imageId);
                // set image data in Memory Cache
                memoryCache.put(photoToLoad.imageId, bmp);
                if (imageViewReused(photoToLoad))
                    return;

                // Get bitmap to display
                BitmapDisplayer bd = new BitmapDisplayer(bmp, photoToLoad);

                // Causes the Runnable bd (BitmapDisplayer) to be added to the message queue. 
                // The runnable will be run on the thread to which this handler is attached.
                // BitmapDisplayer run method will call
                handler.post(bd);

            } catch (Throwable th) {
                th.printStackTrace();
            }
        }
    }


    private Bitmap getBitmap(Integer imageId) {
        File f = fileCache.getFile(imageId);
        //from SD cache
        //CHECK : if trying to decode file which not exist in cache return null
        Bitmap b = decodeFile(f, false);
        if (b != null)
            return b;

        // Download image file from web
        try {
            Bitmap bitmap = null;
            String url = ServerUtilities.SERVER_URL + "/getimage?id=" + imageId;
            URL imageUrl = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) imageUrl.openConnection();
            conn.setConnectTimeout(30000);
            conn.setReadTimeout(30000);
            conn.setInstanceFollowRedirects(true);
            InputStream is = conn.getInputStream();
            // Constructs a new FileOutputStream that writes to file
            // if file not exist then it will create file
            OutputStream os = new FileOutputStream(f);
            // See Utils class CopyStream method
            // It will each pixel from input stream and
            // write pixels to output stream (file)
            StreamUtils.copyStream(is, os);
            os.close();
            conn.disconnect();
            //Now file created and going to resize file with defined height
            // Decodes image and scales it to reduce memory consumption
            bitmap = decodeFile(f, false);
            return bitmap;
        } catch (Throwable ex) {
            ex.printStackTrace();
            if (ex instanceof OutOfMemoryError)
                memoryCache.clear();
            return null;
        }
    }
    //Decodes image and scales it to reduce memory consumption


    private Bitmap decodeFile(File f, boolean originalSize) {
        try {
            if (originalSize) {
                BitmapFactory.Options o2 = new BitmapFactory.Options();
                o2.inSampleSize = 1;
                FileInputStream stream2 = new FileInputStream(f);
                Bitmap bitmap = BitmapFactory.decodeStream(stream2, null, o2);
                stream2.close();
                return bitmap;
            } else {
                //Decode image size
                BitmapFactory.Options o = new BitmapFactory.Options();
                o.inJustDecodeBounds = true;
                FileInputStream stream1 = new FileInputStream(f);
                BitmapFactory.decodeStream(stream1, null, o);
                stream1.close();
                //Find the correct scale value. It should be the power of 2.
                // Set width/height of recreated image

                final int REQUIRED_SIZE = context.getWindow().getDecorView().getWidth() / 2 - 10;
                int width_tmp = o.outWidth, height_tmp = o.outHeight;
                int scale = 1;
                while (true) {
                    if (width_tmp / 2 < REQUIRED_SIZE || height_tmp / 2 < REQUIRED_SIZE)
                        break;
                    width_tmp /= 2;
                    height_tmp /= 2;
                    scale *= 2;
                }
                //decode with current scale values
                BitmapFactory.Options o2 = new BitmapFactory.Options();
                o2.inSampleSize = scale;
                FileInputStream stream2 = new FileInputStream(f);
                Bitmap bitmap = BitmapFactory.decodeStream(stream2, null, o2);
                stream2.close();
                return bitmap;
            }
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    boolean imageViewReused(PhotoToLoad photoToLoad) {
        Integer tag = imageViews.get(photoToLoad.imageView);
        //Check url is already exist in imageViews MAP
        return tag == null || !tag.equals(photoToLoad.imageId);
    }

    //Used to display bitmap in the UI thread
    class BitmapDisplayer implements Runnable {
        Bitmap bitmap;
        PhotoToLoad photoToLoad;

        public BitmapDisplayer(Bitmap b, PhotoToLoad p) {
            bitmap = b;
            photoToLoad = p;
        }

        public void run() {
            if (imageViewReused(photoToLoad))
                return;
            // Show bitmap on UI
            if (bitmap != null)
                photoToLoad.imageView.setImageBitmap(bitmap);
            else
                photoToLoad.imageView.setImageResource(R.drawable.ic_stub);
        }


    }

    public void clearCache() {
        //Clear cache directory downloaded images and stored data in maps
        memoryCache.clear();
        fileCache.clear();
    }
}