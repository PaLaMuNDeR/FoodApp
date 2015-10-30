package polimi.dima.foodapp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.apache.http.NameValuePair;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Marti on 17/06/2015.
 */
public class ListViewIngredientsAdapter extends ArrayAdapter<ListViewItem> {
    private int ingredient_number = 0;
    public ListViewIngredientsAdapter(Context context, List<ListViewItem> items) {
        super(context, R.layout.listview_ingredient_item, items);
    }
    /**
     * The view holder design pattern prevents using findViewById()
     * repeatedly in the getView() method of the adapter.
     *
     */
    private static class ViewHolder {
        TextView title;
        TextView description;
        ImageView moreItem;

    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder viewHolder;
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getContext());
        final SharedPreferences.Editor edit = sp.edit();
        if(convertView == null) {
            // inflate the GridView item layout
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.listview_ingredient_item, parent, false);


            // initialize the view holder
            viewHolder = new ViewHolder();
            viewHolder.title = (TextView) convertView.findViewById(R.id.ingredient_item);
            viewHolder.description = (TextView) convertView.findViewById(R.id.ingredient_amount);
            viewHolder.moreItem = (ImageView) convertView.findViewById(R.id.more_item);
            convertView.setTag(viewHolder);
        } else {
            // recycle the already inflated view
            viewHolder = (ViewHolder) convertView.getTag();
        }

        // update the item view
        final ListViewItem item = getItem(position);
        viewHolder.title.setText(item.title);
        viewHolder.description.setText(item.description);
        viewHolder.moreItem.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                        builder
                                .setTitle(getContext().getResources().getString(R.string.edit))
                                .setMessage(getContext().getResources().getString(R.string.edit_question))
                                .setIcon(R.drawable.ic_launcher)
                                .setNegativeButton(R.string.edit, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        edit.putString("ingredient_title", item.title);
                                        edit.putInt("ingredient_number", getPosition(item));
                                        edit.putBoolean("edit_ingredient", true);
                                        edit.commit();
                                    }
                                })
                                .setNeutralButton(R.string.delete, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        edit.putString("ingredient_title", item.title);
                                        edit.putBoolean("delete_ingredient", true);
                                        edit.commit();

                                    }
                                })
                                .setPositiveButton(R.string.no, null)
                                        //Do nothing on no
                                .show();
                        //   listItems.remove(index.intValue());
                        //  notifyDataSetChanged();
                    }
                });
        return convertView;
    }

}