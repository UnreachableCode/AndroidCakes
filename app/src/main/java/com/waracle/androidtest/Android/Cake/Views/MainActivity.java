package com.waracle.androidtest.Android.Cake.Views;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.waracle.androidtest.Android.Cake.Services.CakeService;
import com.waracle.androidtest.Android.Cake.Services.ImageLoader;
import com.waracle.androidtest.Android.Cake.Models.Cake;
import com.waracle.androidtest.Android.Cake.Models.CakeResultHandler;
import com.waracle.androidtest.Android.Cake.Services.ImageLoadedHandler;
import com.waracle.androidtest.R;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    private static String JSON_URL = "https://gist.githubusercontent.com/hart88/198f29ec5114a3ec3460/" +
            "raw/8dd19a88f9b8d24c23d9960f3300d0c917a4f07c/cake.json";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_refresh) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    /**
     * Fragment is responsible for loading in some JSON and
     * then displaying a list of cakes with images.
     * Fix any crashes
     * Improve any performance issues
     * Use good coding practices to make code more secure
     */
    public static class PlaceholderFragment extends ListFragment implements CakeResultHandler, ImageLoadedHandler {

        private static final String TAG = PlaceholderFragment.class.getSimpleName();

        private ListView mListView;
        private MyAdapter mAdapter;

        public PlaceholderFragment() { /**/ }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            mListView = (ListView)rootView.findViewById(android.R.id.list);
            return rootView;
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);

            // Create and set the list adapter.
            mAdapter = new MyAdapter(this);
//            mListView.setAdapter(mAdapter);

            // Load data from net.
            //try {
                new CakeService(this).execute(JSON_URL);
            //} catch (IOException | JSONException e) {
            //    Log.e(TAG, e.getMessage());
            //}
        }

        @Override
        public void onResultRecieved(List<Cake> cakeList) {
            mAdapter.setItems(cakeList);
            mListView.setAdapter(mAdapter);
        }

        @Override
        public void onImageRecieved(ImageLoader.BitmapDisplayer displayerRunnable) {
            //TODO either need a handler or need to remove the set call in getView
            getActivity().runOnUiThread(displayerRunnable);
        }


        private class MyAdapter extends BaseAdapter {

            private List<Cake>  mItems;
            private ImageLoader mImageLoader;

            public MyAdapter(ImageLoadedHandler handler) {
                this(new ArrayList<Cake>(), handler);
            }

            public MyAdapter(List<Cake> items, ImageLoadedHandler handler) {
                mItems = items;
                mImageLoader = new ImageLoader(handler);
            }

            @Override
            public int getCount() {
                return mItems.size();
            }

            @Override
            public Object getItem(int position) {
                return mItems.get(position);
            }

            @Override
            public long getItemId(int position) {
                return 0;
            }

            @SuppressLint("ViewHolder")
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                LayoutInflater inflater = LayoutInflater.from(getActivity());
                View root = inflater.inflate(R.layout.list_item_layout, parent, false);
                if (root != null) {
                    TextView title = (TextView) root.findViewById(R.id.title);
                    TextView desc = (TextView) root.findViewById(R.id.desc);
                    ImageView image = (ImageView) root.findViewById(R.id.image);
                    Cake cakeObject = (Cake)getItem(position);
                    title.setText(cakeObject.Title);
                    desc.setText(cakeObject.Description);
                    mImageLoader.DisplayImage(cakeObject.ImageUrl, image);
                }

                return root;
            }

            public void setItems(List<Cake> items) {
                mItems = items;
            }
        }
    }
}
