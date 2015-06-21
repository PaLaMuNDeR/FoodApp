package polimi.dima.foodapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Marti on 19/06/2015.
 */

public class ActivityCreateRecipe extends ActionBarActivity implements View.OnClickListener{
    private String current_name = "current_user_name";

    // Progress Dialog
    private ProgressDialog sDialog;
    private EditText recipe_name, ingredients, instructions;//, age_value;
    private ImageView recipe_image;
    private Button btnCreate;

    // Progress Dialog
    private ProgressDialog pDialog;


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
    String TITLES[] = {"Recent Meals", "My Cook Book", "Friends", "Liked", "Forum", "Logout"};
    int ICONS[] = {R.drawable.cutlery,
            R.drawable.open_book, R.drawable.forum, R.drawable.heart_dish,
            R.drawable.group_button, R.drawable.logout};

    private static String recipe_id = "";

    JSONParser jsonParser = new JSONParser();
    private static final String CREATE_REC_URL = "http://expox-milano.com/foodapp/create_rec.php";

    // JSON element ids from repsonse of php script:
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_MESSAGE = "message";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_recipe);
        SharedPreferences sp = PreferenceManager
                .getDefaultSharedPreferences(ActivityCreateRecipe.this);
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
        SharedPreferences.Editor edit = sp.edit();
        edit.putBoolean("logout", false);
        edit.commit();
        Log.d("Username", "LogoutBool in SP: " + sp.getBoolean("logout", false));
        DatabaseHandler db = new DatabaseHandler(ActivityCreateRecipe.this);
        Profile pf =  db.getLastProfile();
        name = pf.name;
        username = pf.username;
        photo = pf.photo;
        cover = pf.cover;
        gender = pf.gender;
        email = pf.email;
        BitmapDrawable coverBitmap = null;
        try {
            File imgFile = new File("/sdcard/FoodApp/profile/user_photo.jpg");

            if(imgFile.exists()){
                profileBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
            }
            imgFile = new File("/sdcard/FoodApp/profile/cover_photo.jpg");
            if(imgFile.exists()){
                Bitmap bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                coverBitmap = new BitmapDrawable(getResources(), bitmap);
            }

        }
        catch (Exception e) {
            e.printStackTrace();
        }

        // Assinging the toolbar object ot the view
        //and setting the the Action bar to our toolbar
        //
        //   toolbar = (Toolbar) findViewById(R.id.tool_bar);
        //   setSupportActionBar(toolbar);
        mRecyclerView = (RecyclerView) findViewById(R.id.RecyclerView); // Assigning the RecyclerView Object to the xml View

        mRecyclerView.setHasFixedSize(true);                            // Letting the system know that the list objects are of fixed size

        mAdapter = new MyAdapter(TITLES, ICONS, name, email, profileBitmap, coverBitmap,  this);       // Creating the Adapter of MyAdapter class(which we are going to see in a bit)
        // And passing the titles,icons,header view recipe_name_view, header view email,
        // and header view profile picture

        mRecyclerView.setAdapter(mAdapter);                              // Setting the adapter to RecyclerView

        final GestureDetector mGestureDetector = new GestureDetector(ActivityCreateRecipe.this, new GestureDetector.SimpleOnGestureListener() {

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
                    Toast.makeText(ActivityCreateRecipe.this, "The Item Clicked is: " + recyclerView.getChildPosition(child), Toast.LENGTH_SHORT).show();
                    if (recyclerView.getChildPosition(child) == 1) {
                        //Go to Main
                        Intent i = new Intent(ActivityCreateRecipe.this,ActivityRecentMeals.class);
                        startActivity(i);
                    }
                    if (recyclerView.getChildPosition(child) == 2) {
                        Intent i = new Intent(ActivityCreateRecipe.this,ActivityCookbook.class);
                        startActivity(i);
                    }

                    if (recyclerView.getChildPosition(child) == 6) {
                        SharedPreferences sp = PreferenceManager
                                .getDefaultSharedPreferences(ActivityCreateRecipe.this);

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
                        Intent i = new Intent(ActivityCreateRecipe.this, LoginActivity.class);
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
        // parsing the data from the shared preferences
//        recipe_id = sp.getString("recipe_id", "");
/*

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
        new DownloadImageTask((ImageView) findViewById(R.id.imageViewSingleRecipe))
                .execute(recipe_image_url);

*/
        recipe_image = (ImageView) findViewById(R.id.recipe_image);
        recipe_name = (EditText) findViewById(R.id.create_recipe_name);
        ingredients = (EditText) findViewById(R.id.create_recipe_ingredients);
        instructions = (EditText) findViewById(R.id.create_recipe_instructions);

        btnCreate = (Button) findViewById(R.id.btn_create);
        btnCreate.setOnClickListener(this);
    }
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

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
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_create:
                new CreateRecipe().execute();

                break;
            case R.id.imageView1:
                break;
            default:
                break;
        }
    }
    class CreateRecipe extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread Show Progress Dialog
         * */

    Boolean bool_success=false;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(ActivityCreateRecipe.this);
            pDialog.setMessage("Creating Recipe...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... args) {
            // Checks for success tag
            int success;
            String string_recipe_name = recipe_name.getText().toString();
            String string_ingredients = ingredients.getText().toString();
            String string_instructions = instructions.getText().toString();

            try {
                // Building Parameters
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("creator_username", current_user));
                params.add(new BasicNameValuePair("recipe_name", string_recipe_name));
                params.add(new BasicNameValuePair("ingredients", string_ingredients));
                params.add(new BasicNameValuePair("instructions", string_instructions));

                Log.d("request!", "starting");

                // Posting user data to script
                JSONObject json = jsonParser.makeHttpRequest(CREATE_REC_URL, "POST",
                        params);


                // full json response
                Log.d("Creating recipe attempt", json.toString());

                // json success element
                success = json.getInt(TAG_SUCCESS);
                if (success == 1) {
                    Log.d("Recipe Created!", json.toString());
                    bool_success=true;
                    return json.getString(TAG_MESSAGE);
                } else {
                    Log.d("Creation Failure!", json.getString(TAG_MESSAGE));
                    bool_success=false;
                    return json.getString(TAG_MESSAGE);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;

        }

        /**
         * After completing background task Dismiss the progress dialog
         * **/
        protected void onPostExecute(String file_url) {
            // dismiss the dialog once product deleted
            pDialog.dismiss();
            if (file_url != null) {
                Toast.makeText(ActivityCreateRecipe.this, file_url,
                        Toast.LENGTH_LONG).show();
                if(bool_success){
                    Intent i = new Intent(ActivityCreateRecipe.this, ActivityCookbook.class);
                    finish();
                    startActivity(i);
                }
            }

        }

    }

}