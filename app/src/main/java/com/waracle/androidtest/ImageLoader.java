package com.waracle.androidtest;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.widget.ImageView;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
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

    private ImageLoadedHandler listener;

    private MemoryCache memoryCache;
    private ExecutorService executorService;
    private Map<ImageView, String> imageViews= Collections.synchronizedMap(new WeakHashMap<ImageView, String>());

    public ImageLoader(ImageLoadedHandler listener) {
        memoryCache = new MemoryCache();
        executorService = Executors.newFixedThreadPool(5);
        this.listener = listener;
    }

    /**
     * Simple function for loading a bitmap image from the web
     *
     * @param url       image url
     * @param imageView view to set image too.
     */
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
                BitmapDisplayer displayerRunnable = new BitmapDisplayer(bitmap, imageToLoad);
                listener.onImageRecieved(displayerRunnable);
            } catch(Exception e){
                Log.e(TAG, e.getMessage());
            }
        }
    }

    boolean imageViewReused(CakeImageToLoad photoToLoad){
        String tag=imageViews.get(photoToLoad.imageView);
        if(tag==null || !tag.equals(photoToLoad.url))
            return true;
        return false;
    }

    //Display the bitmap in the UI thread.
    class BitmapDisplayer implements Runnable
    {
        Bitmap bitmap;
        CakeImageToLoad imageToLoad;
        public BitmapDisplayer(Bitmap b, CakeImageToLoad p){bitmap=b;imageToLoad=p;}
        public void run()
        {
            if(imageViewReused(imageToLoad))
                return;
            if(bitmap!=null)
                imageToLoad.imageView.setImageBitmap(bitmap);
        }
    }

    private static Bitmap convertToBitmap(byte[] data) {
        return BitmapFactory.decodeByteArray(data, 0, data.length);
    }
}
