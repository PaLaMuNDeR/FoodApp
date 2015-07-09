package polimi.dima.foodapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

/**
 * Created by Marti on 19/06/2015.
 */

public class ActivitySingleRecipeEdit extends ActionBarActivity implements View.OnClickListener {
    private String current_name = "current_user_name";

    // Progress Dialog
    private ProgressDialog sDialog;
    private EditText recipe_name, ingredients, instructions;
    private CheckBox keep_image;
    private ImageButton recipe_image_button;
    private ImageView original_image;
    private Button btnCreate;

    // Progress Dialog
    private ProgressDialog pDialog;
    private ProgressDialog qDialog;


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
            R.drawable.open_book, R.drawable.follow, R.drawable.heart_dish,
            R.drawable.group_button, R.drawable.logout};

    private static String recipe_id = "";

    JSONParser jsonParser = new JSONParser();
    private static final String CREATE_REC_URL = "http://expox-milano.com/foodapp/create_rec.php";
    private static final String UPLOAD_IMAGE_URL = "http://expox-milano.com/foodapp/imgupload/upload_image.php";
    private String uploaded_image_url;

    // JSON element ids from repsonse of php script:
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_MESSAGE = "message";

    //taking image from the gallery
    private static int RESULT_LOAD_IMG = 1;
    ProgressDialog prgDialog;
    String encodedString;
    RequestParams image_params = new RequestParams();
    String imgPath, fileName;
    Bitmap bitmap;
    boolean pressed_keep_image;
    String recipe_image_url;

    //for uploading from camera
    private static final String TAG = ActivitySingleRecipeEdit.class.getSimpleName();


    // Camera activity request codes
    private static final int CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 100;
    private static final int CAMERA_CAPTURE_VIDEO_REQUEST_CODE = 200;

    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;

    private Uri fileUri; // file url to store image

    private Button btnCapturePicture;
    boolean camera_bool=false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_recipe);
        SharedPreferences sp = PreferenceManager
                .getDefaultSharedPreferences(ActivitySingleRecipeEdit.this);
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
        DatabaseHandler db = new DatabaseHandler(ActivitySingleRecipeEdit.this);
        Profile pf = db.getLastProfile();
        name = pf.name;
        username = pf.username;
        photo = pf.photo;
        cover = pf.cover;
        gender = pf.gender;
        email = pf.email;
        BitmapDrawable coverBitmap = null;
        try {
            File imgFile = new File("/sdcard/FoodApp/profile/user_photo.jpg");

            if (imgFile.exists()) {
                profileBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
            }
            imgFile = new File("/sdcard/FoodApp/profile/cover_photo.jpg");
            if (imgFile.exists()) {
                Bitmap bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
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

        // Setting the adapter to RecyclerView
        mRecyclerView.setAdapter(mAdapter);

        final GestureDetector mGestureDetector = new GestureDetector(ActivitySingleRecipeEdit.this, new GestureDetector.SimpleOnGestureListener() {

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
                    Toast.makeText(ActivitySingleRecipeEdit.this, "The Item Clicked is: " +
                            recyclerView.getChildPosition(child), Toast.LENGTH_SHORT).show();
                    if (recyclerView.getChildPosition(child) == 1) {
                        //Go to Main
                        Intent i = new Intent(ActivitySingleRecipeEdit.this, ActivityRecentMeals.class);
                        startActivity(i);
                    }
                    if (recyclerView.getChildPosition(child) == 2) {
                        Intent i = new Intent(ActivitySingleRecipeEdit.this, ActivityCookbook.class);
                        startActivity(i);
                    }
                    if (recyclerView.getChildPosition(child) == 4) {
                        Intent i = new Intent(ActivitySingleRecipeEdit.this, ActivityLiked.class);
                        startActivity(i);
                        finish();

                    }

                    if (recyclerView.getChildPosition(child) == 6) {
                        SharedPreferences sp = PreferenceManager
                                .getDefaultSharedPreferences(ActivitySingleRecipeEdit.this);

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
                        Intent i = new Intent(ActivitySingleRecipeEdit.this, ActivityLogin.class);
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


        // Creating a layout Manager
        mLayoutManager = new LinearLayoutManager(this);

        // Setting the layout Manager
        mRecyclerView.setLayoutManager(mLayoutManager);


        // Drawer object Assigned to the view
        Drawer = (DrawerLayout) findViewById(R.id.DrawerLayout);
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
        recipe_id = sp.getString("recipe_id", "");


        String r_name_value = sp.getString("recipe_name", "");
        recipe_name = (EditText) findViewById(R.id.create_recipe_name);
        recipe_name.setText(r_name_value);
        TextView moto = (TextView) findViewById(R.id.textViewMoto);
        moto.setText(r_name_value);
        getSupportActionBar().setTitle(r_name_value);

        String ingredients_value = sp.getString("ingredients", "");
        ingredients = (EditText) findViewById(R.id.create_recipe_ingredients);
        ingredients.setText(ingredients_value);

        String instructions_value = sp.getString("instructions", "");
        instructions = (EditText) findViewById(R.id.create_recipe_instructions);
        instructions.setText(instructions_value);

        recipe_image_url = sp.getString("recipe_image_url", "");
        //TODO fix


        recipe_image_button = (ImageButton) findViewById(R.id.recipe_image_button);

        btnCreate = (Button) findViewById(R.id.btn_create);
        btnCreate.setOnClickListener(this);
        prgDialog = new ProgressDialog(this);
        recipe_image_button.setOnClickListener(this);


        //for taking image from camera
        btnCapturePicture = (Button) findViewById(R.id.btnCapturePicture);

        /**
         * Capture image button click event
         */
        btnCapturePicture.setOnClickListener(this);

        if (!isDeviceSupportCamera()) {
            Toast.makeText(getApplicationContext(),
                    "Sorry! Your device doesn't support camera",
                    Toast.LENGTH_LONG).show();
            // will close the app if the device does't have camera
            finish();
        }
        //This check is needed when the app is returning from the Camera activity.
        //We set here the image on the button to be the one from the camera.
        camera_bool = sp.getBoolean("camera_bool",false);
        if(camera_bool) {
            imgPath = sp.getString("imgPath", imgPath);
            // bimatp factory
            BitmapFactory.Options options = new BitmapFactory.Options();

            // down sizing image as it throws OutOfMemory Exception for larger
            // images
            options.inSampleSize = 8;
            final Bitmap pre_bitmap = BitmapFactory.decodeFile(imgPath, options);
            Bitmap bitmap = Bitmap.createScaledBitmap(pre_bitmap, 330, 330, true);

            recipe_image_button.setImageBitmap(bitmap);
            // Get the Image's file name
            String fileNameSegments[] = imgPath.split("/");
            fileName = fileNameSegments[fileNameSegments.length - 1];
            // Put file name in Async Http Post Param which will used in Php web app
            image_params.put("filename", fileName);
        }

        recipe_image_button.setVisibility(View.GONE);
        btnCapturePicture.setVisibility(View.GONE);
        original_image = (ImageView) findViewById(R.id.old_image);

        keep_image = (CheckBox) findViewById(R.id.keepImageCheck);
        new DownloadImageTask((ImageView) findViewById(R.id.old_image))
                .execute(recipe_image_url);

        keep_image.setOnClickListener(new View.OnClickListener() {  // checkbox listener
            public void onClick(View v) {
                // Perform action on clicks, depending on whether it is checked
                if (((CheckBox) v).isChecked()) {
                    pressed_keep_image=true;
                    recipe_image_button.setVisibility(View.GONE);
                    btnCapturePicture.setVisibility(View.GONE);
                    original_image.setVisibility(View.VISIBLE);
                } else if (((CheckBox) v).isChecked() == false) {
                    pressed_keep_image=false;
                    recipe_image_button.setVisibility(View.VISIBLE);
                    btnCapturePicture.setVisibility(View.VISIBLE);
                    original_image.setVisibility(View.GONE);
                }
            }
        });

    }
    /**
     * Checking device has camera hardware or not
     * */
    private boolean isDeviceSupportCamera() {
        if (getApplicationContext().getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_CAMERA)) {
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            return false;
        }

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
                if(pressed_keep_image){
                    uploaded_image_url=recipe_image_url;
                    new CreateRecipe().execute();
                }
                else{
                    uploadImageAndCreateRecipe(recipe_image_button);
                }
                break;
            case R.id.recipe_image_button:
                loadImagefromGallery(recipe_image_button);
                break;
            case R.id.btnCapturePicture:
                captureImage();
                break;
            case R.id.keepImageCheck:
                pressed_keep_image = keep_image.isChecked();
                if(pressed_keep_image)
                {
                    recipe_image_button.setVisibility(View.GONE);
                    btnCapturePicture.setVisibility(View.GONE);
                }
                else{
                    recipe_image_button.setVisibility(View.VISIBLE);
                    btnCapturePicture.setVisibility(View.VISIBLE);
                }
                break;
            default:
                break;
        }
    }

    class CreateRecipe extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread Show Progress Dialog
         */

        Boolean bool_success = false;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(ActivitySingleRecipeEdit.this);
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
                params.add(new BasicNameValuePair("image_url", uploaded_image_url));

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
                    bool_success = true;
                    return json.getString(TAG_MESSAGE);
                } else {
                    Log.d("Creation Failure!", json.getString(TAG_MESSAGE));
                    bool_success = false;
                    return json.getString(TAG_MESSAGE);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;

        }

        /**
         * After completing background task Dismiss the progress dialog
         * *
         */
        protected void onPostExecute(String file_url) {
            // dismiss the dialog once product deleted
            pDialog.dismiss();
            if (file_url != null) {
                Toast.makeText(ActivitySingleRecipeEdit.this, file_url,
                        Toast.LENGTH_LONG).show();
                if (bool_success) {
                    Intent i = new Intent(ActivitySingleRecipeEdit.this, ActivityCookbook.class);
                    finish();
                    startActivity(i);
                }
            }

        }

    }


    public void loadImagefromGallery(View view) {
        // Create intent to Open Image applications like Gallery, Google Photos
        Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        // Start the Intent
        startActivityForResult(galleryIntent, RESULT_LOAD_IMG);
    }
    // When Image is selected from Gallery
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            // When an Image is picked
            if (requestCode == RESULT_LOAD_IMG && resultCode == RESULT_OK
                    && null != data)
            {
                // Get the Image from data

                Uri selectedImage = data.getData();
                String[] filePathColumn = { MediaStore.Images.Media.DATA };

                // Get the cursor
                Cursor cursor = getContentResolver().query(selectedImage,
                        filePathColumn, null, null, null);
                // Move to first row
                cursor.moveToFirst();

                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                imgPath = cursor.getString(columnIndex);
                cursor.close();

                ImageButton imgView = (ImageButton) findViewById(R.id.recipe_image_button);
                // Set the Image in ImageView
                // bimatp factory
                BitmapFactory.Options options = new BitmapFactory.Options();

                // down sizing image as it throws OutOfMemory Exception for larger
                // images
                options.inSampleSize = 8;
                final Bitmap bitmap = BitmapFactory.decodeFile(imgPath, options);

                imgView.setImageBitmap(bitmap);
                // Get the Image's file name
                String fileNameSegments[] = imgPath.split("/");
                fileName = fileNameSegments[fileNameSegments.length - 1];
                // Put file name in Async Http Post Param which will used in Php web app
                image_params.put("filename", fileName);

            }
        } catch (Exception e) {
            Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG)
                    .show();
        }

        //for image form camera
        // if the result is capturing Image
        if (requestCode == CAMERA_CAPTURE_IMAGE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                camera_bool=true;
                // successfully captured the image
                // launching upload activity
                //TODO remove it
                //launchUploadActivity(true);
                imgPath=fileUri.getPath();
                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ActivitySingleRecipeEdit.this);
                SharedPreferences.Editor edit = sp.edit();
                edit.putBoolean("camera_bool",camera_bool);
                edit.putString("imgPath",imgPath);
                edit.commit();
                ImageButton imgView = (ImageButton) findViewById(R.id.recipe_image_button);
                // Set the Image in ImageView
                // bimatp factory
                BitmapFactory.Options options = new BitmapFactory.Options();

                // down sizing image as it throws OutOfMemory Exception for larger
                // images
                options.inSampleSize = 8;
                final Bitmap bitmap = BitmapFactory.decodeFile(imgPath, options);

                imgView.setImageBitmap(bitmap);
                // Get the Image's file name
                String fileNameSegments[] = imgPath.split("/");
                fileName = fileNameSegments[fileNameSegments.length - 1];
                // Put file name in Async Http Post Param which will used in Php web app
                image_params.put("filename", fileName);
              /*  SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ActivityCreateRecipe.this);
                SharedPreferences.Editor edit = sp.edit();
                edit.putBoolean("camera_bool",true);
                edit.commit();*/
            } else if (resultCode == RESULT_CANCELED) {

                // user cancelled Image capture
                Toast.makeText(getApplicationContext(),
                        "User cancelled image capture", Toast.LENGTH_SHORT)
                        .show();

            } else {
                // failed to capture image
                Toast.makeText(getApplicationContext(),
                        "Sorry! Failed to capture image", Toast.LENGTH_SHORT)
                        .show();
            }

        } else if (requestCode == CAMERA_CAPTURE_VIDEO_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {

                // video successfully recorded
                // launching upload activity
                launchUploadActivity(false);

            } else if (resultCode == RESULT_CANCELED) {

                // user cancelled recording
                Toast.makeText(getApplicationContext(),
                        "User cancelled video recording", Toast.LENGTH_SHORT)
                        .show();

            } else {
                // failed to record video
                Toast.makeText(getApplicationContext(),
                        "Sorry! Failed to record video", Toast.LENGTH_SHORT)
                        .show();
            }
        }


    }

    // When Upload button is clicked
    public void uploadImageAndCreateRecipe(View v) {
        // When Image is selected from Gallery
        if (imgPath != null && !imgPath.isEmpty()) {
            prgDialog.setMessage("Converting Image to Binary Data");
            prgDialog.show();
            // Convert image to String using Base64
            encodeImagetoString();
            // When Image is not selected from Gallery
        } else {
            Toast.makeText(
                    getApplicationContext(),
                    "You must select image from gallery before you try to upload",
                    Toast.LENGTH_LONG).show();
        }
    }

    // AsyncTask - To convert Image to String
    public void encodeImagetoString() {
        new AsyncTask<Void, Void, String>() {

            protected void onPreExecute() {

            };

            @Override
            protected String doInBackground(Void... params) {
                BitmapFactory.Options options = null;
                options = new BitmapFactory.Options();
                options.inSampleSize = 8;
                bitmap = BitmapFactory.decodeFile(imgPath,
                        options);
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                // Must compress the Image to reduce image size to make upload easy
                bitmap.compress(Bitmap.CompressFormat.PNG, 50, stream);
                byte[] byte_arr = stream.toByteArray();
                // Encode Image to String
                encodedString = Base64.encodeToString(byte_arr, 0);
                return "";
            }

            @Override
            protected void onPostExecute(String msg) {
                prgDialog.setMessage("Calling Upload");
                // Put converted Image string into Async Http Post param
                image_params.put("image", encodedString);
                // Trigger Image upload
                triggerImageUpload();
            }
        }.execute(null, null, null);
    }

    void triggerImageUpload(){
        makeHTTPCall();

    }



    // Make Http call to upload Image to Php server
    public void makeHTTPCall() {
        prgDialog.setMessage("Invoking Php");
        AsyncHttpClient client = new AsyncHttpClient();
        // Don't forget to change the IP address to your LAN address. Port no as well.
        client.post(UPLOAD_IMAGE_URL,
                image_params, new AsyncHttpResponseHandler() {
                    // When the response returned by REST has Http
                    // response code '200'
                    @Override
                    public void onSuccess(String response) {
                        // Hide Progress Dialog
                        prgDialog.hide();
                        Toast.makeText(getApplicationContext(), response,
                                Toast.LENGTH_LONG).show();
                        uploaded_image_url=response;
                        new CreateRecipe().execute();

                    }

                    // When the response returned by REST has Http
                    // response code other than '200' such as '404',
                    // '500' or '403' etc
                    @Override
                    public void onFailure(int statusCode, Throwable error,
                                          String content) {
                        // Hide Progress Dialog
                        prgDialog.hide();
                        // When Http response code is '404'
                        if (statusCode == 404) {
                            Toast.makeText(getApplicationContext(),
                                    "Requested resource not found",
                                    Toast.LENGTH_LONG).show();
                        }
                        // When Http response code is '500'
                        else if (statusCode == 500) {
                            Toast.makeText(getApplicationContext(),
                                    "Something went wrong at server end",
                                    Toast.LENGTH_LONG).show();
                        }
                        // When Http response code other than 404, 500
                        else {
                            Toast.makeText(
                                    getApplicationContext(),
                                    "Error Occured \n Most Common Error: \n1. Device not connected to Internet\n2. Web App is not deployed in App server\n3. App server is not running\n HTTP Status code : "
                                            + statusCode, Toast.LENGTH_LONG)
                                    .show();
                        }
                    }
                });
    }

    /**
     * Launching camera app to capture image
     */
    private void captureImage() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);

        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);

        // start the image capture Intent
        startActivityForResult(intent, CAMERA_CAPTURE_IMAGE_REQUEST_CODE);
    }
    /**
     * Here we store the file url as it will be null after returning from camera
     * app
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // save file url in bundle as it will be null on screen orientation
        // changes
        outState.putParcelable("file_uri", fileUri);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        // get the file url
        fileUri = savedInstanceState.getParcelable("file_uri");
    }


    private void launchUploadActivity(boolean isImage){
        Intent i = new Intent(ActivitySingleRecipeEdit.this, UploadActivity.class);
        i.putExtra("filePath", fileUri.getPath());
        i.putExtra("isImage", isImage);
        startActivity(i);
    }

/**
 * ------------ Helper Methods ----------------------
 * */

    /**
     * Creating file uri to store image/video
     */
    public Uri getOutputMediaFileUri(int type) {
        return Uri.fromFile(getOutputMediaFile(type));
    }

    /**
     * returning image / video
     */
    private static File getOutputMediaFile(int type) {

        // External sdcard location
        File mediaStorageDir = new File(
                Environment
                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                Config.IMAGE_DIRECTORY_NAME);

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d(TAG, "Oops! Failed create "
                        + Config.IMAGE_DIRECTORY_NAME + " directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
                Locale.getDefault()).format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator
                    + "IMG_" + timeStamp + ".jpg");
        } else if (type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator
                    + "VID_" + timeStamp + ".mp4");
        } else {
            return null;
        }

        return mediaFile;
    }















    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Dismiss the progress bar when application is closed
        if (prgDialog != null) {
            prgDialog.dismiss();
        }
    }
}