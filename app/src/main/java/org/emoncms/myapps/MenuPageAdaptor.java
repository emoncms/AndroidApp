package org.emoncms.myapps;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.emoncms.myapps.myelectric.MyElectricSettings;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Handles the list of "app" pages in the navigation drawer.
 */
public class MenuPageAdaptor extends RecyclerView.Adapter<MenuPageAdaptor.ViewHolder> implements PageChangeListener  {

    private Context mContext;

    private List<MenuOption> menuOptionList;
    private OnNavigationClick onNavigationClick;
    private int selectedItem = 0;

    @Override
    public void onAddPage(MyElectricSettings settings) {
        menuOptionList.add(menuOptionList.size()-1,new MenuOption(""+(menuOptionList.size()-1), R.drawable.ic_my_electric_white_36dp, settings.getName()));
        notifyDataSetChanged();

    }

    @Override
    public void onDeletePage(MyElectricSettings settings) {

        for (Iterator<MenuOption> iterator = menuOptionList.iterator(); iterator.hasNext(); ) {
            MenuOption item = iterator.next();
            if (item.settings != null && item.settings.getId() == settings.getId()) {
                iterator.remove();
                notifyDataSetChanged();
            }
        }
        //notifyDataSetChanged();

    }

    @Override
    public void onUpdatePage(MyElectricSettings settings) {
        for (Iterator<MenuOption> iterator = menuOptionList.iterator(); iterator.hasNext(); ) {
            MenuOption item = iterator.next();
            if (item.settings != null && item.settings.getId() == settings.getId()) {
                item.settings = settings;
                item.text = settings.getName();
            }
        }
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView textView;
        private ImageView imageView;
        private View divider;

        public ViewHolder(View itemView) {
            super(itemView);
            textView = (TextView) itemView.findViewById(R.id.rowText);
            imageView = (ImageView) itemView.findViewById(R.id.rowIcon);
            divider = itemView.findViewById(R.id.rowDivider);
        }

        public void bind(final MenuOption option, final OnNavigationClick onNavigationClick) {
            textView.setText(option.text);
            imageView.setImageResource(option.icon);
            itemView.setSelected(selectedItem == getLayoutPosition());
            if (option.id.equals("new")) {
                divider.setVisibility(View.VISIBLE);
            }


            View.OnClickListener onClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //only change selection if we are switching accounts
                    notifyItemChanged(selectedItem);
                    if (!option.id.equals("new")) {
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
    public MenuPageAdaptor(Context context, OnNavigationClick onNavigationClick) {
        mContext = context;
        this.onNavigationClick = onNavigationClick;
        menuOptionList = new ArrayList<>();

        int index = 0;


        for(int i = 0; i < EmonApplication.get().getPages().size(); i++) {
            menuOptionList.add(new MenuOption(""+i, R.drawable.ic_my_electric_white_36dp, EmonApplication.get().getPages().get(i).getName(),EmonApplication.get().getPages().get(i)));
            index++;
        }
        menuOptionList.add(new MenuOption("new", R.drawable.ic_my_electric_white_36dp, "Add Page"));

        EmonApplication.get().addPageChangeListener(this);


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
    public MenuPageAdaptor.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.menu_item_page,parent,false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(MenuPageAdaptor.ViewHolder holder, int position) {
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
        MyElectricSettings settings;

        public MenuOption(String id, int icon, String text, MyElectricSettings settings) {
            this.id = id;
            this.icon = icon;
            this.text = text;
            this.settings = settings;
        }

        public MenuOption(String id, int icon, String text) {
            this.id = id;
            this.icon = icon;
            this.text = text;
        }


    }

}