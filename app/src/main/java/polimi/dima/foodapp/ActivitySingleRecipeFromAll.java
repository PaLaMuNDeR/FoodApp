package polimi.dima.foodapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Marti on 19/06/2015.
 */

public class ActivitySingleRecipeFromAll extends ActionBarActivity implements View.OnClickListener {
    private String current_name = "current_user_name";
    private JSONArray mPois = null;

    // Progress Dialog
    private ProgressDialog sDialog;

    // Drawer Layout
    RecyclerView mRecyclerView;                           // Declaring RecyclerView
    RecyclerView.Adapter mAdapter;                        // Declaring Adapter For Recycler View
    RecyclerView.LayoutManager mLayoutManager;            // Declaring Layout Manager as a linear layout manager
    DrawerLayout Drawer;                                  // Declaring DrawerLayout
    Bitmap profileBitmap;
    ActionBarDrawerToggle mDrawerToggle;                  // Declaring Action Bar Drawer Toggle
    private String current_user = "current_user_username";
    private String name = "name";
    private String username = "username";
    private String email = "email";
    private String photo = "photo";
    private String cover = "cover";
    private String gender = "gender";
    private Boolean logout = false;
    String TITLES[] = {"Recent Meals", "My Cook Book", "Followed", "Liked", "Forum", "Logout"};
    int ICONS[] = {R.drawable.cutlery,
            R.drawable.open_book, R.drawable.follow, R.drawable.heart_dish_s_32,
            R.drawable.group_button, R.drawable.logout};
    Button
            btnAdd;

    private static String recipe_id = "";
    String creator_id;

    String user_id;
    JSONParser jsonParser = new JSONParser();

    JSONObject json;
    JSONObject jsonFollow;
    private static final String ADD_REC_URL = "http://expox-milano.com/foodapp/add_rec.php";
    private static final String UNFOLLOW_URL = "http://expox-milano.com/foodapp/del_follow.php";
    private static final String ADD_FOLLOW_URL = "http://expox-milano.com/foodapp/add_follow.php";
    private static final String ADD_LIKE_URL = "http://expox-milano.com/foodapp/add_like.php";
    private static final String LOAD_COMMENTS_URL = "http://expox-milano.com/foodapp/comments_for_recipe.php";
    private static final String POST_COMMENT_URL = "http://expox-milano.com/foodapp/add_comment_for_recipe.php";
    private static final String UNLIKE_URL = "http://expox-milano.com/foodapp/del_like.php";

    private static final String TAG_POSTS = "posts";
    private static final String TAG_RECIPE_ID = "recipe_id";
    private static final String TAG_TEXT = "text";
    private static final String TAG_COMMENTER = "username";
    private static final String TAG_COMMENTER_PHOTO = "photo";


    // JSON element ids from repsonse of php script:
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_MESSAGE = "message";

    private List<ListViewItem> mItems;        // ListView items list

    ImageView image_like;
    ImageView image_followed;
    ImageView post_comment;
    EditText comment_box;
    ListView lv;
    String comment_text;
    Boolean bool_liked = false;
    Boolean bool_followed = false;
    InputMethodManager inputManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_recipe);
        SharedPreferences sp = PreferenceManager
                .getDefaultSharedPreferences(ActivitySingleRecipeFromAll.this);
        String post_username = sp.getString("username", "");
        String post_name = sp.getString("recipe_name_view", "");
        Log.d("recipe_name_view", "Loading recipe_name_view " + post_name);
        current_name = post_name;
        Log.d("recipe_name_view", "current_name" + current_name);

        //Same part for every activity for making the navigation drawer
        current_user = sp.getString("username", "");
        name = sp.getString("recipe_name_view", "");
        email = sp.getString("email", "");
        photo = sp.getString("photo", "");
        cover = sp.getString("cover", "");
        cover = sp.getString("cover", "");
        user_id = sp.getString("user_id", "");
        bool_liked = sp.getBoolean("liked", false);
        bool_followed = sp.getBoolean("followed", false);
        SharedPreferences.Editor edit = sp.edit();
        edit.putBoolean("logout", false);
        edit.commit();
        Log.d("Username", "LogoutBool in SP: " + sp.getBoolean("logout", false));
        DatabaseHandler db = new DatabaseHandler(ActivitySingleRecipeFromAll.this);
        Profile pf = db.getLastProfile();
        name = pf.name;
        username = pf.username;
        photo = pf.photo;
        cover = pf.cover;
        gender = pf.gender;
        email = pf.email;
        mItems = new ArrayList<ListViewItem>();



        BitmapDrawable coverBitmap = null;
        //TODO do it all around

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 3;

        try {
            File imgFile = new File("/sdcard/FoodApp/profile/user_photo.jpg");

            if (imgFile.exists()) {
                Log.d("Download Image", "Profile Image - yes");

               // profileBitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeFile(imgFile.getAbsolutePath()), );

                profileBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath(),options);
            }
            imgFile = new File("/sdcard/FoodApp/profile/cover_photo.jpg");
            if (imgFile.exists()) {
                Log.d("Download Image", "Cover Image - yes");
                Bitmap bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath(),options);
             //   Bitmap bitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeFile(imgFile.getAbsolutePath()), 30,30, true);

                coverBitmap = new BitmapDrawable(getResources(), bitmap);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }



        // Assinging the toolbar object ot the view
        //and setting the the Action bar to our toolbar
        //
        //   toolbar = (Toolbar) findViewById(R.id.tool_bar);
        //   setSupportActionBar(toolbar);
        mRecyclerView = (RecyclerView) findViewById(R.id.RecyclerView); // Assigning the RecyclerView Object to the xml View

        mRecyclerView.setHasFixedSize(true);                            // Letting the system know that the list objects are of fixed size

        mAdapter = new MyAdapter(TITLES, ICONS, name, email, profileBitmap, coverBitmap, this);       // Creating the Adapter of MyAdapter class(which we are going to see in a bit)
        // And passing the titles,icons,header view recipe_name_view, header view email,
        // and header view profile picture

        mRecyclerView.setAdapter(mAdapter);                              // Setting the adapter to RecyclerView

        final GestureDetector mGestureDetector = new GestureDetector(ActivitySingleRecipeFromAll.this, new GestureDetector.SimpleOnGestureListener() {

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                return true;
            }

        });


        mRecyclerView.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(RecyclerView recyclerView, MotionEvent motionEvent) {
                View child = recyclerView.findChildViewUnder(motionEvent.getX(), motionEvent.getY());


                if (child != null && mGestureDetector.onTouchEvent(motionEvent)) {
                    Drawer.closeDrawers();
                    if (recyclerView.getChildPosition(child) == 1) {
                        Intent i = new Intent(ActivitySingleRecipeFromAll.this, ActivityRecentMeals.class);
                        startActivity(i);
                        finish();
                    }
                    if (recyclerView.getChildPosition(child) == 2) {
                        Intent i = new Intent(ActivitySingleRecipeFromAll.this, ActivityCookbook.class);
                        startActivity(i);
                        finish();
                    }
                    if (recyclerView.getChildPosition(child) == 3) {
                        Intent i = new Intent(ActivitySingleRecipeFromAll.this, ActivityFollowRecipes.class);
                        startActivity(i);
                        finish();
                    }
                    if (recyclerView.getChildPosition(child) == 4) {
                        Intent i = new Intent(ActivitySingleRecipeFromAll.this, ActivityLiked.class);
                        startActivity(i);
                        finish();
                    }
                    if (recyclerView.getChildPosition(child) == 5) {
                        Intent i = new Intent(ActivitySingleRecipeFromAll.this, ActivityForum.class);
                        startActivity(i);
                        finish();
                    }
                    if (recyclerView.getChildPosition(child) == 6) {
                        SharedPreferences sp = PreferenceManager
                                .getDefaultSharedPreferences(ActivitySingleRecipeFromAll.this);

                        current_user = "";
                        name = "";
                        logout = true;
                        SharedPreferences.Editor edit = sp.edit();
                        edit.putString("username", current_user);
                        edit.putString("recipe_name_view", name);
                        edit.putBoolean("logout", true);
                        edit.commit();
                        Log.d("Log out current_user:", current_user);
                        Log.d("Log out: ", name);
                        Intent i = new Intent(ActivitySingleRecipeFromAll.this, ActivityLogin.class);
                        startActivity(i);
                        finish();

                    }
                    return true;

                }

                return false;
            }

            @Override
            public void onTouchEvent(RecyclerView recyclerView, MotionEvent motionEvent) {

            }
        });


        mLayoutManager = new LinearLayoutManager(this);                 // Creating a layout Manager

        mRecyclerView.setLayoutManager(mLayoutManager);                 // Setting the layout Manager


        Drawer = (DrawerLayout) findViewById(R.id.DrawerLayout);        // Drawer object Assigned to the view
        mDrawerToggle = new ActionBarDrawerToggle(this, Drawer,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close) {


            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                // code here will execute once the drawer is opened( As I dont want anything happened whe drawer is
                // open I am not going to put anything here)
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                // Code here will execute once drawer is closed
            }

        };

        // Drawer Toggle Object Made
        //    Drawer.setDrawerListener(mDrawerToggle); // Drawer Listener set to the Drawer toggle
        //  mDrawerToggle = new ActionBarDrawerToggle(this, Drawer, 0, 0);
        Drawer.setDrawerListener(mDrawerToggle);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mDrawerToggle.syncState();               // Finally we set the drawer toggle sync State

        current_user = post_username;
        //Load the comments of the recipe
        new LoadComments().execute();


        btnAdd = (Button) findViewById(R.id.btn_add);

        btnAdd.setOnClickListener(this);
        // parsing the data from the shared preferences
        recipe_id = sp.getString("recipe_id", "");

        String r_name_value = sp.getString("recipe_name", "");
        TextView recipe_name_view2 = (TextView) findViewById(R.id.recipe_name);
        recipe_name_view2.setText(r_name_value);
        getSupportActionBar().setTitle(r_name_value);

        String ingredients_value = sp.getString("ingredients", "");
        TextView ingredients = (TextView) findViewById(R.id.ingredients);
        ingredients.setText(ingredients_value);

        String instructions_value = sp.getString("instructions", "");
        TextView instructions = (TextView) findViewById(R.id.instructions);
        instructions.setText(instructions_value);

        String recipe_image_url = sp.getString("recipe_image_url", "");
        creator_id = sp.getString("creator_id", "");
        String creator_username = sp.getString("creator_username", "");
        String creator_photo = sp.getString("creator_photo", "");

        TextView creator_name = (TextView) findViewById(R.id.creatorName);
        creator_name.setText(creator_username);
        creator_name.setOnClickListener(this);
        TextView follow_text = (TextView) findViewById(R.id.followTop);

        image_followed = (ImageView) findViewById(R.id.imageFollow);
        image_followed.setOnClickListener(this);


        new DownloadImageTask((ImageView) findViewById(R.id.imageViewSingleRecipe))
                .execute(recipe_image_url);
        new DownloadImageTask((ImageView) findViewById(R.id.creatorImage))
                .execute(creator_photo);
        if (user_id.equals(creator_id)) {
            image_followed.setVisibility(View.GONE);
            follow_text.setVisibility(View.GONE);

        }

        image_like = (ImageView) findViewById(R.id.image_like);
        image_like.setOnClickListener(this);
        if (bool_liked) {
            image_like.setImageDrawable(getResources().getDrawable(R.drawable.heart_dish_o_64));
        }
        if (bool_followed) {
            this.image_followed.setImageDrawable(getResources().getDrawable(R.drawable.follow_o_64));
        }
        ImageView user_photo= (ImageView) findViewById(R.id.user_photo);
        user_photo.setImageBitmap(profileBitmap);
        TextView current_username = (TextView) findViewById(R.id.username);
                current_username.setText(current_user);
        comment_box = (EditText) findViewById(R.id.comment_box);
        post_comment = (ImageView) findViewById(R.id.post_comment);
        post_comment.setOnClickListener(this);
        lv = (ListView) findViewById(R.id.comments_list);

    }

    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement

        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }


        return super.onOptionsItemSelected(item);
    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                ;
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.toString());
                //e.getMessage());
                //e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_add:
                new AddRecipe().execute();
                break;
            case R.id.imageView1:
                break;
            case R.id.imageFollow:
                new AddFollowUnfollow().execute();
                break;
            case R.id.followTop:
                new AddFollowUnfollow().execute();
                break;
            case R.id.image_like:
                new LikeUnlike().execute();
                break;
            case R.id.post_comment:
                InputMethodManager inputManager = (InputMethodManager)
                        getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                        InputMethodManager.HIDE_NOT_ALWAYS);
                new PostComment().execute();
                break;
            default:
                break;
        }
    }

    class AddRecipe extends AsyncTask<String, String, String> {


        boolean failure = false;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            sDialog = new ProgressDialog(ActivitySingleRecipeFromAll.this);
            sDialog.setMessage("Adding it to your cookbook...");
            sDialog.setIndeterminate(false);
            sDialog.setCancelable(true);
            sDialog.show();
        }

        @Override
        protected String doInBackground(String... args) {
            // Check for success tag
            int success;
            String username = current_user;
            try {
                // Building Parameters
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("username", username));
                params.add(new BasicNameValuePair("recipe_id", recipe_id));

                Log.d("request!", "starting");
                // getting product details by making HTTP request
                JSONObject json = jsonParser.makeHttpRequest(ADD_REC_URL,
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
            // dismiss the dialog once product is added
            sDialog.dismiss();
            Log.d("current_user", "current_user" + current_user);
            if (file_url != null && current_user != "") {
                Toast.makeText(ActivitySingleRecipeFromAll.this, R.string.opening_your_cookbook,
                        Toast.LENGTH_SHORT).show();
            }
            Log.d("Starting new activity", "ActivityCookbook");
            Intent i = new Intent(ActivitySingleRecipeFromAll.this,
                    ActivityCookbook.class);
            finish();
            startActivity(i);

        }

    }

    class AddFollowUnfollow extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... args) {
            // Check for success tag
            int success;
            String username = current_user;
            try {
                // Building Parameters
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("username", username));
                params.add(new BasicNameValuePair("creator_id", creator_id));

                Log.d("request!", "starting");
                // getting product details by making HTTP request
                if (bool_followed) {
                    json = jsonParser.makeHttpRequest(UNFOLLOW_URL,
                            "POST", params);
                } else {
                    json = jsonParser.makeHttpRequest(ADD_FOLLOW_URL,
                            "POST", params);
                }
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
            // dismiss the dialog once product is added
            Log.d("current_user", "current_user" + current_user);
            if (file_url != null && current_user != "") {
                if (bool_followed) {
                    Toast.makeText(ActivitySingleRecipeFromAll.this, "Unfollowed",
                            Toast.LENGTH_SHORT).show();
                    image_followed.setImageDrawable(getResources().getDrawable(R.drawable.follow_s_64));
                    bool_followed = false;
                } else {
                    Toast.makeText(ActivitySingleRecipeFromAll.this, "Following",
                            Toast.LENGTH_SHORT).show();
                    image_followed.setImageDrawable(getResources().getDrawable(R.drawable.follow_o_64));
                    bool_followed = true;
                }

            }
        }

    }

    class LikeUnlike extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... args) {
            // Check for success tag
            int success;
            String username = current_user;
            try {
                // Building Parameters
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("username", username));
                params.add(new BasicNameValuePair("recipe_id", recipe_id));

                Log.d("request!", "starting");
                // getting product details by making HTTP request
                if (bool_liked) {
                    json = jsonParser.makeHttpRequest(UNLIKE_URL,
                            "POST", params);

                } else {
                    json = jsonParser.makeHttpRequest(ADD_LIKE_URL,
                            "POST", params);
                }
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
            // dismiss the dialog once product is added
            Log.d("current_user", "current_user" + current_user);
            if (file_url != null && current_user != "") {
                if (bool_liked) {
                    Toast.makeText(ActivitySingleRecipeFromAll.this, "Unliked",
                            Toast.LENGTH_SHORT).show();
                    image_like.setImageDrawable(getResources().getDrawable(R.drawable.heart_dish_s_64));
                    bool_liked = false;
                } else {
                    Toast.makeText(ActivitySingleRecipeFromAll.this, "Liked",
                            Toast.LENGTH_SHORT).show();
                    image_like.setImageDrawable(getResources().getDrawable(R.drawable.heart_dish_o_64));
                    bool_liked = true;
                }
            }

        }

    }

    class PostComment extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... args) {
            // Check for success tag
            Log.d("Post comment", "start");
            int success;
            String username = current_user;
            try {
                comment_text = comment_box.getText().toString();
                // Building Parameters
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("username", username));
                params.add(new BasicNameValuePair("recipe_id", recipe_id));
                params.add(new BasicNameValuePair("text", comment_text));
                json = jsonParser.makeHttpRequest(POST_COMMENT_URL,
                        "POST", params);

                Log.d("request!", "starting");
                // getting product details by making HTTP request
              /*  if (bool_liked) {
                    json = jsonParser.makeHttpRequest(UNLIKE_URL,
                            "POST", params);

                } else {
                    json = jsonParser.makeHttpRequest(ADD_LIKE_URL,
                            "POST", params);
                }*/
                // check your log for json response
                Log.d("Comment attempt", json.toString());

                // json success tag
                success = json.getInt(TAG_SUCCESS);
                if (success == 1) {
                    Log.d("Sync Successful!", json.toString());

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
            // dismiss the dialog once product is added
            Log.d("current_user", "current_user" + current_user);
            if (file_url != null && current_user != "") {
                comment_box.setText("");
                comment_box.setInputType(InputType.TYPE_NULL);
/*
                comment_box.setFocusable(false);
                comment_box.setFocusableInTouchMode(true);*/
                mItems.clear();

                Toast.makeText(ActivitySingleRecipeFromAll.this, "Posted",
                        Toast.LENGTH_SHORT).show();
                new LoadComments().execute();

            }

        }

    }

    class LoadComments extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... args) {
            // Check for success tag
            int success;
            try{
                // Building Parameters
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("recipe_id", recipe_id));

                Log.d("request!", "starting");
                // getting product details by making HTTP request
                    json = jsonParser.makeHttpRequest(LOAD_COMMENTS_URL,
                            "POST", params);
                // check your log for json response
                Log.d("Comments attempt", json.toString());
            success = json.getInt(TAG_SUCCESS);
            if (success == 1) {
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
                        String text = c.getString(TAG_TEXT);
                        String commenter = c.getString(TAG_COMMENTER);
                        String commenter_photo = c.getString(TAG_COMMENTER_PHOTO);

                        Drawable draw_temp = null;
                        try {
                            draw_temp = drawableFromUrl(commenter_photo);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        mItems.add(new ListViewItem(recipe_id, commenter, draw_temp, text));
                        // annndddd, our JSON data is up to date same with our array
                        // list

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                }
            }
            catch(Exception e){
                e.printStackTrace();
            }
            return null;

        }

        protected void onPostExecute(String file_url) {
            // dismiss the dialog once product is added
            Log.d("current_user", "current_user" + current_user);
            lv.deferNotifyDataSetChanged();
            updateCommentsList();
        }

    }

    private void updateCommentsList() {
        lv.setAdapter(new ListViewCommentsAdapter(ActivitySingleRecipeFromAll.this, mItems));
        //setListAdapter(new ListViewCommentsAdapter(ActivitySingleRecipeFromAll.this, mItems));
        //getListView();
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                Log.e("poi_name", "The position is '" + position
                        + "' the id is '" + id + "'");

          //      id_click = position;

            //    updateJSONdataForOne();

/*
                Intent i = new Intent(getActivity(),
                        ActivitySingleRecipeFromAll.class);
                getActivity().finish();
                startActivity(i);
*/

            }
        });
    }
    public static Drawable drawableFromUrl(String url) throws IOException {
        Bitmap x;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 3;
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.connect();
        InputStream input = connection.getInputStream();

        x = BitmapFactory.decodeStream(input,null,options);
        return new BitmapDrawable(x);
    }

    @Override
    public void onBackPressed(){
        super.onBackPressed();
        Intent i = new Intent(ActivitySingleRecipeFromAll.this, ActivityRecentMeals.class);
        startActivity(i);
        finish();
    }

}