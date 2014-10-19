package ai.autonumber.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import ai.autonumber.R;
import ai.autonumber.util.ImageLoader;

/**
 * Created by Andrew on 19.10.2014.
 */
public class LazyPhotoLoadAdapter extends BaseAdapter {
    private Activity activity;
    private List<Integer> photoIds = new ArrayList<Integer>();
    private static LayoutInflater inflater = null;
    private ImageLoader imageLoader;


    public LazyPhotoLoadAdapter(Activity activity) {
        this.activity = activity;
        imageLoader = new ImageLoader(activity);
        inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void add(Integer photoId) {
        for (Integer id : photoIds) {
            if (id.equals(photoId))
                return;
        }

        photoIds.add(photoId);
        Collections.sort(photoIds, new Comparator<Integer>() {
            @Override
            public int compare(Integer mi1, Integer mi2) {
                return -mi1.compareTo(mi2);
            }
        });
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return photoIds.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        if (row == null)
            row = inflater.inflate(R.layout.photo_list_item, parent, false);

        ImageView photoView = (ImageView) row.findViewById(R.id.photoView);

        imageLoader.displayImage(photoIds.get(position), photoView);
        return row;
    }

    public void clearCaches() {
        imageLoader.clearCache();
    }

    public Bitmap getImageBitmap(int position) {
        Integer imageId = photoIds.get(position);
        return imageLoader.getBitmapFromCache(imageId);
    }
}
