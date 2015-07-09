package polimi.dima.foodapp;

import android.graphics.drawable.Drawable;

/**
 * Created by Marti on 17/06/2015.
 */
public class ListViewItem {
    public final Drawable icon;       // the drawable for the ListView item ImageView
    public final String title;        // the text for the ListView item title
    public final String description;  // the text for the ListView item description
    public final String creator_name;
    public final Drawable creator_photo;
    public final String id;

    public ListViewItem(Drawable icon, String title,
                        String description, String creator_name, Drawable creator_photo) {
        this.icon = icon;
        this.title = title;
        this.description = description;
        this.creator_name = creator_name;
        this.creator_photo = creator_photo;
        this.id=null;
    }
    public ListViewItem(String id, String creator_name, Drawable creator_photo){
        this.creator_name = creator_name;
        this.creator_photo = creator_photo;
        this.id=id;
        this.icon=null;
        this.title=null;
        this.description=null;
    }

}