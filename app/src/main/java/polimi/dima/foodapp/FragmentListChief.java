package polimi.dima.foodapp;

import android.app.AlertDialog;
import android.app.ListFragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
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
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

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
public class FragmentListChief extends ListFragment {

    private List<ListViewItem> mItems;        // ListView items list
    ListViewSimpleAdapter adapter;
    private int id_click = 0;
    JSONParser jsonParser = new JSONParser();
    JSONObject json;
    // Progress Dialog
    private ProgressDialog pDialog;
    private ProgressDialog sDialog;


    private static final String READ_USERS_URL = "http://expox-milano.com/foodapp/user_follow.php";
    private static final String UNFOLLOW_URL = "http://expox-milano.com/foodapp/del_follow.php";
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
    private static final String TAG_CREATOR_ID = "user_id";
    private static final String TAG_CREATOR_USERNAME = "creator_username";
    private static final String TAG_CREATOR_PHOTO = "creator_photo";

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

        new loadChiefs().execute();
        // initialize and set the list adapter
        setListAdapter(new ListViewSimpleAdapter(getActivity(), mItems));

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
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 3;
        x = BitmapFactory.decodeStream(input,null,options);
        return new BitmapDrawable(x);
    }

    public class loadChiefs extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(getActivity());
            pDialog.setMessage("Loading their secrets...");
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
        String username = sp.getString("username", "");
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("username", username));

        Log.d("request!", "starting");
        // getting product details by making HTTP request

        json = jsonParser.makeHttpRequest(READ_USERS_URL,
                "POST", params);

        SharedPreferences.Editor edit = sp.edit();
        edit.putString("json_users",json.toString());
        edit.commit();
        // when parsing JSON stuff, we should probably
        // try to catch any exceptions:
        if (!downloaded_list) {
            try {

                // mPois will tell us how many "posts" are
                // available
                mPois = json.getJSONArray(TAG_POSTS);

                // looping through all posts according to the json object
                // returned
                for (int i = 0; i < mPois.length(); i++) {
                    JSONObject c = mPois.getJSONObject(i);

                    // gets the content of each tag
                    String creator_id = c.getString(TAG_CREATOR_ID);
                    String creator_username = c.getString(TAG_CREATOR_USERNAME);
                    String creator_photo = c.getString(TAG_CREATOR_PHOTO);

                    // creating new HashMap
                    HashMap<String, String> map = new HashMap<String, String>();

                    // map.put(TAG_POI_ID, poi_id);
                    map.put(TAG_CREATOR_ID, creator_id);
                    map.put(TAG_CREATOR_USERNAME, creator_username);
                    map.put(TAG_CREATOR_PHOTO, creator_photo);

                    // adding HashList to ArrayList
                    mPoiList.add(map);

                    Drawable draw_temp_2 = null;
                    try {
                        draw_temp_2 = drawableFromUrl(creator_photo);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    mItems.add(new ListViewItem(creator_id, creator_username, draw_temp_2));
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

        setListAdapter(new ListViewSimpleAdapter(getActivity(), mItems));
        // Optional: when the user clicks a list item we
        // could do something. However, we will choose
        // to do nothing...

        final ListView lv = getListView();

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                Log.e("poi_name", "The position is '" + position
                        + "' the id is '" + id + "'");

                id_click = position;
                updateJSONdataForTheChief();
                Intent i = new Intent(getActivity(),
                        ActivityFollowRecipes.class);
                getActivity().finish();
                startActivity(i);
            }
        });
    }



    public void updateJSONdataForTheChief() {

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
            String creator_id = c.getString(TAG_CREATOR_ID);
            String creator_username = c.getString(TAG_CREATOR_USERNAME);
            String creator_photo = c.getString(TAG_CREATOR_PHOTO);

            // // creating new HashMap
            HashMap<String, String> map = new HashMap<String, String>();
            map.put(TAG_CREATOR_ID, creator_id);
            map.put(TAG_CREATOR_USERNAME, creator_username);
            map.put(TAG_CREATOR_PHOTO, creator_photo);

            // adding HashList to ArrayList
            mPoiList.add(map);

            // annndddd, our JSON data is up to date same with our array
            // list
            // now we save the strings for the poi in the sharedresources
            // to pass it to another activity

            SharedPreferences sp = PreferenceManager
                    .getDefaultSharedPreferences(getActivity());
            SharedPreferences.Editor edit = sp.edit();
            edit.putString("creator_id", creator_id);
            edit.putString("creator_username", creator_username);
            edit.putString("creator_photo", creator_photo);
            edit.putBoolean("one_user_recipes", true);
            edit.commit();

        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("Recipe name", "");

        }

    }
//Unfollow is not used here, but in the ListViewSimpleAdapter,
// because it is a button on top of the view.
/*    public class Unfollow extends AsyncTask<Void, Void, Boolean> {

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
            updateJSONdataForTheChief();
            // Instantiate the arraylist to contain all the JSON data.
            // we are going to use a bunch of key-value pairs, referring
            // to the json element name, and the content, for example,
            // message it the tag, and "I'm awesome" as the content..

            mPoiList = new ArrayList<HashMap<String, String>>();

            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
            String username = sp.getString("username", "");
            String creator_id = sp.getString("creator_id", "");

            List<NameValuePair> params = new ArrayList<NameValuePair>();

            Log.d("request!", "starting");
            // getting product details by making HTTP request


            params.add(new BasicNameValuePair("username", username));
            params.add(new BasicNameValuePair("creator_id", creator_id));

            json = jsonParser.makeHttpRequest(UNFOLLOW_URL,
                    "POST", params);

            //json = jParser.getJSONFromUrl(READ_RECIPES_URL);

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
                    String success = c.getString(TAG_SUCCESS);

                    // creating new HashMap
                    HashMap<String, String> map = new HashMap<String, String>();

                    // map.put(TAG_POI_ID, poi_id);
                    map.put(TAG_SUCCESS, success);

                    // adding HashList to ArrayList
                    mPoiList.add(map);


                }
            } catch (JSONException e) {
                e.printStackTrace();

            }
            return null;

        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            // pDialog.dismiss();
            getActivity().recreate();
        }
    } */
}