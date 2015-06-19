package polimi.dima.foodapp;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Marti on 19/06/2015.
 */

public class SingleRecipeActivity extends Activity implements View.OnClickListener{
    private String current_user = "current_user_username";
    private String current_name = "current_user_name";
    // Progress Dialog
    private ProgressDialog sDialog;

    // Drawer Layout
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private CharSequence mDrawerTitle;
    private CharSequence mTitle;
    private String[] mDrawerTitles;

    Button
            //btnAllPoi,
            //btnLogout,
            btnAdd;

    private static String p_id = "";

    JSONParser jsonParser = new JSONParser();
    private static final String ADD_REC_URL = "http://expox-milano.com/foodapp/add_rec.php";

    // JSON element ids from repsonse of php script:
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_MESSAGE = "message";
    private static String TAG_IMAGES = "images";
    public View mFragment;

    public SingleRecipeActivity(){

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.single_recipe);
        SharedPreferences sp = PreferenceManager
                .getDefaultSharedPreferences(SingleRecipeActivity.this);
        String post_username = sp.getString("username", "");
        String post_name = sp.getString("name", "");
        Log.d("name", "Loading name " + post_name);
        current_name = post_name;
        Log.d("name", "current_name" + current_name);

        current_user = post_username;
        btnAdd = (Button) findViewById(R.id.btn_add);

        btnAdd.setOnClickListener(this);
        // parsing the data from the shared preferences
        p_id = sp.getString("poi_id", "");

        String r_name_value = sp.getString("recipe_name", "");
        TextView name = (TextView) findViewById(R.id.r_name);
        TextView name2 = (TextView) findViewById(R.id.recipe_name);
        name.setText(r_name_value);
        name2.setText(r_name_value);

        String ingredients_value = sp.getString("ingredients", "");
        TextView ingredients = (TextView) findViewById(R.id.ingredients);
        ingredients.setText(ingredients_value);

        String instructions_value = sp.getString("instructions", "");
        TextView instructions = (TextView) findViewById(R.id.instructions);
        instructions.setText(instructions_value);

        String image_url = sp.getString("image_url", "");

      /*  Drawable draw_temp = null;
        try {
            draw_temp = drawableFromUrl(image_url);
        } catch (Exception e) {
            e.printStackTrace();
        }
*/
//                Drawable draw_temp= new DownloadImageTask(image_url);


        // annndddd, our JSON data is up to date same with our array
        // list
        new DownloadImageTask((ImageView) findViewById(R.id.imageViewSingleRecipe))
                .execute(image_url);
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
//		case R.id.btn_logout: {
//			SharedPreferences sp = PreferenceManager
//					.getDefaultSharedPreferences(ActivityOneFromAllPoi.this);
//			// saves the logout user data
//			current_user = "";
//
//			Editor edit = sp.edit();
//			edit.putString("username", current_user);
//			edit.putString("name", current_name);
//			edit.commit();
//			Log.d("Log out - current_user", current_user);
//			Log.d("Log out - current name", current_name);
//			ClearPreferences();
//			Intent i = new Intent(v.getContext(), ActivityLogin.class);
//			startActivity(i);
//			finish();
//		}
//			break;
//		case R.id.btn_poi_2:
//			Intent p = new Intent(this, ActivityAllPoi.class);
//			startActivity(p);
//			finish();
//			break;
            case R.id.btn_add:
              //  new AddRecommendation().execute();

                break;
            case R.id.imageView1:
                Toast.makeText(SingleRecipeActivity.this, TAG_IMAGES,
                        Toast.LENGTH_LONG).show();
                break;
            default:
                break;
        }
    }
/*
    class AddRecommendation extends AsyncTask<String, String, String> {


        boolean failure = false;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            sDialog = new ProgressDialog(SingleRecipeActivity.this);
            sDialog.setMessage("Adding it to your recommendations...");
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
                params.add(new BasicNameValuePair("poi_id", p_id));

                Log.d("request!", "starting");
                // getting product details by making HTTP request
                JSONObject json = jsonParser.makeHttpRequest(ADD_REC_URL,
                        "POST", params);

                // check your log for json response
                Log.d("Login attempt", json.toString());

                // json success tag
                success = json.getInt(TAG_SUCCESS);
                if (success == 1) {
                    Log.d("Synchronization Successful!", json.toString());

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
            Log.d("current_user", "current_user" + current_user);
            Log.d("current_user", "current_user" + current_user);
            if (file_url != null && current_user != "") {
                Toast.makeText(ActivityOneFromAllPoi.this, file_url,
                        Toast.LENGTH_SHORT).show();
            }
            Log.d("Starting new activity", "ActivityOnePoi");
            Intent i = new Intent(ActivityOneFromAllPoi.this,
                    ActivityLogin.class);
            finish();
            startActivity(i);

        }

    }

    public void ClearPreferences() {
        SharedPreferences sp = PreferenceManager
                .getDefaultSharedPreferences(ActivityOneFromAllPoi.this);
        Editor edit = sp.edit();
        edit.putString("poi_id", "poi_id");
        edit.putString("poi_name", "poi_name");
        edit.putString("short_description", "short_description");
        edit.putString("address", "address");
        edit.putString("visibility", "visibility");
        edit.putString("region", "region");
        edit.putString("web_page", "web_page");
        edit.putString("vote", "vote");
        edit.commit();

    }
*/
}