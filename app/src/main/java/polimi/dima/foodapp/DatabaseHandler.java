package polimi.dima.foodapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Marti on 10/06/2015.
 */
public class DatabaseHandler extends SQLiteOpenHelper {
    Context mContext;

    // All Static variables
    // Database Version
    private static int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "foodManager";

    //Tables names
    private static final String TABLE_PROFILE = "profile";
    private static final String TABLE_VERSION = "version";

    // Profiles Table Columns names
    private static final String KEY_ID = "id";
    private static final String KEY_NAME = "name";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_PHOTO = "photo";
    private static final String KEY_COVER = "cover";
    private static final String KEY_GENDER = "gender";
    private static final String KEY_VERSION = "version";

    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static int getDATABASE_VERSION() {
        return DATABASE_VERSION;
    }

    public static void setDATABASE_VERSION(int DATABASE_VERSION) {
        DatabaseHandler.DATABASE_VERSION = DATABASE_VERSION;
    }


    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_PROFILE_TABLE = "CREATE TABLE " + TABLE_PROFILE + "(" +
                KEY_ID + " INTEGER PRIMARY KEY, " + KEY_NAME + " TEXT," +
                KEY_USERNAME + " TEXT," + KEY_PHOTO + " TEXT," + KEY_COVER + " TEXT,"
                + KEY_GENDER + " TEXT," + KEY_EMAIL + " TEXT" + ")";
        db.execSQL(CREATE_PROFILE_TABLE);
        String CREATE_VERSION_TABLE="CREATE TABLE " + TABLE_VERSION + "(" +
                KEY_ID + " INTEGER PRIMARY KEY," + KEY_VERSION + " INTEGER" +")";
        db.execSQL(CREATE_VERSION_TABLE);
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PROFILE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_VERSION);

//       setDATABASE_VERSION(newVersion);
        //      Log.d("Database", "DB Update. Version set to: " + newVersion);
        // Create tables again
        onCreate(db);
    }

    // Adding new profile
    public void addProfile(Profile profile) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_NAME, profile.getName()); // Profile Name
        values.put(KEY_USERNAME, profile.getUsername()); // Profile Description
        values.put(KEY_PHOTO, profile.getPhoto()); // Profile Image
        values.put(KEY_COVER, profile.getCover()); // Beacon's Mac
        values.put(KEY_GENDER, profile.getGender()); // TrackingData file for Metaio
        values.put(KEY_EMAIL, profile.getEmail()); // TrackingData file for Metaio

        // Inserting Row
        db.insert(TABLE_PROFILE, null, values);
        db.close(); // Closing database connection
    }

    // Adding new version
    public void addVersion(VersionVerifier versionVerifier) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_VERSION, versionVerifier.getVersion()); // Version


        // Inserting Row
        db.insert(TABLE_VERSION, null, values);
        db.close(); // Closing database connection
    }

    // Getting single profile
    public Profile getLastProfile() {
        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT " + KEY_ID + " , " + KEY_NAME + " , " +
                KEY_USERNAME + " , " + KEY_PHOTO + " , " + KEY_COVER + " , " + KEY_GENDER + " , " +
                KEY_EMAIL +
                " FROM " + TABLE_PROFILE + " ORDER BY " + KEY_ID +" DESC LIMIT 1";
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor != null)
            cursor.moveToFirst();

        Profile profile = new Profile(Integer.parseInt(cursor.getString(0)),
                cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4),
                cursor.getString(5), cursor.getString(6));
        db.close();
        // return profile
        return profile;
    }


    // Getting All Profiles
    public List<Profile> getAllProfiles() {
        List<Profile> profileList = new ArrayList<Profile>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_PROFILE;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Profile profile = new Profile();
                profile.setId(Integer.parseInt(cursor.getString(0)));
                profile.setName(cursor.getString(1));
                profile.setUsername(cursor.getString(2));
                profile.setPhoto(cursor.getString(3));
                profile.setCover(cursor.getString(4));
                profile.setGender(cursor.getString(5));
                profile.setEmail(cursor.getString(6));

                // Adding profile  to list
                profileList.add(profile);
            } while (cursor.moveToNext());
        }

        // return profile list
        return profileList;
    }

    // Getting Last version
    public VersionVerifier getLastVersion() {
        //SQLiteDatabase db = this.getReadableDatabase();
        SQLiteDatabase db = this.getWritableDatabase();

        String selectQuery = "SELECT * FROM " + TABLE_VERSION + " ORDER BY " + KEY_ID +" DESC LIMIT 1";
        Cursor cursor = db.rawQuery(selectQuery, null);

        VersionVerifier versionVerifier = new VersionVerifier();
        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                versionVerifier.setId(Integer.parseInt(cursor.getString(0)));
                versionVerifier.setVersion(Integer.parseInt(cursor.getString(1)));
            } while (cursor.moveToNext());
        }
        return versionVerifier;
    }



    // Getting profile Count
    public int getProfilesCount() {
        String countQuery = "SELECT  * FROM " + TABLE_PROFILE;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        cursor.close();

        // return count
        return cursor.getCount();
    }

    // Updating single profile
    public int updateProfile(Profile profile) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_NAME, profile.getName());
        values.put(KEY_USERNAME, profile.getUsername());
        values.put(KEY_PHOTO, profile.getPhoto());
        values.put(KEY_COVER, profile.getCover());
        values.put(KEY_GENDER, profile.getGender());
        values.put(KEY_EMAIL, profile.getEmail());

        // updating row
        return db.update(TABLE_PROFILE, values, KEY_ID + " = ?",
                new String[] { String.valueOf(profile.getId()) });
    }

    // Deleting single profile
    public void deleteProfile(Profile profile) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_PROFILE, KEY_ID + " = ?",
                new String[] { String.valueOf(profile.getId()) });
        db.close();
    }

    //New version
    public void flushOnNewVersion() {
        SQLiteDatabase db = this.getWritableDatabase();
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PROFILE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_VERSION);

//       setDATABASE_VERSION(newVersion);
        //      Log.d("Database", "DB Update. Version set to: " + newVersion);
        // Create tables again
        onCreate(db);
    }
}
