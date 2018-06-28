package com.waracle.androidtest;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import static android.content.ContentValues.TAG;

/**
 * Created by cmf on 27/06/2018.
 */

public class CakeService extends AsyncTask<String, Void, JSONArray> {

    private CakeResultHandler listener;
    private Exception exception;

    public CakeService(CakeResultHandler listener) {
        this.listener = listener;
    }

    @Override
    protected JSONArray doInBackground(String... params) {
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(params[0]);
            urlConnection = (HttpURLConnection) url.openConnection();
            InputStream in = new BufferedInputStream(urlConnection.getInputStream());

            // Can you think of a way to improve the performance of loading data
            // using HTTP headers???

            // Also, Do you trust any utils thrown your way????

            byte[] bytes = StreamUtils.readUnknownFully(in);

            // Read in charset of HTTP content.
            String charset = parseCharset(urlConnection.getRequestProperty("Content-Type"));

            // Convert byte array to appropriate encoded string.
            String jsonText = new String(bytes, charset);

            // Read string as JSON.
            return new JSONArray(jsonText);
        } catch (Exception e){
            this.exception = e;
            return null;
        }
        finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
    }

    protected void onPostExecute(JSONArray result) {
        // TODO: check this.exception
        if (this.exception!=null){
            Log.e(TAG, "onPostExecute: ", exception);
        }
        // TODO: do something with the feed
        listener.onResultRecieved(result);
    }

    /**
     * Returns the charset specified in the Content-Type of this header,
     * or the HTTP default (ISO-8859-1) if none can be found.
     */
    private static String parseCharset(String contentType) {
        if (contentType != null) {
            String[] params = contentType.split(",");
            for (int i = 1; i < params.length; i++) {
                String[] pair = params[i].trim().split("=");
                if (pair.length == 2) {
                    if (pair[0].equals("charset")) {
                        return pair[1];
                    }
                }
            }
        }
        return "UTF-8";
    }
}
