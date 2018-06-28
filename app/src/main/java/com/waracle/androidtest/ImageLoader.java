package com.waracle.androidtest;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageView;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.InvalidParameterException;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Riad on 20/05/2015.
 */
public class ImageLoader {

    private static final String TAG = ImageLoader.class.getSimpleName();

    private MemoryCache memoryCache;
    private ExecutorService executorService;
    private Map<ImageView, String> imageViews= Collections.synchronizedMap(new WeakHashMap<ImageView, String>());

    public ImageLoader() {
        memoryCache = new MemoryCache();
        executorService = Executors.newFixedThreadPool(5);
    }

    /**
     * Simple function for loading a bitmap image from the web
     *
     * @param url       image url
     * @param imageView view to set image too.
     */
    public void load(String url, ImageView imageView) {
        if (TextUtils.isEmpty(url)) {
            throw new InvalidParameterException("URL is empty!");
        }

        // Can you think of a way to improve loading of bitmaps
        // that have already been loaded previously??

        try {
            setImageView(imageView, convertToBitmap(loadImageData(url)));
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    public void DisplayImage(String url, ImageView imageView)
    {
        imageViews.put(imageView, url);
        Bitmap bitmap = memoryCache.get(url);
        if(bitmap!=null)
            imageView.setImageBitmap(bitmap);
        else
        {
            queuePhoto(url, imageView);
        }
    }

    private void queuePhoto(String url, ImageView imageView) {
        CakeImageToLoad p = new CakeImageToLoad(url, imageView);
        executorService.submit(new PhotosLoader(p));
    }

    private static byte[] loadImageData(String url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        InputStream inputStream = null;
        try {
            try {
                // Read data from workstation
                inputStream = connection.getInputStream();
            } catch (IOException e) {
                // Read the error from the workstation
                inputStream = connection.getErrorStream();
            }

            // Can you think of a way to make the entire
            // HTTP more efficient using HTTP headers??

            return StreamUtils.readUnknownFully(inputStream);
        } finally {
            // Close the input stream if it exists.
            StreamUtils.close(inputStream);

            // Disconnect the connection
            connection.disconnect();
        }
    }

    class PhotosLoader implements Runnable {
        CakeImageToLoad imageToLoad;
        PhotosLoader(CakeImageToLoad imageToLoad){
            this.imageToLoad=imageToLoad;
        }

        @Override
        public void run() {
            try{
                if(imageViewReused(imageToLoad))
                    return;
                Bitmap bitmap = convertToBitmap(loadImageData(imageToLoad.url));
                memoryCache.put(imageToLoad.url, bitmap);
                if(imageViewReused(imageToLoad))
                    return;
                //todo: BitmapDisplayer bd=new BitmapDisplayer(bitmap, imageToLoad);
                setImageView(imageToLoad.imageView, bitmap);
                //listener.ImageLoaded(bd);
            }catch(Throwable th){
                th.printStackTrace();
            }
        }
    }

    boolean imageViewReused(CakeImageToLoad photoToLoad){
        String tag=imageViews.get(photoToLoad.imageView);
        if(tag==null || !tag.equals(photoToLoad.url))
            return true;
        return false;
    }

    private static Bitmap convertToBitmap(byte[] data) {
        return BitmapFactory.decodeByteArray(data, 0, data.length);
    }

    private static void setImageView(ImageView imageView, Bitmap bitmap) {
        imageView.setImageBitmap(bitmap);
    }
}
