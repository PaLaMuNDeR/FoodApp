package polimi.dima.foodapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;

import org.json.JSONArray;

import java.io.File;


public class MainActivity extends ActionBarActivity {
//        implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    private String current_user = "current_user_username";
    private String name = "name";
    private String username = "username";
    private String email = "email";
    private String photo = "photo";
    private String cover = "cover";
    private String gender = "gender";
    private Boolean logout = false;
    private GoogleApiClient mGoogleApiClient;
    ProgressDialog myPd_bar;
    private JSONArray mProfile = null;

    //First We Declare Titles And Icons For Our Navigation Drawer List View
    //This Icons And Titles Are holded in an Array as you can see

    String TITLES[] = {"Recent Meals", "My Cook Book", "Friends", "Liked", "Forum", "Logout"};
    int ICONS[] = {R.drawable.cutlery,
            R.drawable.open_book, R.drawable.forum, R.drawable.heart_dish,
            R.drawable.group_button, R.drawable.logout};

    int PROFILE = R.drawable.default_face;
    //private Toolbar toolbar; // Declaring the Toolbar Object
    RecyclerView mRecyclerView;                           // Declaring RecyclerView
    RecyclerView.Adapter mAdapter;                        // Declaring Adapter For Recycler View
    RecyclerView.LayoutManager mLayoutManager;            // Declaring Layout Manager as a linear layout manager
    DrawerLayout Drawer;                                  // Declaring DrawerLayout
Bitmap profileBitmap;
    ActionBarDrawerToggle mDrawerToggle;                  // Declaring Action Bar Drawer Toggle
Context mContext;

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link # restoreActionBar()}.
     */

    private CharSequence mTitle = "Recent Meals";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences sp = PreferenceManager
                .getDefaultSharedPreferences(MainActivity.this);
        current_user = sp.getString("username", "");
        name = sp.getString("name", "");
        email = sp.getString("email", "");
        photo = sp.getString("photo", "");
        cover = sp.getString("cover", "");
        DatabaseHandler db = new DatabaseHandler(MainActivity.this);
        Profile pf =  db.getLastProfile();
        name = pf.name;
        username = pf.username;
        photo = pf.photo;
        cover = pf.cover;
        gender = pf.gender;
        email = pf.email;
        String imagePath="";
        String coverPath="";
        BitmapDrawable coverBitmap = null;
        try {
                imagePath = Environment.getExternalStorageDirectory().toString() +"/sdcard/FoodApp/profile/user_photo.jpg";
                coverPath = Environment.getExternalStorageDirectory().toString() +"/sdcard/FoodApp/profile/cover_photo.jpg";
                File imgFile = new File("/sdcard/FoodApp/profile/user_photo.jpg");

                if(imgFile.exists()){
                    Log.d("Download Image","Profile Image - yes");
                    profileBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                }
                imgFile = new File("/sdcard/FoodApp/profile/cover_photo.jpg");
                if(imgFile.exists()){
                    Log.d("Download Image","Cover Image - yes");
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
        // And passing the titles,icons,header view name, header view email,
        // and header view profile picture

        mRecyclerView.setAdapter(mAdapter);                              // Setting the adapter to RecyclerView

        final GestureDetector mGestureDetector = new GestureDetector(MainActivity.this, new GestureDetector.SimpleOnGestureListener() {

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
                    Toast.makeText(MainActivity.this, "The Item Clicked is: " + recyclerView.getChildPosition(child), Toast.LENGTH_SHORT).show();
                    if (recyclerView.getChildPosition(child) == 6) {
                        SharedPreferences sp = PreferenceManager
                                .getDefaultSharedPreferences(MainActivity.this);

                        current_user = "";
                        name = "";
                        logout = true;
                        SharedPreferences.Editor edit = sp.edit();
                        edit.putString("username", current_user);
                        edit.putString("name", name);
                        edit.putBoolean("logout", true);
                        edit.commit();
                        Log.d("Log out - current_user", current_user);
                        Log.d("Log out - current name", name);
                        Intent i = new Intent(MainActivity.this, LoginActivity.class);
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
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        //   restoreActionBar();
        return true;
    }

    /*

        public void restoreActionBar() {
            ActionBar actionBar = getSupportActionBar();
            actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setTitle(mTitle);

        }
    */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }


        return super.onOptionsItemSelected(item);
    }


}

    /*
    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, PlaceholderFragment.newInstance(position + 1))
                .commit();
    }

    public void onSectionAttached(int number) {
        switch (number) {
            case 1:
                mTitle = getString(R.string.title_section1);
                break;
            case 2:
                mTitle = getString(R.string.title_section2);
                break;
            case 3:
                mTitle = getString(R.string.title_section3);
                break;
            case 4:
                SharedPreferences sp = PreferenceManager
                        .getDefaultSharedPreferences(MainActivity.this);

                current_user = "";
                current_name = "";
                logout = true;
                SharedPreferences.Editor edit = sp.edit();
                edit.putString("username", current_user);
                edit.putString("name", current_name);
                edit.putBoolean("logout",true);
                edit.commit();
                Log.d("Log out - current_user", current_user);
                Log.d("Log out - current name", current_name);
                Intent i = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(i);
                finish();
                break;
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.main, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    public static class PlaceholderFragment extends Fragment {

        private static final String ARG_SECTION_NUMBER = "section_number";


        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            return rootView;
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            ((MainActivity) activity).onSectionAttached(
                    getArguments().getInt(ARG_SECTION_NUMBER));
        }
    }

}
        */