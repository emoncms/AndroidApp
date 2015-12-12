package org.emoncms.myapps;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class NavigationDrawerAdapter extends RecyclerView.Adapter<NavigationDrawerAdapter.ViewHolder> {

    private Context mContext;
    private int mNavTitles[];
    private int mIcons[];
    private int selectedItem = 0;

    public static class ViewHolder extends RecyclerView.ViewHolder { //} implements View.OnClickListener {

        TextView textView;
        ImageView imageView;

        public ViewHolder(View itemView) {
            super(itemView);
            //itemView.setClickable(true);
            //itemView.setOnClickListener(this);
            textView = (TextView) itemView.findViewById(R.id.rowText);
            imageView = (ImageView) itemView.findViewById(R.id.rowIcon);
        }

        //@Override
        //public void onClick(View v) {
        //}
    }

    NavigationDrawerAdapter(Context context, int Titles[],int Icons[]) {
        mContext = context;
        mNavTitles = Titles;
        mIcons = Icons;
    }

    public int getSelectedItem() {
        return selectedItem;
    }

    public void setSelectedItem(int selected) {
        selectedItem = selected;
    }

    @Override
    public NavigationDrawerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_row,parent,false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(NavigationDrawerAdapter.ViewHolder holder, int position) {
        holder.textView.setText(mContext.getResources().getString(mNavTitles[position]));
        holder.imageView.setImageResource(mIcons[position]);
        if (selectedItem == position) holder.itemView.setSelected(true);
    }

    @Override
    public int getItemCount() {
        return mNavTitles.length;
    }

}