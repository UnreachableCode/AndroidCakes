package com.waracle.androidtest.Android.Cake.Services;

import com.waracle.androidtest.Android.Cake.Models.Cake;

import java.util.List;

/**
 * Created by cmf on 27/06/2018.
 */

public interface CakeResultHandler {
    void onResultRecieved(List<Cake> cakeList);
}
