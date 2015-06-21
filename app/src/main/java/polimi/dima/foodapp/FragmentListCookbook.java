package polimi.dima.foodapp;

import android.app.ListFragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
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
public class FragmentListCookbook extends ListFragment {

    private List<ListViewItem> mItems;        // ListView items list
    ListViewAdapter adapter;
    private int id_click = 0;
    JSONParser jsonParser = new JSONParser();
    JSONObject json;
    // Progress Dialog
    private ProgressDialog pDialog;
    private ProgressDialog sDialog;


    private static final String READ_RECIPES_URL = "http://expox-milano.com/foodapp/cookbook.php";
    private boolean downloaded_list = false;
    // JSON IDS:
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_MESSAGE = "message";
    private static final String TAG_POSTS = "posts";
    private static final String TAG_RECIPE_ID = "recipe_id";
    private static final String TAG_RECIPE_NAME = "recipe_name";
    private static final String TAG_INSTRUCTIONS = "instructions";
    private static final String TAG_INGREDIENTS = "ingredients";
    private static final String TAG_RECIPE_IMAGE_URL = "recipe_image_url";

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

        new LoadRecipes().execute();
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



    public static Drawable drawableFromUrl(String url) throws IOException {
        Bitmap x;

        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.connect();
        InputStream input = connection.getInputStream();

        x = BitmapFactory.decodeStream(input);
        return new BitmapDrawable(x);
    }

    public class LoadRecipes extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(getActivity());
            pDialog.setMessage("Loading all delicious things...");
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
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String username = sp.getString("username","");
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("username", username));

        Log.d("request!", "starting");
        // getting product details by making HTTP request

        json = jsonParser.makeHttpRequest(READ_RECIPES_URL,
                "POST", params);
        //json = jParser.getJSONFromUrl(READ_RECIPES_URL);

        // when parsing JSON stuff, we should probably
        // try to catch any exceptions:
        if(!downloaded_list) {
            try {

                // mPois will tell us how many "posts" are
                // available
                mPois = json.getJSONArray(TAG_POSTS);

                // looping through all posts according to the json object
                // returned
                for (int i = 0; i < mPois.length(); i++) {
                    JSONObject c = mPois.getJSONObject(i);

                    // gets the content of each tag
                    String recipe_id = c.getString(TAG_RECIPE_ID);
                    String name = c.getString(TAG_RECIPE_NAME);
                    String ingredients = c.getString(TAG_INGREDIENTS);
                    String instructions = c.getString(TAG_INSTRUCTIONS);
                    // String poi_id = c.getString(TAG_POI_ID);
                    String recipe_image_url = c.getString(TAG_RECIPE_IMAGE_URL);

                    // creating new HashMap
                    HashMap<String, String> map = new HashMap<String, String>();

                    // map.put(TAG_POI_ID, poi_id);
                    map.put(TAG_RECIPE_ID, recipe_id);
                    map.put(TAG_RECIPE_NAME, name);
                    map.put(TAG_INSTRUCTIONS, instructions);
                    map.put(TAG_INGREDIENTS, ingredients);
                    map.put(TAG_RECIPE_IMAGE_URL, recipe_image_url);

                    // adding HashList to ArrayList
                    mPoiList.add(map);

                    Drawable draw_temp = null;
                    try {
                        draw_temp = drawableFromUrl(recipe_image_url);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    mItems.add(new ListViewItem(draw_temp, name, instructions));
                    // annndddd, our JSON data is up to date same with our array
                    // list

                }
                downloaded_list = true;
            } catch (JSONException e) {
                e.printStackTrace();

            }
        }
    }


/*
    class AttemptTakeOnePoi extends AsyncTask<String, String, String> {

        *//**
         * Before starting background thread Show Progress Dialog
         * *//*

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            sDialog = new ProgressDialog(getActivity());
            sDialog.setMessage("Taking all the info for the Recipe...");
            sDialog.setIndeterminate(false);
            sDialog.setCancelable(true);
            sDialog.show();
        }

        @Override
        protected String doInBackground(String... args) {
            // Check for success tag
            int success;
            SharedPreferences sp = PreferenceManager
                    .getDefaultSharedPreferences(getActivity());
            String username = sp.getString("username", "");
            try {
                // Building Parameters
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("username", username));

                Log.d("request!", "starting");
                // getting product details by making HTTP request

                JSONObject json = jsonParser.makeHttpRequest(READ_RECIPES_URL,
                        "POST", params);

                // check your log for json response
                Log.d("Login attempt", json.toString());

                // json success tag
                success = json.getInt(TAG_SUCCESS);
                if (success == 1) {
                    Log.d("Synch Successful!", json.toString());

                    return json.getString(TAG_MESSAGE);
                } else {
                    Log.d("Synchronization failed!",
                            json.getString(TAG_MESSAGE));
                    return json.getString(TAG_MESSAGE);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;

        }

        protected void onPostExecute(String file_url) {
            // dismiss the dialog once product deleted
            // sDialog.dismiss();
            sDialog.dismiss();

            if (file_url != null) {
                Toast.makeText(getActivity(), file_url,
                        Toast.LENGTH_SHORT).show();
            }

        }

    }
    */
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
/*
        ListAdapter adapter = new SimpleAdapter(getActivity(), mPoiList,
                R.layout.single_post, new String[] {TAG_RECIPE_NAME,
                TAG_INGREDIENTS,
                // TAG_LONG_DESCRIPTION,
                TAG_INSTRUCTIONS}, new int[] { R.id.poi_name,
                R.id.description, R.id.address });

        // I shouldn't have to comment on this one:
        setListAdapter(adapter);
*/

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

              //  new AttemptTakeOnePoi().execute();
                new LoadRecipe().execute();

                // This method is triggered if an item is click within our
                // list. For our example we won't be using this, but
                // it is useful to know in real life applications.

            }
        });
    }

    public class LoadRecipe extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // pDialog = new ProgressDialog(ActivityRecommendations.this);
            // pDialog.setMessage("Loading your place..");
            // pDialog.setIndeterminate(false);
            // pDialog.setCancelable(true);
            // pDialog.show();
        }

        @Override
        protected Boolean doInBackground(Void... arg0) {
            updateJSONdataForOne();
            return null;

        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            // pDialog.dismiss();
            updateList();

            Log.d("Starting new activity", "SingleRecipeActivity");


//            ListViewDemoFragment thisFragment = (ListViewDemoFragment) getActivity().getFragmentManager().findFragmentById(R.id.fragment1);




/*
            final Fragment srf = new Fragment();
            ft.add(R.id.fragmentSingleRecipe,srf);
            ft.commit();
*/

//            getActivity().getFragmentManager().findFragmentById(R.id.fragmentSingleRecipe).getView().setVisibility(View.VISIBLE);
         /*   FragmentTransaction fragmentTransaction = getActivity().getFragmentManager().beginTransaction();
            final Fragment fragmentC = new Fragment();
            fragmentTransaction.add(R.id.fragmentSingleRecipe, fragmentC);
            fragmentTransaction.commit();
*/




            Intent i = new Intent(getActivity(),
                    ActivitySingleRecipeFromCookbook.class);
            //finish();
            startActivity(i);

        }
    }

    public void updateJSONdataForOne() {

        // Instantiate the arraylist to contain all the JSON data.
        // we are going to use a bunch of key-value pairs, referring
        // to the json element name, and the content, for example,
        // message it the tag, and "I'm awesome" as the content..

        mPoiList = new ArrayList<HashMap<String, String>>();

        // it's time to power up the J parser
        JSONParser jParser = new JSONParser();
        // Feed the beast our comments url, and it spits us
        // back a JSON object. Boo-yeah Jerome.
       //
       //
       // JSONObject json = jParser.getJSONFromUrl(READ_RECIPES_URL);

        // when parsing JSON stuff, we should probably
        // try to catch any exceptions:
        try {

            // mPois will tell us how many "posts" are
            // available
            mPois = json.getJSONArray(TAG_POSTS);

            // looping through all posts according to the json object
            // returned

            JSONObject c = mPois.getJSONObject(id_click);

            // gets the content of each tag
            String recipe_id = c.getString(TAG_RECIPE_ID);
            String recipe_name = c.getString(TAG_RECIPE_NAME);
            String ingredients = c.getString(TAG_INGREDIENTS);
            String instructions = c.getString(TAG_INSTRUCTIONS);
            String recipe_image_url = c.getString(TAG_RECIPE_IMAGE_URL);

            // // creating new HashMap
            HashMap<String, String> map = new HashMap<String, String>();

            map.put(TAG_RECIPE_ID, recipe_id);
            map.put(TAG_RECIPE_NAME, recipe_name);
            map.put(TAG_INGREDIENTS, ingredients);
            map.put(TAG_INSTRUCTIONS, instructions);
            map.put(TAG_RECIPE_IMAGE_URL, recipe_image_url);
            // adding HashList to ArrayList
            mPoiList.add(map);

            // annndddd, our JSON data is up to date same with our array
            // list
            // now we save the strings for the poi in the sharedresources
            // to pass it to another activity

            SharedPreferences sp = PreferenceManager
                    .getDefaultSharedPreferences(getActivity());
            SharedPreferences.Editor edit = sp.edit();
            edit.putString("recipe_id", recipe_id);
            edit.putString("recipe_name", recipe_name);
            edit.putString("ingredients", ingredients);
            edit.putString("instructions", instructions);
            // edit.putString("visibility", visibility);
            edit.putString("recipe_image_url", recipe_image_url);
            edit.commit();

        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("Recipe name", "");

        }

    }
}