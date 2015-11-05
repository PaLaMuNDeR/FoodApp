package polimi.dima.foodapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ListFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.NumberPicker;
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

//import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;

/**
 * Created by Marti on 17/06/2015.
 */
public class FragmentIngredients extends DialogFragment {

    public List<ListViewItem> getmItems() {
        return mItems;
    }

    public void setmItems(List<ListViewItem> mItems) {
        this.mItems = mItems;
    }

    public List<ListViewItem> mItems = new ArrayList<ListViewItem>();        // ListView items list
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
    private static final String TAG_AMOUNT = "amount";
    private static final String TAG_RECIPE_IMAGE_URL = "recipe_image_url";
    private static final String TAG_CREATOR_ID = "creator_id";
    private static final String TAG_CREATOR_USERNAME = "creator_username";
    private static final String TAG_CREATOR_PHOTO = "creator_photo";
    private static final String TAG_RECIPE_ID = "recipe_id";
    private static final String TAG_LIKES = "likes";
    private static final String TAG_FOLLOWED = "followed";
    private static final String TAG_L_RECIPE_ID = "l_recipe_id";
    private static final String TAG_F_USER_ID = "f_user_id";

    // An array of all of our recipes
    private JSONArray mPois = null;
    //page_shown serves for iterator for pages shown (*10 items per page)
    public Integer page_shown = 1;
    public Integer items_per_page = 10;
    public Integer mPoisLength = 0;
    private JSONArray mLikes = null;
    private JSONArray mFollowed = null;
    // manages all of our pois in a list.
    private ArrayList<HashMap<String, String>> recipesToLoad = new ArrayList<HashMap<String, String>>();
    private ArrayList<HashMap<String, Drawable>> imagesOfRecipes;
    private ArrayList<HashMap<String, String>> mLikeList;
    private ArrayList<HashMap<String, String>> mFollowedList;
    // Checks whether there is internet

    public Boolean flag_loading = false;
    public Boolean flag_load_more = true;
    public Boolean flag_need_refresh = true;
    ListViewAdapter list_adapt = null;
    EditText ingredients_edit_text = null;
    NumberPicker numberPicker_1 = null;
    NumberPicker numberPicker_2 = null;
    NumberPicker measurePicker = null;
    ImageButton btn_add = null;
    ImageButton btn_close = null;
    ListView ingredientsList = null;
    String[] number_picker_2_values = null;
    String[] measurings = null;

    boolean edit_flag = false;
    boolean delete_flag = false;
    Integer edit_number = 0;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View rootView = inflater.inflate(R.layout.fragment_ingredients, container,
                false);
//        getDialog().setTitle(getResources().getString(R.string.add_ingredients));
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        ingredients_edit_text = (EditText) rootView.findViewById(R.id.ingredient_name);

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(rootView.getContext());
        final SharedPreferences.Editor edit = sp.edit();
        edit_flag = sp.getBoolean("edit_ingredient", false);
        delete_flag = sp.getBoolean("delete_ingredient", false);
        edit_number = sp.getInt("ingredient_number", 0);

        int ingredient_number = sp.getInt("ingredient_number", 0);


        numberPicker_1 = (NumberPicker) rootView.findViewById(R.id.numberPicker1);
        numberPicker_2 = (NumberPicker) rootView.findViewById(R.id.numberPicker2);
        measurePicker = (NumberPicker) rootView.findViewById(R.id.numberPicker3);
        ingredientsList = (ListView) rootView.findViewById(R.id.ingredientsListViewEditable);
        final ListViewIngredientsAdapter listViewIngredientsAdapter = new ListViewIngredientsAdapter(rootView.getContext(), mItems);

        ListViewItem edit_item = null;


        numberPicker_1.setMinValue(0);
        numberPicker_1.setMaxValue(100);
        numberPicker_2.setMinValue(0);
        numberPicker_2.setMaxValue(3);

        final String[] number_picker_2_values = {".00", ".25", ".50", ".75"};
        numberPicker_2.setDisplayedValues(number_picker_2_values);

        final String[] measurings = getResources().getStringArray(R.array.measurings);
        measurePicker.setMinValue(0);
        measurePicker.setMaxValue(measurings.length - 1);
        measurePicker.setDisplayedValues(measurings);

        btn_add = (ImageButton) rootView.findViewById(R.id.btn_add);
        btn_add.setOnClickListener((new View.OnClickListener() {  // checkbox listener
            public void onClick(View v) {
                // Perform action on clicks, depending on whether it is checked

                String ingredient = ingredients_edit_text.getText().toString();
                String amount;
                if (numberPicker_2.getValue() != 0) {
                    amount = "" + numberPicker_1.getValue() + number_picker_2_values[numberPicker_2.getValue()] + " " + measurings[measurePicker.getValue()];
                    if (numberPicker_1.getValue() != 1 && measurePicker.getValue() == 0) {
                        amount = "" + numberPicker_1.getValue() + number_picker_2_values[numberPicker_2.getValue()] + " " + getResources().getString(R.string.pieces);
                    }
                    if (numberPicker_1.getValue() != 1 && measurePicker.getValue() == 1) {
                        amount = "" + numberPicker_1.getValue() + number_picker_2_values[numberPicker_2.getValue()] + " " + getResources().getString(R.string.cups);
                    }
                } else {
                    amount = "" + numberPicker_1.getValue() + " " + measurings[measurePicker.getValue()];
                    if (numberPicker_1.getValue() != 1 && measurePicker.getValue() == 0) {
                        amount = "" + numberPicker_1.getValue() + number_picker_2_values[numberPicker_2.getValue()] + " " + getResources().getString(R.string.pieces);
                    }
                    if (numberPicker_1.getValue() != 1 && measurePicker.getValue() == 1) {
                        amount = "" + numberPicker_1.getValue() + number_picker_2_values[numberPicker_2.getValue()] + " " + getResources().getString(R.string.cups);
                    }
                }

                HashMap<String, String> map = new HashMap<String, String>();

                map.put(TAG_INGREDIENTS, ingredient);
                map.put(TAG_AMOUNT, amount);

                mItems.add(new ListViewItem(ingredient, amount));

                ingredientsList.setAdapter(listViewIngredientsAdapter);
//                new ListViewIngredientsAdapter(rootView.getContext(), mItems));

//                ingredientsList.setAdapter(new ListViewSimpleAdapter(rootView.getContext(), mItems));
                ingredients_edit_text.setText("");
                numberPicker_1.setValue(0);
                numberPicker_2.setValue(0);
                measurePicker.setValue(0);
            }
        }

        ));

        ingredientsList = (ListView) rootView.findViewById(R.id.ingredientsListViewEditable);
        ingredientsList.setDivider(null);
        ingredientsList.setAdapter(listViewIngredientsAdapter);
        ingredientsList.setOnItemClickListener((new AdapterView.OnItemClickListener() {  // checkbox listener
            @Override
            public void onItemClick(AdapterView<?> adapter, View v, int position,
                                    long arg3) {
                final Integer item_no = position;
                AlertDialog.Builder builder = new AlertDialog.Builder(rootView.getContext());
                builder
                        .setTitle(rootView.getContext().getResources().getString(R.string.edit))
                        .setMessage(rootView.getContext().getResources().getString(R.string.edit_question))
                        .setIcon(R.drawable.ic_launcher)
                        .setNegativeButton(R.string.edit, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                                ListViewItem edit_item = mItems.get(item_no);
                                ingredients_edit_text.setText(edit_item.title);

                                mItems.remove(edit_item);
                                ingredientsList.setAdapter(listViewIngredientsAdapter);
                            }
                        })
                        .setNeutralButton(R.string.delete, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                ListViewItem edit_item = mItems.get(item_no);
                                mItems.remove(edit_item);
                                listViewIngredientsAdapter.notifyDataSetChanged();
                            }
                        })
                        .setPositiveButton(R.string.no, null)
                                //Do nothing on no
                        .show();
            }
        }
        ));
        if (edit_flag && mItems != null && mItems.size() > 0) {
            Integer item_no = sp.getInt("ingredient_number", 0);
            edit_item = mItems.get(item_no);
            ingredients_edit_text.setText(edit_item.title);
            mItems.remove(edit_item);

            ingredientsList.setAdapter(listViewIngredientsAdapter);
            edit.putBoolean("edit_ingredient", false);
            edit.commit();
        }

        btn_close = (ImageButton) rootView.findViewById(R.id.btn_close);
        btn_close.setOnClickListener((new View.OnClickListener() {  // checkbox listener
            public void onClick(View v) {
                getDialog().dismiss();
            }
        }
        ));
        return rootView;
    }

    public interface editItemsDialogListener {
        void onFinishEditDIalog(String input);
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();

        LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(
                new Intent("SOME_ACTION"));
//        for (int i = 0; i < mItems.size(); i++) {
//            mItems.remove(i);
//        }
//        SharedPreferences sp = PreferenceManager
//                .getDefaultSharedPreferences(getActivity());
//
//        SharedPreferences.Editor edit = sp.edit();
//        edit.putBoolean("liked_activity_bool", false);
//        edit.putBoolean("flag_load_more", true);
//        edit.putInt("page_shown", 1);
//        edit.commit();
    }

    public void updateData() {


        String ingredient = ingredients_edit_text.getText().toString();
        String amount = "" + numberPicker_1.getValue() + "" + +measurePicker.getValue();

        HashMap<String, String> map = new HashMap<String, String>();

        map.put(TAG_INGREDIENTS, ingredient);
        map.put(TAG_AMOUNT, amount);
        mItems.add(new ListViewItem(ingredient, amount));

    }

    public void updateList() {

        list_adapt = new ListViewAdapter(getActivity(), mItems);
//        setListAdapter(list_adapt);
//        final ListView lv = getListView();
//        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view,
//                                    int position, long id) {
//
//                Log.e("poi_name", "The position is '" + position
//                        + "' the id is '" + id + "'");
//
//                id_click = position;
//
//                updateJSONdataForOne();
//
//                Intent i = new Intent(getActivity(),
//                        ActivitySingleRecipeFromAll.class);
//                getActivity().finish();
//                startActivity(i);
//
//            }
//        });

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

            Boolean liked = false;
            Boolean followed = false;
            mLikes = json.getJSONArray(TAG_LIKES);
            for (int i = 0; i < mLikes.length(); i++) {
                JSONObject l = mLikes.getJSONObject(i);

                if (recipe_id.equals(l.getString(TAG_L_RECIPE_ID))) {
                    liked = true;
                }
            }
            mFollowed = json.getJSONArray(TAG_FOLLOWED);
            for (int i = 0; i < mFollowed.length(); i++) {
                JSONObject l = mFollowed.getJSONObject(i);

                if (creator_id.equals(l.getString(TAG_F_USER_ID))) {
                    followed = true;
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
            edit.putBoolean("followed", followed);
            edit.commit();

        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("Recipe name", "");

        }

    }

}


