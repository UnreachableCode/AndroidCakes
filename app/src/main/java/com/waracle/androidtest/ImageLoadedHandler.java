package com.waracle.androidtest;

import android.widget.ImageView;

/**
 * Created by cmf on 28/06/2018.
 */

interface ImageLoadedHandler {
    void onImageRecieved(ImageLoader.BitmapDisplayer displayerRunnable);
}
