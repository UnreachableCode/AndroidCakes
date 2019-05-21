package com.waracle.androidtest.Android.Cake.Models;

import android.widget.ImageView;

/**
 * Created by cmf on 28/06/2018.
 */

public class CakeImageToLoad {
    public String url;
    public ImageView imageView;

    public CakeImageToLoad(String u, ImageView i) {
        url = u;
        imageView = i;
    }
}
