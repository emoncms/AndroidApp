package org.emoncms.myapps;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Handles the Items in the Account List in the navigaton drawer
 */
public class MenuAccountAdapter extends RecyclerView.Adapter<MenuAccountAdapter.ViewHolder> implements AccountListChangeListener {

    private Context mContext;

    private List<MenuOption> menuOptionList;
    private OnNavigationClick onNavigationClick;
    private int selectedItem = 0;

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView textView;
        private ImageView imageView;

        public ViewHolder(View itemView) {
            super(itemView);
            textView = (TextView) itemView.findViewById(R.id.rowText);
            imageView = (ImageView) itemView.findViewById(R.id.rowIcon);
            imageView.setVisibility(View.GONE);
        }

        public void bind(final MenuOption option, final OnNavigationClick onNavigationClick) {
            textView.setText(option.text);
            //imageView.setImageResource(option.icon);
            itemView.setSelected(selectedItem == getLayoutPosition());


            View.OnClickListener onClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //only change selection if we are switching accounts
                    notifyItemChanged(selectedItem);
                    if (!option.id.equals("settings")) {
                        selectedItem = getLayoutPosition();
                        notifyItemChanged(selectedItem);
                    }
                    onNavigationClick.onClick(option.id);
                }
            };

            itemView.setOnClickListener(onClickListener);
        }
    }

    /**
     * Context is required
     * @param context
     */
    public MenuAccountAdapter(Context context, OnNavigationClick onNavigationClick) {
        mContext = context;
        this.onNavigationClick = onNavigationClick;
        menuOptionList = new ArrayList<>();

        int index = 0;
        for(Map.Entry<String,String> account : EmonApplication.get().getAccounts().entrySet()) {

            if (account.getKey().equals(EmonApplication.get().getCurrentAccount())) {
                selectedItem = index;
            }
            menuOptionList.add(new MenuOption(account.getKey(), R.drawable.ic_my_electric_white_36dp, account.getValue()));
            index++;
        }

        menuOptionList.add(new MenuOption("settings", R.drawable.ic_settings_applications_white_36dp, "Settings"));
    }

    @Override
    public void onAddAccount(String id, String name) {
        int newPosition = menuOptionList.size()-1;
        menuOptionList.add(newPosition,new MenuOption(id, R.drawable.ic_my_electric_white_36dp, name));
        notifyItemInserted(newPosition);
    }
    @Override
    public void onDeleteAccount(String id) {
        int position = getPosition(id);
        menuOptionList.remove(position);
        notifyItemRemoved(position);
    }
    @Override
    public void onUpdateAccount(String id, String name) {
        int position = getPosition(id);
        menuOptionList.get(position).text = name;
        notifyItemChanged(position);
    }

    private int getPosition(String id) {
        int position = -1;
        for (int i = 0; i < menuOptionList.size(); i++) {
            if (menuOptionList.get(i).id.equals(id)) {
                position = i;
                break;
            }
        }
        return position;
    }

    @Override
    public MenuAccountAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_row,parent,false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(MenuAccountAdapter.ViewHolder holder, int position) {
        holder.bind(menuOptionList.get(position), onNavigationClick);
    }

    @Override
    public int getItemCount() {
        return menuOptionList.size();
    }

    public static class MenuOption {
        String id;
        int icon;
        String text;

        public MenuOption(String id, int icon, String text) {
            this.id = id;
            this.icon = icon;
            this.text = text;
        }


    }

}