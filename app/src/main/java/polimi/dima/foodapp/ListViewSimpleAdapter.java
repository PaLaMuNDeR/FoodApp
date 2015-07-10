package polimi.dima.foodapp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Marti on 17/06/2015.
 */
public class ListViewSimpleAdapter extends ArrayAdapter<ListViewItem> {

    private ArrayList<HashMap<String, String>> mPoiList;
    JSONParser jsonParser = new JSONParser();
    JSONObject json;
    private static final String UNFOLLOW_URL = "http://expox-milano.com/foodapp/del_follow.php";
    private JSONArray mPois = null;
    private static final String TAG_POSTS = "posts";
    private static final String TAG_SUCCESS = "success";
    private int id_click = 0;
    private static final String TAG_CREATOR_ID = "user_id";
    private static final String TAG_CREATOR_USERNAME = "creator_username";
    private static final String TAG_CREATOR_PHOTO = "creator_photo";



    public ListViewSimpleAdapter(Context context, List<ListViewItem> items) {
        super(context, R.layout.listviewsimple_item, items);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder viewHolder;

        if (convertView == null) {
            // inflate the GridView item layout
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.listviewsimple_item, parent, false);

            // initialize the view holder
            viewHolder = new ViewHolder();
            viewHolder.creatorName = (TextView) convertView.findViewById(R.id.creatorName);
            viewHolder.creatorImage = (ImageView) convertView.findViewById(R.id.creatorImage);
            viewHolder.moreItem = (ImageView) convertView.findViewById(R.id.more_item);
            convertView.setTag(viewHolder);
        } else {
            // recycle the already inflated view
            viewHolder = (ViewHolder) convertView.getTag();
        }

        // update the item view
        final ListViewItem item = getItem(position);
        viewHolder.creatorName.setText(item.creator_name);
        viewHolder.creatorImage.setImageDrawable(item.creator_photo);
        viewHolder.moreItem.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //Integer index = (Integer) convertView.getTag();

                        Toast.makeText(getContext(), "More item 1", Toast.LENGTH_LONG).show();
                        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getContext());
                        SharedPreferences.Editor edit = sp.edit();
                        edit.putBoolean("Del", true);
                        edit.commit();

                        // Intent i = new Intent(ActivityChiefs.this, ActivityChiefs.class);
                        // startActivity(i);
                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                        builder
                                .setTitle(getContext().getResources().getString(R.string.unfollow))
                                .setMessage(getContext().getResources().getString(R.string.unfollow_question))
                                .setIcon(R.drawable.ic_launcher)
                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        // Extract all the assets
                                        Toast.makeText(getContext(), "Return number: "+getPosition(item), Toast.LENGTH_SHORT).show();
                                        id_click = getPosition(item);
                                       new Unfollow().execute();

                                    }
                                })
                                .setNegativeButton("No", null)
                                        //Do nothing on no
                                .show();
                        //   listItems.remove(index.intValue());
                        //  notifyDataSetChanged();
                    }
                });
        return convertView;
    }
    /*
@Override
public View getView(int position, View convertView, ViewGroup parent)
{
    ViewHolder viewHolder;


    View row = null;
    LayoutInflater inflater = LayoutInflater.from(getContext());
    convertView = inflater.inflate(R.layout.listviewsimple_item, parent, false);
    // initialize the view holder
    viewHolder = new ViewHolder();
    viewHolder.creatorName = (TextView) convertView.findViewById(R.id.creatorName);
    viewHolder.creatorImage = (ImageView) convertView.findViewById(R.id.creatorImage);
    convertView.setTag(viewHolder);

    ImageView image= (ImageView) row.findViewById(R.id.your_image);
    image.setTag(position);

    image.setOnClickListener(
            new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Integer index = (Integer) view.getTag();
                    listItems.remove(index.intValue());
                    notifyDataSetChanged();
                }
            }
    );
    */

    /**
     * The view holder design pattern prevents using findViewById()
     * repeatedly in the getView() method of the adapter.
     */
    private static class ViewHolder {
        ImageView moreItem;
        TextView creatorName;
        ImageView creatorImage;
    }








    public class Unfollow extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // pDialog = new ProgressDialog(ActivityRecommendations.this);
            // pDialog.setMessage("Loading your place..");
            // pDialog.setIndeterminate(false);
            // pDialog.setCancelable(true);
            // pDialog.show();
        }

        @Override
        protected Boolean doInBackground(Void... arg0) {
            updateJSONdataForTheChief();


            // Instantiate the arraylist to contain all the JSON data.
            // we are going to use a bunch of key-value pairs, referring
            // to the json element name, and the content, for example,
            // message it the tag, and "I'm awesome" as the content..

            mPoiList = new ArrayList<HashMap<String, String>>();

            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getContext());
            String username = sp.getString("username", "");
            String creator_id = sp.getString("creator_id", "");

            List<NameValuePair> params = new ArrayList<NameValuePair>();

            Log.d("request!", "starting");
            // getting product details by making HTTP request


            params.add(new BasicNameValuePair("username", username));
            params.add(new BasicNameValuePair("creator_id", creator_id));

            json = jsonParser.makeHttpRequest(UNFOLLOW_URL,
                    "POST", params);

            //json = jParser.getJSONFromUrl(READ_RECIPES_URL);

            // when parsing JSON stuff, we should probably
            // try to catch any exceptions:
            try {

                // mPois will tell us how many "posts" are
                // available
                mPois = json.getJSONArray(TAG_POSTS);

                // looping through all posts according to the json object
                // returned
                for (int i = 0; i < mPois.length(); i++) {
                    JSONObject c = mPois.getJSONObject(i);

                    // gets the content of each tag
                    String success = c.getString(TAG_SUCCESS);

                    // creating new HashMap
                    HashMap<String, String> map = new HashMap<String, String>();

                    // map.put(TAG_POI_ID, poi_id);
                    map.put(TAG_SUCCESS, success);

                    // adding HashList to ArrayList
                    mPoiList.add(map);


                }
            } catch (JSONException e) {
                e.printStackTrace();

            }
            return null;

        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            // pDialog.dismiss();
            Intent i = new Intent(getContext(),
                    ActivityChiefs.class);
            getContext().startActivity(i);
        }
    }
    public void updateJSONdataForTheChief() {

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

            SharedPreferences sp = PreferenceManager
                    .getDefaultSharedPreferences(getContext());
            SharedPreferences.Editor edit = sp.edit();
            String json_string =sp.getString("json_users", "");

            json = new JSONObject( json_string );

            mPois = json.getJSONArray(TAG_POSTS);

            // looping through all posts according to the json object
            // returned

            JSONObject c = mPois.getJSONObject(id_click);

            // gets the content of each tag
            String creator_id = c.getString(TAG_CREATOR_ID);
            String creator_username = c.getString(TAG_CREATOR_USERNAME);
            String creator_photo = c.getString(TAG_CREATOR_PHOTO);

            // // creating new HashMap
            HashMap<String, String> map = new HashMap<String, String>();
            map.put(TAG_CREATOR_ID, creator_id);
            map.put(TAG_CREATOR_USERNAME, creator_username);
            map.put(TAG_CREATOR_PHOTO, creator_photo);

            // adding HashList to ArrayList
            mPoiList.add(map);

            // annndddd, our JSON data is up to date same with our array
            // list
            // now we save the strings for the poi in the sharedresources
            // to pass it to another activity

            edit.putString("creator_id", creator_id);
            edit.putString("creator_username", creator_username);
            edit.putString("creator_photo", creator_photo);
            edit.putBoolean("one_user_recipes", true);
            edit.commit();

        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("Recipe name", "");

        }

    }
}