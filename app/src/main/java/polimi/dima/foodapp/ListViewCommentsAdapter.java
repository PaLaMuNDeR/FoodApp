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
public class ListViewCommentsAdapter extends ArrayAdapter<ListViewItem> {

    public ListViewCommentsAdapter(Context context, List<ListViewItem> items) {
        super(context, R.layout.listview_comment_item, items);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;

        if(convertView == null) {
            // inflate the GridView item layout
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.listview_comment_item, parent, false);


            // initialize the view holder
            viewHolder = new ViewHolder();
            viewHolder.creatorName = (TextView) convertView.findViewById(R.id.creatorName);
            viewHolder.creatorImage = (ImageView) convertView.findViewById(R.id.creatorImage);
            viewHolder.textComment = (TextView) convertView.findViewById(R.id.commentText);
            convertView.setTag(viewHolder);
        } else {
            // recycle the already inflated view
            viewHolder = (ViewHolder) convertView.getTag();
        }

        // update the item view
        final ListViewItem item = getItem(position);
        viewHolder.creatorName.setText(item.creator_name);
        viewHolder.creatorImage.setImageDrawable(item.creator_photo);
        viewHolder.textComment.setText(item.description);

        return convertView;
    }

    /**
     * The view holder design pattern prevents using findViewById()
     * repeatedly in the getView() method of the adapter.
     *
     */
    private static class ViewHolder {

        TextView textComment;
        TextView creatorName;
        ImageView creatorImage;
    }
}