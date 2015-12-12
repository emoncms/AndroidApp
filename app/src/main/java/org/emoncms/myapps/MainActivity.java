package org.emoncms.myapps;

import android.app.FragmentManager;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.support.v7.widget.Toolbar;

public class MainActivity extends AppCompatActivity
{
    boolean isFullScreen = false;
    Toolbar mToolbar;
    DrawerLayout mDrawer;

    int TITLE_IDS[] = {R.string.me_title, R.string.settings_title};
    int ICONS[] = {R.drawable.ic_my_electric_white_36dp, R.drawable.ic_settings_applications_white_36dp};

    RecyclerView mRecyclerView;
    RecyclerView.Adapter mAdapter;
    RecyclerView.LayoutManager mLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        isFullScreen = SP.getBoolean("display_fullscreen", false);

        setContentView(R.layout.activity_main);

        FragmentManager fragmentManager = getFragmentManager();

        mToolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(mToolbar);

        mRecyclerView = (RecyclerView) findViewById(R.id.RecyclerView);
        mRecyclerView.setHasFixedSize(true);

        mAdapter = new NavigationDrawerAdapter(this, TITLE_IDS, ICONS);
        mRecyclerView.setAdapter(mAdapter);

        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        final GestureDetector mGestureDetector = new GestureDetector(MainActivity.this, new GestureDetector.SimpleOnGestureListener() {
            @Override public boolean onSingleTapUp(MotionEvent e) {
                return true;
            }
        });

        mRecyclerView.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(RecyclerView recyclerView, MotionEvent motionEvent) {
                View child = recyclerView.findChildViewUnder(motionEvent.getX(),motionEvent.getY());

                if(child!=null && mGestureDetector.onTouchEvent(motionEvent)){
                    int position = recyclerView.getChildAdapterPosition(child);
                    setSelectedNavigationItem(position);
                    mDrawer.closeDrawers();

                    switch (position) {
                        case 0:
                            getFragmentManager().beginTransaction()
                                    .replace(R.id.container, new MyElectricMainFragment(), getResources().getString(R.string.tag_me_fragment))
                                    .commit();
                            break;
                        case 1:
                            getFragmentManager().beginTransaction()
                                    .replace(R.id.container, new SettingsFragment(), getResources().getString(R.string.tag_settings_fragment))
                                    .commit();
                            break;
                    }

                    return true;
                }
                return false;
            }

            @Override
            public void onTouchEvent(RecyclerView recyclerView, MotionEvent motionEvent) {
            }

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
            }
        });

        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(this,mDrawer,mToolbar,R.string.openDrawer,R.string.closeDrawer);
        mDrawer.setDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();

        fragmentManager.beginTransaction()
                .replace(R.id.container, new MyElectricMainFragment(), getResources().getString(R.string.tag_me_fragment))
                .commit();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (isFullScreen && hasFocus)
            setFullScreen();
    }

    void setFullScreen()
    {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
        {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                            | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
        else if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
        {
            int mUIFlag = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;

            getWindow().getDecorView().setSystemUiVisibility(mUIFlag);
        }
        else
        {
            int mUIFlag = View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;

            getWindow().getDecorView().setSystemUiVisibility(mUIFlag);
        }
    }

    private View.OnSystemUiVisibilityChangeListener mOnSystemUiVisibilityChangeListener = new View.OnSystemUiVisibilityChangeListener() {
        @Override
        public void onSystemUiVisibilityChange(int visibility) {
            ActionBar ab = getSupportActionBar();
            if (ab == null)
                return;

            if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == View.VISIBLE)
            {
                //mToolbar.startAnimation(mSlideDown);
                ab.show();
            }
            else
            {
                ab.hide();
                //mToolbar.startAnimation(mSlideUp);
            }
        }
    };

    private void setSelectedNavigationItem(int position) {
        View selected_child = mRecyclerView.getChildAt(((NavigationDrawerAdapter) mAdapter).getSelectedItem());
        if (selected_child != null) selected_child.setSelected(false);
        ((NavigationDrawerAdapter) mAdapter).setSelectedItem(position);
        selected_child = mRecyclerView.getChildAt(position);
        selected_child.setSelected(true);
    }

    @Override
    public void onBackPressed() {

        if (getFragmentManager().findFragmentByTag(getResources().getString(R.string.tag_me_fragment)) == null)
        {
            getFragmentManager().beginTransaction()
                    .replace(R.id.container, new MyElectricMainFragment(), getResources().getString(R.string.tag_me_fragment))
                    .commit();

            setSelectedNavigationItem(0);
        }
        else
            super.onBackPressed();
    }
}