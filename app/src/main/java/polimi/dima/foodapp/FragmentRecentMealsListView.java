package polimi.dima.foodapp;

import android.app.Activity;
import android.app.LauncherActivity;
import android.app.ListFragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
//import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListAdapter;
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
import java.util.Map;

/**
 * Created by Marti on 17/06/2015.
 */
public class FragmentRecentMealsListView extends ListFragment{

    private List<ListViewItem> mItems;        // ListView items list
    ListViewAdapter adapter;
    private int id_click = 0;
    JSONParser jsonParser = new JSONParser();
    JSONObject json;

    private static final String READ_RECIPES_URL = "http://expox-milano.com/foodapp/user_likes.php";
    private static final String READ_LIKED_RECIPES_URL = "http://expox-milano.com/foodapp/user_liked_recipes.php";
    private boolean downloaded_list = false;
    // JSON IDS:
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_MESSAGE = "message";
    private static final String TAG_POSTS = "posts";
    private static final String TAG_RECIPE_NAME = "recipe_name";
    private static final String TAG_INSTRUCTIONS = "instructions";
    private static final String TAG_INGREDIENTS = "ingredients";
    private static final String TAG_RECIPE_IMAGE_URL = "recipe_image_url";
    private static final String TAG_CREATOR_ID = "creator_id";
    private static final String TAG_CREATOR_USERNAME = "creator_username";
    private static final String TAG_CREATOR_PHOTO = "creator_photo";
    private static final String TAG_RECIPE_ID = "recipe_id";
    private static final String TAG_LIKES= "likes";
    private static final String TAG_FOLLOWED = "followed";
    private static final String TAG_L_RECIPE_ID = "l_recipe_id";
    private static final String TAG_F_USER_ID = "f_user_id";

    // An array of all of our recipes
    private JSONArray mPois = null;
    //page_shown serves for iterator for pages shown (*10 items per page)
    public Integer page_shown=1;
    public Integer items_per_page=10;
    public Integer items_visualized=0;
    public Integer mPoisLength=0;
    private JSONArray mLikes = null;
    private JSONArray mFollowed = null;
    // manages all of our pois in a list.
    private ArrayList<HashMap<String, String>> recipesToLoad;
    private ArrayList<HashMap<String, Drawable>> imagesOfRecipes;
    private ArrayList<HashMap<String, String>> mLikeList;
    private ArrayList<HashMap<String, String>> mFollowedList;
    // Checks whether there is internet

    public Boolean flag_loading=false;
    public Boolean flag_load_more=true;
    public Boolean flag_need_refresh=true;
    ListViewAdapter list_adapt=null;












    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // initialize the items list
        mItems = new ArrayList<ListViewItem>();
        Resources resources = getResources();
        new LoadRecipes().execute();


        // initialize and set the list adapter
        //setListAdapter(new ListViewAdapter(getActivity(), mItems));

    }


    @Override
    public void onDestroyView(){
super.onDestroyView();
        for(int i=0;i<mItems.size();i++) {
            mItems.remove(i);
        } SharedPreferences sp = PreferenceManager
                .getDefaultSharedPreferences(getActivity());

        SharedPreferences.Editor edit = sp.edit();
        edit.putBoolean("liked_activity_bool", false);
        edit.putBoolean("flag_load_more", true);
        edit.commit();
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


    public class LoadRecipes extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            flag_loading=true;
        }

        @Override
        protected Boolean doInBackground(Void... arg0) {
            updateJSONdata();
            return null;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            flag_loading=false;
            updateList();

        }

    }
    public class LoadNewRecipes extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            flag_loading=true;
        }

        @Override
        protected Boolean doInBackground(Void... arg0) {
            if(flag_load_more) {
                updateJSONdata();
            }
            else{
                flag_need_refresh=false;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            if(flag_need_refresh) {
                list_adapt.notifyDataSetChanged();
            }
            flag_loading=false;
        }

    }
    /**
     * Retrieves recent post data from the server.
     */
    public void updateJSONdata() {
        SharedPreferences sp = PreferenceManager
                .getDefaultSharedPreferences(getActivity());
        if(!downloaded_list) {
            String username = sp.getString("username", "");
            Boolean liked_activity_bool = sp.getBoolean("liked_activity_bool", false);

            List<NameValuePair> params = new ArrayList<NameValuePair>();
            String creator_id = sp.getString("creator_id", "0");
            params.add(new BasicNameValuePair("creator_id", creator_id));
            params.add(new BasicNameValuePair("username", username));
            if (liked_activity_bool) {
                json = jsonParser.makeHttpRequest(READ_LIKED_RECIPES_URL,
                        "POST", params);

            } else {
                json = jsonParser.makeHttpRequest(READ_RECIPES_URL,
                        "POST", params);
            }

        }

        downloaded_list = true;
        // when parsing JSON stuff, we should probably
        // try to catch any exceptions:

            try {
                //.-=Parse the recipes=-.
                // mPois will tell us how many "posts" are
                mPois = json.getJSONArray(TAG_POSTS);
                mPoisLength=mPois.length();

                recipesToLoad = new ArrayList<HashMap<String, String>>();
                imagesOfRecipes = new ArrayList<HashMap<String,Drawable>>();

                flag_need_refresh=sp.getBoolean("flag_need_refresh",true);
                flag_load_more=sp.getBoolean("flag_load_more",true);
                // looping through all posts according to the json object
                // returned
               // for (int i = 0; i < mPois.length(); i++) {
                Log.d("Length of items", "" + mPois.length());
                Log.d("page_shown",""+ page_shown);
                Log.d("items_per_page",""+ items_per_page);
                Integer last_page;
                if(mPoisLength%items_per_page==0) {
                    last_page = mPoisLength / items_per_page;
                }
                else{
                    last_page = mPoisLength/items_per_page+1;
                }
//                for (int i = 0; i+(page_shown-1)*10 < page_shown*10; i++) {
                for (int i = 0; (i+(page_shown-1)*items_per_page < page_shown*items_per_page || i+(last_page-1)*items_per_page<mPoisLength-1) && items_visualized<=mPoisLength-1; i++) {
                    JSONObject c = mPois.getJSONObject(i + (page_shown - 1) * items_per_page);

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

                    //TODO the splitting here
                    HashMap<String,String> map = new HashMap<String,String>();

//                    map.put(TAG_RECIPE_ID,recipe_id);
//                    map.put(TAG_RECIPE_NAME,name);
//                    map.put(TAG_INGREDIENTS,ingredients);
//                    map.put(TAG_INSTRUCTIONS,instructions);
//                    map.put(TAG_RECIPE_IMAGE_URL,recipe_image_url);
//                    map.put(TAG_CREATOR_ID,creator_id);
//                    map.put(TAG_CREATOR_USERNAME, creator_username);
//                    map.put(TAG_CREATOR_PHOTO,creator_photo);
//                    recipesToLoad.add(map);
//                    HashMap<String,Drawable> mapOfDrawables = new HashMap<String,Drawable>();
                    Drawable recipe_draw=null;
                    Drawable creator_draw=null;
                    try{
                        recipe_draw = drawableFromUrl(recipe_image_url);
                    }
                    catch (Exception e){
                        e.printStackTrace();
                    }
                    try{
                        creator_draw = drawableFromUrl(creator_photo);
                    }
                    catch (Exception e){
                        e.printStackTrace();
                    }
//                    mapOfDrawables.put(TAG_RECIPE_IMAGE_URL,recipe_draw);
//                    mapOfDrawables.put(TAG_RECIPE_IMAGE_URL,creator_draw);

//                    imagesOfRecipes.add(mapOfDrawables);

                    mItems.add(new ListViewItem(recipe_draw, name, instructions, creator_username, creator_draw));

                    items_visualized=items_visualized+1;
                }
                if(items_visualized<mPoisLength){
                    page_shown=page_shown+1;
                    flag_load_more=true;
                }
                else{
                    flag_load_more=false;
                }
                Log.d("page_shown","incremented to " + page_shown);
                SharedPreferences.Editor edit = sp.edit();
                edit.putInt("page_shown", page_shown);
                edit.putBoolean("flag_load_more", flag_load_more);
                edit.commit();
            } catch (JSONException e) {
                e.printStackTrace();

            }


    }

    /**
     * Inserts the parsed data into the listview.
     */
    public void updateList() {

        list_adapt = new ListViewAdapter(getActivity(), mItems);
        setListAdapter(list_adapt);
        final ListView lv = getListView();
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

                if(creator_id.equals(l.getString(TAG_F_USER_ID))){
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


    public static Drawable drawableFromUrl(String url) throws IOException {
        Bitmap x;

        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.connect();
        InputStream input = connection.getInputStream();

        x = BitmapFactory.decodeStream(input);
        return new BitmapDrawable(x);
    }


}