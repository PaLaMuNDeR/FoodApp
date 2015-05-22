package polimi.dima.foodapp;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;



import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.Toast;

public class RegisterActivity extends Activity implements OnClickListener {

	private EditText user, name, pass, mail;//, age_value;
    private String gend = "NULL";
    private Button mRegister;
	private RadioButton rbutton1, rbutton2, rbuttonOver14, rbuttonUnder14;

	// Progress Dialog
	private ProgressDialog pDialog;

	// JSON parser class
	JSONParser jsonParser = new JSONParser();

	//Drawer Layout
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    private CharSequence mDrawerTitle;
    private CharSequence mTitle;
    private String[] mDrawerTitles;

	// php login script

	// localhost :
	// testing on your device
	// put your local ip instead, on windows, run CMD > ipconfig
	// or in mac's terminal type ifconfig and look for the ip under en0 or en1
	// private static final String LOGIN_URL =
	// "http://xxx.xxx.x.x:1234/webservice/register.php";

	// testing on Emulator:
	private static final String LOGIN_URL = "http://expox-milano.com/foodapp/register.php";

	// testing from a real server:
	// private static final String LOGIN_URL =
	// "http://www.yourdomain.com/webservice/register.php";

	// ids
	private static final String TAG_SUCCESS = "success";
	private static final String TAG_MESSAGE = "message";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        user = (EditText) findViewById(R.id.username);
        name = (EditText) findViewById(R.id.name);
        pass = (EditText) findViewById(R.id.password);
        mail = (EditText) findViewById(R.id.email);
        //  age_value = (EditText) findViewById(R.id.age);


        mRegister = (Button) findViewById(R.id.register);
        mRegister.setOnClickListener(this);

		/*		//Drawer navigation
	        mTitle = mDrawerTitle = getTitle();
	        	mDrawerTitles = getResources().getStringArray(R.array.draw_login);
	        mDrawerLayout = (DrawerLayout)findViewById(R.id.drawer_reg);
	        mDrawerList = (ListView) findViewById(R.id.left_drawer);

	        // set a custom shadow that overlays the main content when the drawer opens
	       // mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
	        // set up the drawer's list view with items and click listener
	        mDrawerList.setAdapter(new ArrayAdapter<String>(this,
	                R.layout.drawer_list_item, mDrawerTitles));
	        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
	
	        // enable ActionBar app icon to behave as action to toggle nav drawer
	        getActionBar().setDisplayHomeAsUpEnabled(true);
	        getActionBar().setHomeButtonEnabled(true);


	        // ActionBarDrawerToggle ties together the the proper interactions
	        // between the sliding drawer and the action bar app icon
	        mDrawerToggle = new ActionBarDrawerToggle(
	                this,                  // host Activity
	                mDrawerLayout,         // DrawerLayout object /
	                R.drawable.ic_drawer,  // nav drawer image to replace 'Up' caret /
	                R.string.drawer_open,  // "open drawer" description for accessibility /
	                R.string.drawer_close  // "close drawer" description for accessibility /
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

        public void onRadioButtonClicked(View view) {
            boolean checked = ((RadioButton) view).isChecked();
            switch (view.getId()) {
                case R.id.radioButton1:
                    if (checked)
                        gend = "male";
                    break;
                case R.id.radioButton2:
                    if (checked)
                        gend = "female";
                    break;
                default: break;
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
        /*if we want the websearch button on the right*/
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
	
    /* The click listner for ListView in the navigation drawer */
    private class DrawerItemClickListener implements ListView.OnItemClickListener {
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
		/*case 2:
			Intent s = new Intent(this, ActivityRecTopTen.class);
			startActivity(s);
			finish();
			break;
		case 3:
			Intent z = new Intent(this, ActivityAllPoi.class);
			startActivity(z);
			finish();
			break;
		*/
		}
    	
    	
        // update selected item and title, then close the drawer
        mDrawerList.setItemChecked(position, true);
        setTitle(mDrawerTitles[position]);
        mDrawerLayout.closeDrawer(mDrawerList);
    }
    
    
    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getActionBar().setTitle(mTitle);
    }

/*    @Override
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



	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if (v == findViewById(R.id.register))
			new CreateUser().execute();

	}

	class CreateUser extends AsyncTask<String, String, String> {

		/**
		 * Before starting background thread Show Progress Dialog
		 * */
		boolean failure = false;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pDialog = new ProgressDialog(RegisterActivity.this);
			pDialog.setMessage("Creating User...");
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(true);
			pDialog.show();
		}

		@Override
		protected String doInBackground(String... args) {
			// TODO Auto-generated method stub
			// Check for success tag
			int success;
			String username = user.getText().toString();
			String user_name = name.getText().toString();
			String password = pass.getText().toString();
			String email = mail.getText().toString();
			String gender = gend;
			//String age = age_value.getText().toString();

			try {
				// Building Parameters
				List<NameValuePair> params = new ArrayList<NameValuePair>();
				params.add(new BasicNameValuePair("username", username));
				params.add(new BasicNameValuePair("name", user_name));
				params.add(new BasicNameValuePair("email", email));
				params.add(new BasicNameValuePair("password", password));
				params.add(new BasicNameValuePair("gender", gender));
			//	params.add(new BasicNameValuePair("age", age));

				Log.d("request!", "starting");

				// Posting user data to script
				JSONObject json = jsonParser.makeHttpRequest(LOGIN_URL, "POST",
						params);


				// full json response
				Log.d("Login attempt", json.toString());

				// json success element
				success = json.getInt(TAG_SUCCESS);
				if (success == 1) {
					Log.d("User Created!", json.toString());
					finish();
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
			pDialog.dismiss();
			if (file_url != null) {
				Toast.makeText(RegisterActivity.this, file_url,
						Toast.LENGTH_LONG).show();
			}

		}

	}

}