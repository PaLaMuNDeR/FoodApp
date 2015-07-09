package polimi.dima.foodapp;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.URL;
import java.net.URLConnection;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.IntentSender;
import android.os.Environment;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

//import polimi.dima.foodapp.ActivityOneFromRecPoi.DeleteRecommendation;
//import com.example.expoxmilano.ActivityRecommendations.AttemptTakeOnePoi;
//import com.example.expoxmilano.ActivityRecommendations.LoadRecommendation;
//import com.example.expoxmilanomilano.R;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;

public class ActivityLogin extends Activity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

	private EditText user, pass;
	private Button btnSubmit, btnRegister, btnPOI;

	//Drawer Layout
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    private CharSequence mDrawerTitle;
    private CharSequence mTitle;
    private String[] mDrawerTitles;

	// Progress Dialog
	private ProgressDialog pDialog;
	private ProgressDialog qDialog;
	private ProgressDialog rDialog;

	// JSON parser class
	JSONParser jsonParser = new JSONParser();

	// php login script location:

	// localhost :
	// testing on your device
	// put your local ip instead, on windows, run CMD > ipconfig
	// or in mac's terminal type ifconfig and look for the ip under en0 or en1
	// private static final String LOGIN_URL =
	// "http://xxx.xxx.x.x:1234/webservice/login.php";

	private static final String LOGIN_URL = "http://expox-milano.com/foodapp/login.php";
	private static final String LOGINGP_URL = "http://expox-milano.com/foodapp/logingp.php";
	private static final String REGISTER_URL = "http://expox-milano.com/foodapp/registergp.php";
	//registergp is the file for registration with GooglePlus

	// testing from a real server:
	// private static final String LOGIN_URL =
	// "http://www.yourdomain.com/webservice/login.php";

	// JSON element ids from repsonse of php script:
	private static final String TAG_SUCCESS = "success";
	private static final String TAG_MESSAGE = "message";

    private static final String TAG_ID = "user_id";
    private static final String TAG_NAME = "name";
    private static final String TAG_USERNAME = "username";
    private static final String TAG_VERSION = "version";
    private static final String TAG_PHOTO = "photo";
    private static final String TAG_COVER = "cover";
    private static final String TAG_GENDER = "gender";
    private static final String TAG_EMAIL = "email";



    // username
	private static String saved_username = "";

	// Connection to Internet Boolean
	boolean isConnectedToInternet = true;

    private static final int RC_SIGN_IN = 0;
    // Google client to communicate with Google
    private GoogleApiClient mGoogleApiClient;
    private boolean mIntentInProgress;
    private boolean signedInUser=false;
    private ConnectionResult mConnectionResult;
    private SignInButton signinButton;
    private ImageView image;
    private LinearLayout profileFrame, signinFrame;
    private String google_username, name, password;
    private String image_url, cover_url, email, gender;
    private boolean logout_bool = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
        mGoogleApiClient = new GoogleApiClient.Builder(this).addConnectionCallbacks(this).
                addOnConnectionFailedListener(this).addApi(Plus.API, Plus.PlusOptions.builder().
                build()).addScope(Plus.SCOPE_PLUS_LOGIN).build();

        try {
			// Loading whether there is logged-in user
			Log.d("Username","saved_username: " + saved_username);
			SharedPreferences sp = PreferenceManager
					.getDefaultSharedPreferences(ActivityLogin.this);
			String post_username = sp.getString("username", "");
            logout_bool = sp.getBoolean("logout", false);
            Log.d("Username", "Logout Bool: "+ logout_bool);
			Log.d("Username", "Current username: " + post_username);
			saved_username = post_username;
			Log.d("Username", "saved_username: "+ saved_username);

            if(logout_bool){
                saved_username="";
                signedInUser=false;
                Log.d("Username","signedInUser google bool: "+signedInUser);
             //   sp.edit().putBoolean("logout", logout_bool);
            }
			if (!saved_username.equals("")) {

                Log.d("Login","Username is " + saved_username);
                Intent i = new Intent(this, ActivityRecentMeals.class);
                startActivity(i);
                finish();
            }



		} catch (Exception e) {
			Log.e("Not having username", e.toString());
		}
		;

		// setup input fields
		user = (EditText) findViewById(R.id.et_username);
		pass = (EditText) findViewById(R.id.et_password);

		// setup buttons
		btnSubmit = (Button) findViewById(R.id.btn_login);
		btnSubmit.setOnClickListener(new OnClickListener() {
		    public void onClick(View v) {
				new AttemptLogin().execute();
		    }
		});
		btnRegister = (Button) findViewById(R.id.btn_register);
		btnRegister.setOnClickListener(new OnClickListener() {
		    public void onClick(View w) {
				Intent reg = new Intent(ActivityLogin.this, ActivityRegister.class);
				startActivity(reg);		   
		    }
		});


        //Google Sign in Button
        signinButton = (SignInButton) findViewById(R.id.signin);
        signinButton.setOnClickListener(new OnClickListener() {
            public void onClick(View w) {

                //In case the user is already loged in with Google Plus account,
                // we update the logout_bool to false and set the
                //Google profile to true
                //if he is not - we sign him in
                if(signedInUser){
                    logout_bool=false;
                    updateProfile(true);
                }
                else{
                    googlePlusLogin();}
            }});

        image = (ImageView) findViewById(R.id.image);

        signinFrame = (LinearLayout) findViewById(R.id.signinFrame);



		/*
		btnPOI = (Button) findViewById(R.id.btn_poi);
		btnPOI.setOnClickListener(new OnClickListener() {
		    public void onClick(View w) {
		    	final Dialog dialog_rec = new Dialog(LoginActivity.this);
				dialog_rec.setTitle(getResources().getString(R.string.browse_poi));
				dialog_rec.setContentView(R.layout.custom_dialog_layout_2);

				Button Button1 = (Button) dialog_rec.findViewById(R.id.button1);
				Button1.setText(R.string.top_ten);
				final Intent intent_top_rec = new Intent(LoginActivity.this, ActivityRecTopTen.class);
				Button1.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View view) {
						startActivity(intent_top_rec);
						finish();
						dialog_rec.hide();
					}
				});
				
				Button Button2 = (Button) dialog_rec.findViewById(R.id.button2);
				Button2.setText(R.string.all_poi);
				final Intent intent_all_rec = new Intent(LoginActivity.this, ActivityAllPoi.class);
				Button2.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View view) {
						startActivity(intent_all_rec);
						finish();
						dialog_rec.hide();
					}
				});
				
				dialog_rec.show();		   
		    }
		});
		
	*/
        /*
		//Drawer navigation
	        mTitle = mDrawerTitle = getTitle();
	        	mDrawerTitles = getResources().getStringArray(R.array.draw_login);
	        mDrawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout_login);
	        mDrawerList = (ListView) findViewById(R.id.left_drawer);
			
	        // set a custom shadow that overlays the main content when the drawer opens
	       // mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
	        // set up the drawer's list view with items and click listener
	        mDrawerList.setAdapter(new ArrayAdapter<String>(LoginActivity.this,
	                R.layout.drawer_list_item, mDrawerTitles));
	        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
	
	        // enable ActionBar app icon to behave as action to toggle nav drawer
	        getActionBar().setDisplayHomeAsUpEnabled(true);
	        getActionBar().setHomeButtonEnabled(true);
	        

	        // ActionBarDrawerToggle ties together the the proper interactions
	        // between the sliding drawer and the action bar app icon
	        mDrawerToggle = new ActionBarDrawerToggle(
	                this,                  // host Activity /
	                mDrawerLayout,         // DrawerLayout object /
	                R.drawable.ic_drawer,  // nav drawer image to replace 'Up' caret /
	                R.string.drawer_open,  // "open drawer" description for accessibility
	                R.string.drawer_close  // "close drawer" description for accessibility
	                ) {
	            public void onDrawerClosed(View view) {
	                getActionBar().setTitle(mTitle);
	                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
	            }

	            public void onDrawerOpened(View drawerView) {
	                getActionBar().setTitle(mDrawerTitle);
	                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
	            }
	        };
	        mDrawerLayout.setDrawerListener(mDrawerToggle);

	      //  if (savedInstanceState == null) {
	      //      selectItem(0);
	      //  }
	    */
		}

    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
        if(!signedInUser)
        {
            googlePlusLogout();
        }
    }

    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }


    //If we want Menu button on the top right of the action bar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }
    /* Called whenever we call invalidateOptionsMenu() */
    //    @Override
    //    public boolean onPrepareOptionsMenu(Menu menu) {
    //        // If the nav drawer is open, hide action items related to the content view
    //        boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
    //        menu.findItem(R.id.action_websearch).setVisible(!drawerOpen);
    //        return super.onPrepareOptionsMenu(menu);
    //    }
/*
	//Options for the drawer
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
         // The action bar home/up action should open or close the drawer.
         // ActionBarDrawerToggle will take care of this.
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        // Handle action buttons
        switch(item.getItemId()) {
        //if we want the websearch button on the right
		//        case R.id.action_websearch:
		//            // create intent to perform web search for this planet
		//            Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);
		//            intent.putExtra(SearchManager.QUERY, getActionBar().getTitle());
		//            // catch event that there's no activity to handle intent
		//            if (intent.resolveActivity(getPackageManager()) != null) {
		//                startActivity(intent);
		//            } else {
		//                Toast.makeText(this, R.string.app_not_available, Toast.LENGTH_LONG).show();
		//            }
		//            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    /* The click listner for ListView in the navigation drawer
    private class DrawerItemClickListener implements OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectItem(position);
        }
    }

    //Select Item for the drawer
    private void selectItem(int position) {
    	// update the main content by replacing fragments
//		        Fragment fragment = new PlanetFragment();
//		        Bundle args = new Bundle();
//		        args.putInt(PlanetFragment.ARG_PLANET_NUMBER, position);
//		        fragment.setArguments(args);

		       // FragmentManager fragmentManager = getFragmentManager();
		        //fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();

    	switch (position) {
    	default:
    		break;
    	case 0:
    		Intent k = new Intent(this, LoginActivity.class);
    		startActivity(k);
    		finish();
			break;
    	case 1:
			// saves the logout user data
//			current_user = "";
//
//			Editor edit = sp.edit();
//			edit.putString("username", current_user);
//			edit.putString("name", current_name);
//			edit.commit();
//			Log.d("Log out - current_user", current_user);
//			Log.d("Log out - current name", current_name);
			//ClearPreferences();

			Intent q = new Intent(this, RegisterActivity.class);
			startActivity(q);
			finish();
			break;
		case 2:
			Intent s = new Intent(this, ActivityRecTopTen.class);
			startActivity(s);
			finish();
			break;
		case 3:
			Intent z = new Intent(this, ActivityAllPoi.class);
			startActivity(z);
			finish();
			break;

		}


        // update selected item and title, then close the drawer
        mDrawerList.setItemChecked(position, true);
        setTitle(mDrawerTitles[position]);
        mDrawerLayout.closeDrawer(mDrawerList);
    }

    */
    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getActionBar().setTitle(mTitle);
    }
    /*
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggls
        mDrawerToggle.onConfigurationChanged(newConfig);
    }
		*/

    private void resolveSignInError() {
        if (mConnectionResult.hasResolution()) {
            try {
                mIntentInProgress = true;
                mConnectionResult.startResolutionForResult(this, RC_SIGN_IN);
            } catch (IntentSender.SendIntentException e) {
                mIntentInProgress = false;
                mGoogleApiClient.connect();
            }
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        if (!result.hasResolution()) {
            GooglePlayServicesUtil.getErrorDialog(result.getErrorCode(), this, 0).show();
            return;
        }

        if (!mIntentInProgress) {
            // store mConnectionResult
            mConnectionResult = result;

            if (signedInUser) {
                resolveSignInError();
            }
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int responseCode, Intent intent) {
        switch (requestCode) {
            case RC_SIGN_IN:
                if (responseCode == RESULT_OK) {
                    signedInUser = false;

                }
                mIntentInProgress = false;
                if (!mGoogleApiClient.isConnecting()) {
                    mGoogleApiClient.connect();
                }
                break;
        }
    }
    @Override
    public void onConnected(Bundle arg0) {
        signedInUser=true;
        getProfileInformation();
        updateProfile(signedInUser);
    }

    private void updateProfile(boolean isSignedIn) {

        SharedPreferences sp = PreferenceManager
                .getDefaultSharedPreferences(ActivityLogin.this);
        Editor edit = sp.edit();
        if (isSignedIn) {
            Log.d("Login Successful!", "");
            // save user data

            Log.d("Name", "Taking name " + name);

            edit.putString("username", name);
            edit.putString("name", name);
            edit.putString("email", email);
            edit.commit();
            //(After first run) If the user is signed in with Google Account,
            // but he has clicked Log out, we check the boolean loogout_bool and if it is true, we don't do anything,
            // otherwise we sign him in with Google Account
            Log.d("Username","Logout Bool: "+logout_bool);
            if(logout_bool){
                edit.putString("username", name);
                edit.putString("name", name);
                edit.commit();
                saved_username = "";
                Log.d("saved_username", saved_username);

                Log.d("Login Failure!", "");

                return ;
            }
            else{
                saved_username = name;
                Log.d("saved_username", saved_username);
                edit.putString("username", name);
                edit.putString("name", name);
                edit.putBoolean("logout",false);
                edit.commit();

                new AutomaticCreateUser().execute();

                new AutomaticLoginUser().execute();

            }
        } else {
            name="";
            edit.putString("username", name);
            edit.putString("name", name);
            edit.commit();
            saved_username = "";
            Log.d("saved_username", saved_username);

            Log.d("Login Failure!", "");

            return ;
        }

    }

    private void getProfileInformation() {
        try {
            if (Plus.PeopleApi.getCurrentPerson(mGoogleApiClient) != null) {
                Person currentPerson = Plus.PeopleApi.getCurrentPerson(mGoogleApiClient);
                String personName = currentPerson.getDisplayName();
                String personPhotoUrl = currentPerson.getImage().getUrl();
                String personCoverUrl = currentPerson.getCover().getCoverPhoto().getUrl();
                String g_gender = "male";
                if(currentPerson.hasGender()) {
                    Integer i_gender = currentPerson.getGender();
                    if(i_gender==1)
                        g_gender = "female";
                }

                String g_email = Plus.AccountApi.getAccountName(mGoogleApiClient);

                name = personName;
                google_username = g_email.split("@")[0];
                email = g_email;
                image_url = personPhotoUrl;
                cover_url = personCoverUrl;
                gender = g_gender;

                SharedPreferences sp = PreferenceManager
                        .getDefaultSharedPreferences(ActivityLogin.this);
                Editor edit = sp.edit();
                edit.putString("username", google_username);
                edit.putString("name", name);
                edit.putString("email", email);
                edit.putString("photo", image_url);
                edit.putString("cover", cover_url);
                edit.commit();
                // update profile frame with new info about Google Account
                // profile
                updateProfile(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Override
    public void onConnectionSuspended(int cause) {
        mGoogleApiClient.connect();
        updateProfile(false);
    }

    public void signIn(View v) {
        googlePlusLogin();
    }

    public void logout(View v) {
        googlePlusLogout();
    }

    private void googlePlusLogin() {
        if (!mGoogleApiClient.isConnecting()) {
            signedInUser = true;
            resolveSignInError();
        }
    }

    private void googlePlusLogout() {
        if (mGoogleApiClient.isConnected()) {
            signedInUser=true;
            Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
            mGoogleApiClient.disconnect();
            mGoogleApiClient.connect();
            updateProfile(false);
        }
    }





    class AttemptLogin extends AsyncTask<String, String, String> {

		/**
		 * Before starting background thread Show Progress Dialog
		 * */
		boolean failure = false;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pDialog = new ProgressDialog(ActivityLogin.this);
			pDialog.setMessage("Attempting login...");
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(true);
			pDialog.show();
		}

		@Override
		protected String doInBackground(String... args) {

			// Check for success and name tags
			int success;
            String id;
			String name;
			String username;
			String email;
			String photo;
			String cover;

			// input those strings
			String u_username = user.getText().toString();
			String u_password = pass.getText().toString();
			try {
				isConnectedToInternet = true;
				// Building Parameters
				List<NameValuePair> params = new ArrayList<NameValuePair>();
				params.add(new BasicNameValuePair("username", u_username));
				params.add(new BasicNameValuePair("password", u_password));

				Log.d("request!", "starting");
				// getting product details by making HTTP request
				JSONObject json = jsonParser.makeHttpRequest(LOGIN_URL, "POST",
						params);

				// check your log for json response
				Log.d("Login attempt", json.toString());


                // json success tag
                success = json.getInt(TAG_SUCCESS);
                if (success == 1) {
                    Log.d("Login Successful!", json.toString());
                    // save user data
                    id = json.getString(TAG_ID);
                    name = json.getString(TAG_NAME);
                    username = json.getString(TAG_USERNAME);
                    photo = json.getString(TAG_PHOTO);
                    cover = json.getString(TAG_COVER);
                    gender = json.getString(TAG_GENDER);
                    email = json.getString(TAG_EMAIL);
                    Log.d("Name", "Taking name " + name);
                    String photo_save_name = "user_photo";
                    String cover_save_name = "cover_photo";

                    SharedPreferences sp = PreferenceManager
                            .getDefaultSharedPreferences(ActivityLogin.this);
                    Editor edit = sp.edit();
                    edit.putString("username", u_username);
                    edit.putString("name", name);
                    edit.putString("user_id",id);
                    edit.commit();
                    saved_username = u_username;
                    Log.d("saved_username", saved_username);

                    // gets the content of each tag
                    Log.d("Download", "Downloading profile image...");
                    Log.d("Download", "From " + photo);
                    Log.d("Download", "To " + photo_save_name);
                    DownloadFileFromURL(photo_save_name, photo);
                    // gets the content of each tag
                    Log.d("Download", "Downloading cover image...");
                    Log.d("Download", "From " + cover);
                    Log.d("Download", "To " + cover_save_name);
                    DownloadFileFromURL(cover_save_name, cover);

                    try {
                        DatabaseHandler db = new DatabaseHandler(ActivityLogin.this);

                        Log.d("Database", "Inserting...");
                        db.addProfile(new Profile(name, username, photo, cover, gender, email));
                    } catch (Exception e) {
                        Log.e("Error JSON Parser",
                                "Error when parsing the JSON");
                    }
                    Intent i = new Intent(ActivityLogin.this,
                            ActivityRecentMeals.class);
                    finish();
                    startActivity(i);
                    return json.getString(TAG_MESSAGE);
                } else {
                    Log.d("Login Failure!", json.getString(TAG_MESSAGE));
                    return json.getString(TAG_MESSAGE);
                }
			} catch (JSONException e) {

				e.printStackTrace();
				Log.e("Connection", e.toString());
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
				Toast.makeText(ActivityLogin.this, file_url, Toast.LENGTH_LONG)
						.show();
			}

		}

	}



    class AutomaticLoginUser extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread Show Progress Dialog
         * */
        boolean failure = false;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
         /*   qDialog = new ProgressDialog(LoginActivity.this);
            qDialog.setMessage("Preparing your cookbook...");
            qDialog.setIndeterminate(false);
            qDialog.setCancelable(true);
            qDialog.show();
       */ }

        @Override
        protected String doInBackground(String... args) {
            // TODO Auto-generated method stub
            // Check for success tag
            int success;


            password = new SessionIdentifierGenerator().nextSessionId();

            try {
                // Building Parameters
                List<NameValuePair> params = new ArrayList<NameValuePair>();
               // params.add(new BasicNameValuePair("username", google_username));
                //params.add(new BasicNameValuePair("name", name));
                params.add(new BasicNameValuePair("email", email));
               // params.add(new BasicNameValuePair("password", password));
               // params.add(new BasicNameValuePair("gender", gender));

                Log.d("request!", "starting");

                // Posting user data to script
                JSONObject json = jsonParser.makeHttpRequest(LOGINGP_URL, "POST",
                        params);

                // full json response
                Log.d("Login attempt", json.toString());


                // json success tag
                success = json.getInt(TAG_SUCCESS);
                if (success == 1) {
                    Log.d("Login Successful!", json.toString());

                    String user_id="";
                    String username="";
                    String photo="";
                    String cover="";
                    // save user data
                    user_id = json.getString(TAG_ID);
                    name = json.getString(TAG_NAME);
                    username = json.getString(TAG_USERNAME);
                    photo = json.getString(TAG_PHOTO);
                    cover = json.getString(TAG_COVER);
                    gender = json.getString(TAG_GENDER);
                    email = json.getString(TAG_EMAIL);
                    Log.d("Name", "Taking name " + name);
                    String photo_save_name = "user_photo";
                    String cover_save_name = "cover_photo";

                    SharedPreferences sp = PreferenceManager
                            .getDefaultSharedPreferences(ActivityLogin.this);
                    Editor edit = sp.edit();
                    edit.putString("username", username);
                    edit.putString("name", name);
                    edit.putString("user_id",user_id);
                    edit.commit();
                    saved_username = username;
                    Log.d("saved_username", saved_username);

                    // gets the content of each tag
                    Log.d("Download", "Downloading profile image...");
                    Log.d("Download", "From " + photo);
                    Log.d("Download", "To " + photo_save_name);
                    DownloadFileFromURL(photo_save_name, photo);
                    // gets the content of each tag
                    Log.d("Download", "Downloading cover image...");
                    Log.d("Download", "From " + cover);
                    Log.d("Download", "To " + cover_save_name);
                    DownloadFileFromURL(cover_save_name, cover);

                    try {
                        DatabaseHandler db = new DatabaseHandler(ActivityLogin.this);

                        Log.d("Database", "Inserting...");
                        db.addProfile(new Profile(name, username, photo, cover, gender, email));
                    } catch (Exception e) {
                        Log.e("Error JSON Parser",
                                "Error when parsing the JSON");
                    }
                    Intent i = new Intent(ActivityLogin.this,
                            ActivityRecentMeals.class);
                    finish();
                    startActivity(i);
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
         //   qDialog.dismiss();
            if (file_url != null) {
                Toast.makeText(ActivityLogin.this, file_url,
                        Toast.LENGTH_LONG).show();
            }


        }


}
    class AutomaticCreateUser extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread Show Progress Dialog
         * */
        boolean failure = false;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
          /*  rDialog = new ProgressDialog(LoginActivity.this);
            rDialog.setMessage("Heating the stove...");
            rDialog.setIndeterminate(false);
            rDialog.setCancelable(true);
            rDialog.show();
        */}

        @Override
        protected String doInBackground(String... args) {
            // TODO Auto-generated method stub
            // Check for success tag
            int success;


            password = new SessionIdentifierGenerator().nextSessionId();

            try {
                // Building Parameters
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("username", google_username));
                params.add(new BasicNameValuePair("name", name));
                params.add(new BasicNameValuePair("email", email));
                params.add(new BasicNameValuePair("password", password));
                params.add(new BasicNameValuePair("gender", gender));
                params.add(new BasicNameValuePair("photo", image_url));
                params.add(new BasicNameValuePair("cover", cover_url));

                Log.d("request!", "starting");

                // Posting user data to script
                JSONObject json = jsonParser.makeHttpRequest(REGISTER_URL, "POST",
                        params);

                // full json response
                Log.d("Login attempt", json.toString());


                // json success tag
                success = json.getInt(TAG_SUCCESS);
                if (success == 1) {
                    Log.d("Login Successful!", json.toString());
                    String username="";
                    String photo="";
                    String cover="";
                    // save user data
                    name = json.getString(TAG_NAME);
                    username = json.getString(TAG_USERNAME);
                    photo = json.getString(TAG_PHOTO);
                    cover = json.getString(TAG_COVER);
                    gender = json.getString(TAG_GENDER);
                    email = json.getString(TAG_EMAIL);
                    Log.d("Name", "Taking name " + name);
                    String photo_save_name = "user_photo";
                    String cover_save_name = "cover_photo";

                    SharedPreferences sp = PreferenceManager
                            .getDefaultSharedPreferences(ActivityLogin.this);
                    Editor edit = sp.edit();
                    edit.putString("username", username);
                    edit.putString("name", name);
                    edit.commit();
                    saved_username = username;
                    Log.d("saved_username", saved_username);

                    // gets the content of each tag
                    Log.d("Download", "Downloading profile image...");
                    Log.d("Download", "From " + photo);
                    Log.d("Download", "To " + photo_save_name);
                    DownloadFileFromURL(photo_save_name, photo);
                    // gets the content of each tag
                    Log.d("Download", "Downloading cover image...");
                    Log.d("Download", "From " + cover);
                    Log.d("Download", "To " + cover_save_name);
                    DownloadFileFromURL(cover_save_name, cover);

                    try {
                        DatabaseHandler db = new DatabaseHandler(ActivityLogin.this);

                        Log.d("Database", "Inserting...");
                        db.addProfile(new Profile(name, username, photo, cover, gender, email));
                    } catch (Exception e) {
                        Log.e("Error JSON Parser",
                                "Error when parsing the JSON");
                    }
                    Intent i = new Intent(ActivityLogin.this,
                            ActivityRecentMeals.class);
                    finish();
                    startActivity(i);
                    return json.getString(TAG_MESSAGE);
                } else {
                    Log.d("Login Failure!", json.getString(TAG_MESSAGE));
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
          //  rDialog.dismiss();
            if (file_url != null) {
                Toast.makeText(ActivityLogin.this, file_url,
                        Toast.LENGTH_LONG).show();
            }

        }


    }
    //Class for generating random password when the user has logged in with Google Plus
    public final class SessionIdentifierGenerator {
        private SecureRandom random = new SecureRandom();

        public String nextSessionId() {
            return new BigInteger(130, random).toString(32);
        }
    }
    protected String DownloadFileFromURL(String saved, String... f_url) {
        int count;
        try {

            URL url = new URL(f_url[0]);
            URLConnection connection = url.openConnection();
            connection.connect();
            // getting file length
            int lenghtOfFile = connection.getContentLength();

            // input stream to read file - with 8k buffer
            InputStream input = new BufferedInputStream(url.openStream(), 8192);

            //Check whether such folder already exists
            File folder = new File(Environment.getExternalStorageDirectory() + "/FoodApp/profile");
            boolean success = true;
            if (!folder.exists()) {
                success = folder.mkdirs();
            }
            // Output stream to write file
            OutputStream output = new FileOutputStream("/sdcard/FoodApp/profile/"+saved+".jpg");

            byte data[] = new byte[1024];

            long total = 0;

            while ((count = input.read(data)) != -1) {
                total += count;
                // publishing the progress....
                // After this onProgressUpdate will be called
                Log.d("Download","Progress..."+((100*total)/(total+count)));
                // writing data to file
                output.write(data, 0, count);

            }

            // flushing output
            output.flush();

            // closing streams
            output.close();
            input.close();
            Log.d("Download", "Download was successful");
        } catch (Exception e) {
            Log.e("Error: ", e.getMessage());
        }

        return null;

    }


}