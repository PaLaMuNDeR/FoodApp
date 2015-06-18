package polimi.dima.foodapp;

import android.app.ListFragment;
import android.app.ProgressDialog;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Marti on 17/06/2015.
 */
public class ListViewDemoFragment extends ListFragment {

    private List<ListViewItem> mItems;        // ListView items list
    ListViewAdapter adapter;
    private int id_click = 0;

    // Progress Dialog
    private ProgressDialog pDialog;
    private ProgressDialog sDialog;

    public List<ListViewItem> getmItems() {
        return mItems;
    }
    private static final String READ_RECIPIES = "http://expox-milano.com/foodapp/recipies.php";
    // JSON IDS:
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_POSTS = "posts";
    private static final String TAG_NAME = "name";
    private static final String TAG_INSTRUCTIONS = "instructions";
    private static final String TAG_INGREDIENTS = "ingredients";
    private static final String TAG_IMAGE_URL = "image_url";

    // An array of all of our pois
    private JSONArray mPois = null;
    // manages all of our pois in a list.
    private ArrayList<HashMap<String, String>> mPoiList;
    // Checks whether there is internet


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // initialize the items list
        mItems = new ArrayList<ListViewItem>();
        Resources resources = getResources();

        mItems.add(new ListViewItem(resources.getDrawable(R.drawable.ic_launcher), getString(R.string.address), getString(R.string.address)));
        mItems.add(new ListViewItem(resources.getDrawable(R.drawable.ic_launcher), getString(R.string.address), getString(R.string.address)));

        mItems.add(new ListViewItem(resources.getDrawable(R.drawable.ic_launcher), getString(R.string.address), getString(R.string.address)));

        // initialize and set the list adapter
        setListAdapter(new ListViewAdapter(getActivity(), mItems));




    }




    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // remove the dividers from the ListView of the ListFragment
        getListView().setDivider(null);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        // retrieve theListView item
        ListViewItem item = mItems.get(position);

        // do something
        Toast.makeText(getActivity(), item.title, Toast.LENGTH_SHORT).show();
    }

    /**
     * Retrieves recent post data from the server.
     */
    public void updateJSONdata() {

        // Instantiate the arraylist to contain all the JSON data.
        // we are going to use a bunch of key-value pairs, referring
        // to the json element name, and the content, for example,
        // message it the tag, and "I'm awesome" as the content..

        mPoiList = new ArrayList<HashMap<String, String>>();

        // it's time to power up the J parser
        JSONParser jParser = new JSONParser();
        // Feed the beast our comments url, and it spits us
        // back a JSON object. Boo-yeah Jerome.
        JSONObject json = jParser.getJSONFromUrl(READ_RECIPIES);

        // when parsing JSON stuff, we should probably
        // try to catch any exceptions:
        try {

            // mPois will tell us how many "posts" are
            // available
            mPois = json.getJSONArray(TAG_POSTS);

            // looping through all posts according to the json object
            // returned
            for (int i = 0; i < mPois.length(); i++) {
                JSONObject c = mPois.getJSONObject(i);

                // gets the content of each tag
                String name = c.getString(TAG_NAME);
                String ingredients = c.getString(TAG_INGREDIENTS);
                String instructions = c.getString(TAG_INSTRUCTIONS);
                // String poi_id = c.getString(TAG_POI_ID);
                String image_url = c.getString(TAG_IMAGE_URL);

                // creating new HashMap
                HashMap<String, String> map = new HashMap<String, String>();

                // map.put(TAG_POI_ID, poi_id);
                map.put(TAG_NAME, name);
                map.put(TAG_INSTRUCTIONS, instructions);
                map.put(TAG_INGREDIENTS, ingredients);
                map.put(TAG_IMAGE_URL,image_url);

                // adding HashList to ArrayList
                mPoiList.add(map);
                Uri image_01_uri = Uri.parse(image_url);
                Drawable draw_temp=null;
                try {
                    draw_temp = drawableFromUrl(image_url);
                }
                catch(Exception e){
                    e.printStackTrace();
                }

//                Drawable draw_temp= new DownloadImageTask(image_url);
                
                
                mItems.add(new ListViewItem(draw_temp, name, instructions));
                // annndddd, our JSON data is up to date same with our array
                // list
            }

        } catch (JSONException e) {
            e.printStackTrace();

        }
        try{
           
        
        }
        catch (Exception e){
            e.printStackTrace();
        }
    
    }

    public static Drawable drawableFromUrl(String url) throws IOException {
        Bitmap x;

        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.connect();
        InputStream input = connection.getInputStream();

        x = BitmapFactory.decodeStream(input);
        return new BitmapDrawable(x);
    }


    public class LoadComments extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(getActivity());
            pDialog.setMessage("Loading all points of interest...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        @Override
        protected Boolean doInBackground(Void... arg0) {
            updateJSONdata();
            return null;

        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            pDialog.dismiss();
            updateList();
        }

    }
    /**
     * Inserts the parsed data into the listview.
     */
    private void updateList() {
        // For a ListActivity we need to set the List Adapter, and in order to
        // do
        // that, we need to create a ListAdapter. This SimpleAdapter,
        // will utilize our updated Hashmapped ArrayList,
        // use our single_post xml template for each item in our list,
        // and place the appropriate info from the list to the
        // correct GUI id. Order is important here.
        ListAdapter adapter = new SimpleAdapter(getActivity(), mPoiList,
                R.layout.single_post, new String[] { TAG_NAME,
                TAG_INGREDIENTS,
                // TAG_LONG_DESCRIPTION,
                TAG_INSTRUCTIONS}, new int[] { R.id.poi_name,
                R.id.description, R.id.address });

        // I shouldn't have to comment on this one:
        setListAdapter(adapter);

        setListAdapter(new ListViewAdapter(getActivity(), mItems));
        // Optional: when the user clicks a list item we
        // could do something. However, we will choose
        // to do nothing...
        ListView lv = getListView();
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                Log.e("poi_name", "The position is '" + position
                        + "' the id is '" + id + "'");

                id_click = position;

           //     new AttemptTakeOnePoi().execute();
            //    new LoadRecommendation().execute();

                // This method is triggered if an item is click within our
                // list. For our example we won't be using this, but
                // it is useful to know in real life applications.

            }
        });
    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        Uri imageUri;
        Drawable drawableImage;

        public DownloadImageTask(Uri imageUri) {
            this.imageUri = imageUri;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                ;
                InputStream in = new java.net.URL(urldisplay).openStream();
                drawableImage = Drawable.createFromStream(in, urldisplay);
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Boolean result) {
        //    return ;
        }
    }
}