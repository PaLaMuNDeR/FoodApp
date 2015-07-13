package polimi.dima.foodapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.software.shell.fab.ActionButton;

import org.json.JSONArray;

import java.io.File;

//AKA MainActivity
public class ActivityLiked extends ActionBarActivity {
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

    private Toast backtoast;


    //First We Declare Titles And Icons For Our Navigation Drawer List View
    //This Icons And Titles Are holded in an Array as you can see


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
    private FragmentNavigationDrawer mFragmentNavigationDrawer;

    /**
     * Used to store the last screen title. For use in {@link # restoreActionBar()}.
     */

    private CharSequence mTitle = "Recent Meals";

    public static Boolean isWifiAvailable(Context context) {
        ConnectivityManager connManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connManager
                .getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        /*NetworkInfo networkInfo1 = connManager.
                getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        if(networkInfo.isConnected() || networkInfo1.isConnected()){
            return true;
        }

        return false;
*/
        return networkInfo.isConnected();
    }

    private SwipeRefreshLayout swipeLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences sp = PreferenceManager
                .getDefaultSharedPreferences(ActivityLiked.this);
        current_user = sp.getString("username", "");
        name = sp.getString("name", "");
        email = sp.getString("email", "");
        photo = sp.getString("photo", "");
        cover = sp.getString("cover", "");
        SharedPreferences.Editor edit = sp.edit();
        edit.putBoolean("logout", false);
        edit.putBoolean("liked_activity_bool",true);
        edit.commit();
        setContentView(R.layout.activity_main);
        Log.d("Username", "LogoutBool in SP: " + sp.getBoolean("logout", false));
        DatabaseHandler db = new DatabaseHandler(ActivityLiked.this);
        Profile pf = db.getLastProfile();
        name = pf.name;
        username = pf.username;
        photo = pf.photo;
        cover = pf.cover;
        gender = pf.gender;
        email = pf.email;
        String imagePath = "";
        String coverPath = "";
        BitmapDrawable coverBitmap = null;
        try {
            File imgFile = new File("/sdcard/FoodApp/profile/user_photo.jpg");
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 3;
            if (imgFile.exists()) {
                Log.d("Download Image", "Profile Image - yes");
                profileBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath(),options);
            }

            imgFile = new File("/sdcard/FoodApp/profile/cover_photo.jpg");
            if (imgFile.exists()) {
                Log.d("Download Image", "Cover Image - yes");
                Bitmap bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath(),options);
                coverBitmap = new BitmapDrawable(getResources(), bitmap);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        //Setting the Navigation drawer
        String recent_meals = getResources().getString(R.string.recent_meals);
        String my_cook_book = getResources().getString(R.string.my_cook_book);
        String friends = getResources().getString(R.string.friends);
        String liked = getResources().getString(R.string.liked);
        String forum = getResources().getString(R.string.forum);
        String logout_string = getResources().getString(R.string.logout);
        String TITLES[] = {recent_meals, my_cook_book, friends, liked, forum, logout_string};
        int ICONS[] = {R.drawable.cutlery,
                R.drawable.open_book, R.drawable.follow, R.drawable.heart_dish_s_32,
                R.drawable.group_button, R.drawable.logout};
        mRecyclerView = (RecyclerView) findViewById(R.id.RecyclerView); // Assigning the RecyclerView Object to the xml View

        // Letting the system know that the list objects are of fixed size
        mRecyclerView.setHasFixedSize(true);

        mAdapter = new MyAdapter(TITLES, ICONS, name, email, profileBitmap, coverBitmap, this);       // Creating the Adapter of MyAdapter class(which we are going to see in a bit)
        // And passing the titles,icons,header view name, header view email,
        // and header view profile picture

        // Setting the adapter to RecyclerView
        mRecyclerView.setAdapter(mAdapter);

        final GestureDetector mGestureDetector = new GestureDetector(ActivityLiked.this,
                new GestureDetector.SimpleOnGestureListener() {

                    @Override
                    public boolean onSingleTapUp(MotionEvent e) {
                        return true;
                    }

                });


        mRecyclerView.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(RecyclerView recyclerView, MotionEvent
                    motionEvent) {
                View child = recyclerView.findChildViewUnder(motionEvent.getX(),
                        motionEvent.getY());
                if (child != null && mGestureDetector.onTouchEvent(motionEvent)) {
                    Drawer.closeDrawers();
                    //0 is the image on top
                    if (recyclerView.getChildPosition(child) == 1) {
                        Intent i = new Intent(ActivityLiked.this, ActivityRecentMeals.class);
                        startActivity(i);
                        finish();

                    }
                    if (recyclerView.getChildPosition(child) == 2) {
                        Intent i = new Intent(ActivityLiked.this, ActivityCookbook.class);
                        startActivity(i);
                        finish();
                    }
                    if (recyclerView.getChildPosition(child) == 3) {
                        Intent i = new Intent(ActivityLiked.this, ActivityFollowRecipes.class);
                        startActivity(i);
                        finish();
                    }
                    if (recyclerView.getChildPosition(child) == 4) {
                    //Remain here
                    }
                    if (recyclerView.getChildPosition(child) == 5) {
                        Intent i = new Intent(ActivityLiked.this, ActivityForum.class);
                        startActivity(i);
                        finish();
                    }
                    if (recyclerView.getChildPosition(child) == 6) {

                        //Getting the user settings
                        SharedPreferences sp = PreferenceManager
                                .getDefaultSharedPreferences(ActivityLiked.this);
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
                        Intent i = new Intent(ActivityLiked.this, ActivityLogin.class);
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
        // Drawer.setDrawerListener(mDrawerToggle); // Drawer Listener set to the Drawer toggle
        Drawer.setDrawerListener(mDrawerToggle);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mDrawerToggle.syncState();               // Finally we set the drawer toggle sync State

        // Swipe for refresh
        swipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {

            @Override
            public void onRefresh() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        swipeLayout.setRefreshing(false);
                        recreate();
                    }
                }, 4000);
            }

        });
        swipeLayout.setColorScheme(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
        FragmentRecentMealsListView myFragment = (FragmentRecentMealsListView) getFragmentManager().findFragmentById(R.id.fragment1);
        final ListView fragmentListView = myFragment.getListView();
        fragmentListView.setOnScrollListener(new AbsListView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem,
                                 int visibleItemCount, int totalItemCount) {
                boolean enable = false;
                if (fragmentListView != null && fragmentListView.getChildCount() > 0) {
                    // check if the first item of the list is visible
                    boolean firstItemVisible = fragmentListView.getFirstVisiblePosition() == 0;
                    // check if the top of the first item is visible
                    boolean topOfFirstItemVisible = fragmentListView.getChildAt(0).getTop() == 0;
                    // enabling or disabling the refresh layout
                    enable = firstItemVisible && topOfFirstItemVisible;
                }
                swipeLayout.setEnabled(enable);
            }
        });
        getSupportActionBar().setTitle(liked);

        //Floating Action Button
        ActionButton actionButton = (ActionButton) findViewById(R.id.action_button);
        actionButton.setRippleEffectEnabled(true);
        actionButton.setImageResource(R.drawable.fab_plus_icon);
        actionButton.setSize(60.0f);
        actionButton.setButtonColor(getResources().getColor(R.color.accentColor));
        actionButton.playShowAnimation();   // plays the show animation

        actionButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent i = new Intent(ActivityLiked.this, ActivityCreateRecipe.class);
                startActivity(i);
                finish();
            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();
        if (!isWifiAvailable(ActivityLiked.this)) {


            Toast.makeText(ActivityLiked.this, R.string.no_connection,
                    Toast.LENGTH_LONG).show();
        }
    }
    @Override
    public void onBackPressed(){
        super.onBackPressed();
        Intent i = new Intent(ActivityLiked.this, ActivityRecentMeals.class);
        startActivity(i);
        finish();
    }
    @Override
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


}