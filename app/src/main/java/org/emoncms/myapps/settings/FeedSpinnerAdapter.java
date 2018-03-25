package org.emoncms.myapps.settings;

import android.content.Context;
import android.support.v7.widget.AppCompatTextView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import java.util.List;

/**
 * Adaptor to show a list of feeds
 */
public class FeedSpinnerAdapter extends ArrayAdapter {

    private List<String> feedNames;
    private List<Integer> feedIds;

    public FeedSpinnerAdapter(Context context, int resource, List<Integer> feedIds, List<String> feedNames) {
        super(context, resource, feedNames);
        this.feedNames = feedNames;
        this.feedIds = feedIds;
    }

    @Override
    public long getItemId(int position) {
        return feedIds.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        AppCompatTextView checkedTextView = (AppCompatTextView) super.getView(position, convertView, parent);
        checkedTextView.setText(feedNames.get(position));
        return checkedTextView;
    }

}

