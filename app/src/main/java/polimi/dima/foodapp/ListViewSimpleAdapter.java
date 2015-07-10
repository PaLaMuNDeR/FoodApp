package polimi.dima.foodapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Marti on 17/06/2015.
 */
public class ListViewSimpleAdapter extends ArrayAdapter<ListViewItem> {

    public ListViewSimpleAdapter(Context context, List<ListViewItem> items) {
        super(context, R.layout.listviewsimple_item, items);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;

        if(convertView == null) {
            // inflate the GridView item layout
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.listviewsimple_item, parent, false);

            // initialize the view holder
            viewHolder = new ViewHolder();
            viewHolder.creatorName = (TextView) convertView.findViewById(R.id.creatorName);
            viewHolder.creatorImage = (ImageView) convertView.findViewById(R.id.creatorImage);
            convertView.setTag(viewHolder);
        } else {
            // recycle the already inflated view
            viewHolder = (ViewHolder) convertView.getTag();
        }

        // update the item view
        ListViewItem item = getItem(position);
        viewHolder.creatorName.setText(item.creator_name);
        viewHolder.creatorImage.setImageDrawable(item.creator_photo);

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
     *
     */
    private static class ViewHolder {

        TextView creatorName;
        ImageView creatorImage;
    }
}