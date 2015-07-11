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
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
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
public class FragmentRecentMealsListView extends ListFragment {

    private List<ListViewItem> mItems;        // ListView items list
    ListViewAdapter adapter;
    private int id_click = 0;
    JSONParser jsonParser = new JSONParser();
    JSONObject json;
    // Progress Dialog
    private ProgressDialog pDialog;
    private ProgressDialog sDialog;


    private static final String READ_RECIPES_URL = "http://expox-milano.com/foodapp/user_likes.php";
    private boolean downloaded_list = false;
    // JSON IDS:
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_MESSAGE = "message";
    private static final String TAG_POSTS = "posts";
    private static final String TAG_LIKES= "likes";
    private static final String TAG_FOLLOWED = "followed";
    private static final String TAG_RECIPE_ID = "recipe_id";
    private static final String TAG_L_RECIPE_ID = "l_recipe_id";
    private static final String TAG_F_USER_ID = "f_user_id";
    private static final String TAG_RECIPE_NAME = "recipe_name";
    private static final String TAG_INSTRUCTIONS = "instructions";
    private static final String TAG_INGREDIENTS = "ingredients";
    private static final String TAG_RECIPE_IMAGE_URL = "recipe_image_url";
    private static final String TAG_CREATOR_ID = "creator_id";
    private static final String TAG_CREATOR_USERNAME = "creator_username";
    private static final String TAG_CREATOR_PHOTO = "creator_photo";

    // An array of all of our pois
    private JSONArray mPois = null;
    private JSONArray mLikes = null;
    private JSONArray mFollowed = null;
    // manages all of our pois in a list.
    private ArrayList<HashMap<String, String>> mPoiList;
    private ArrayList<HashMap<String, String>> mLikeList;
    private ArrayList<HashMap<String, String>> mFollowedList;
    // Checks whether there is internet

    Drawable draw_temp_2;


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
            pDialog.setMessage("Loading all recipes...");
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

        if(!downloaded_list) {
            mPoiList = null;
            mPoiList = new ArrayList<HashMap<String, String>>();
            mLikeList = null;
            mLikeList = new ArrayList<HashMap<String, String>>();
            mFollowedList= null;
            mFollowedList= new ArrayList<HashMap<String, String>>();

            JSONParser jParser = new JSONParser();
            SharedPreferences sp = PreferenceManager
                    .getDefaultSharedPreferences(getActivity());
           String username = sp.getString("username","");
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("username", username));

    // Posting user data to script
    json = jsonParser.makeHttpRequest(READ_RECIPES_URL, "POST",
            params);
//            json = jParser.getJSONFromUrl(READ_RECIPES_URL);

        // when parsing JSON stuff, we should probably
        // try to catch any exceptions:
            try {
                //.-=Parse the recipes=-.


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
                    String creator_id = c.getString(TAG_CREATOR_ID);
                    String creator_username = c.getString(TAG_CREATOR_USERNAME);
                    String creator_photo = c.getString(TAG_CREATOR_PHOTO);

                    // creating new HashMap
                    HashMap<String, String> map = new HashMap<String, String>();

                    // map.put(TAG_POI_ID, poi_id);
                    map.put(TAG_RECIPE_ID, recipe_id);
                    map.put(TAG_RECIPE_NAME, name);
                    map.put(TAG_INSTRUCTIONS, instructions);
                    map.put(TAG_INGREDIENTS, ingredients);
                    map.put(TAG_RECIPE_IMAGE_URL, recipe_image_url);
                    map.put(TAG_CREATOR_ID, creator_id);
                    map.put(TAG_CREATOR_USERNAME, creator_username);
                    map.put(TAG_CREATOR_PHOTO, creator_photo);

                    // adding HashList to ArrayList
                    mPoiList.add(map);

                    Drawable draw_temp = null;
                    try {
                        draw_temp = drawableFromUrl(recipe_image_url);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    // Drawable draw_temp_2 = null;
                    try {
                        draw_temp_2 = drawableFromUrl(creator_photo);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    // Drawable draw_temp_2=null;
                       /*    new DrawableFromUrl2()
                            .execute(creator_photo);*/

                    mItems.add(new ListViewItem(draw_temp, name, instructions, creator_username, draw_temp_2));
                    // annndddd, our JSON data is up to date same with our array
                    // list

                }
                downloaded_list = true;
            } catch (JSONException e) {
                e.printStackTrace();

            }
            try{
                //.-=Parse the likes=-.

                // mPois will tell us how many "posts" are
                // available
                mLikes = json.getJSONArray(TAG_LIKES);

                // looping through all posts according to the json object
                // returned
                for (int i = 0; i < mLikes.length(); i++) {
                    JSONObject c = mLikes.getJSONObject(i);

                    // gets the content of each tag
                    String l_recipe_id = c.getString(TAG_L_RECIPE_ID);

                    // creating new HashMap
                    HashMap<String, String> map = new HashMap<String, String>();

                    map.put(TAG_L_RECIPE_ID, l_recipe_id);

                    // adding HashList to ArrayList
                    mLikeList.add(map);
                }
            }
            catch (JSONException e) {
                e.printStackTrace();

            }
            try{
                //.-=Parse the followed users=-.

                // mPois will tell us how many "posts" are
                // available
                mFollowed = json.getJSONArray(TAG_FOLLOWED);

                // looping through all posts according to the json object
                // returned
                for (int i = 0; i < mFollowed.length(); i++) {
                    JSONObject f = mFollowed.getJSONObject(i);

                    // gets the content of each tag
                    String f_user_id = f.getString(TAG_F_USER_ID);

                    // creating new HashMap
                    HashMap<String, String> map = new HashMap<String, String>();

                    // map.put(TAG_POI_ID, poi_id);
                    map.put(TAG_F_USER_ID, f_user_id);

                    // adding HashList to ArrayList
                    mFollowedList.add(map);
                }
            }
            catch (JSONException e) {
                e.printStackTrace();

            }
        }
    }

    /**
     * Inserts the parsed data into the listview.
     */
    private void updateList() {

        setListAdapter(new ListViewAdapter(getActivity(), mItems));
        ListView lv = getListView();
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                Log.e("poi_name", "The position is '" + position
                        + "' the id is '" + id + "'");

                id_click = position;

                updateJSONdataForOne();

                Intent i = new Intent(getActivity(),
                        ActivitySingleRecipeFromAll.class);
                getActivity().finish();
                startActivity(i);

            }
        });
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
            String creator_id = c.getString(TAG_CREATOR_ID);
            String creator_username = c.getString(TAG_CREATOR_USERNAME);
            String creator_photo = c.getString(TAG_CREATOR_PHOTO);

            Boolean liked=false;
            Boolean followed=false;
            mLikes = json.getJSONArray(TAG_LIKES);
            for(int i=0; i<mLikes.length(); i++){
                JSONObject l = mLikes.getJSONObject(i);

                if(recipe_id.equals(l.getString(TAG_L_RECIPE_ID))){
                    liked=true;
                }
            }
            mFollowed=json.getJSONArray(TAG_FOLLOWED);
            for(int i=0; i<mFollowed.length(); i++){
                JSONObject l = mFollowed.getJSONObject(i);

                if(recipe_id.equals(l.getString(TAG_F_USER_ID))){
                    followed=true;
                }
            }

            SharedPreferences sp = PreferenceManager
                    .getDefaultSharedPreferences(getActivity());
            SharedPreferences.Editor edit = sp.edit();
            edit.putString("recipe_id", recipe_id);
            edit.putString("recipe_name", recipe_name);
            edit.putString("ingredients", ingredients);
            edit.putString("instructions", instructions);
            // edit.putString("visibility", visibility);
            edit.putString("recipe_image_url", recipe_image_url);
            edit.putString("creator_id", creator_id);
            edit.putString("creator_username", creator_username);
            edit.putString("creator_photo", creator_photo);
            edit.putBoolean("liked", liked);
            edit.putBoolean("followed",followed);
            edit.commit();

        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("Recipe name", "");

        }

    }
}