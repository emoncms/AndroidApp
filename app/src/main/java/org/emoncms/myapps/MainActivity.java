package org.emoncms.myapps;

import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.support.v7.widget.Toolbar;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;

import org.emoncms.myapps.settings.AccountListFragment;
import org.emoncms.myapps.settings.AccountSettingsFragment;
import org.emoncms.myapps.settings.AppSettingsFragment;
import org.emoncms.myapps.settings.SettingsActivity;

public class MainActivity extends BaseActivity
{
    private Toolbar mToolbar;
    private DrawerLayout mDrawer;

    private RecyclerView mRecyclerView;

    private boolean fullScreenRequested;
    private boolean isFirstRun;
    private Handler mFullscreenHandler = new Handler();

    private static final String PREF_APP_FIRST_RUN = "app_first_run";

    public enum MyAppViews {
        MyElectricView,
        MyElectricSettingsView,
        MySolarView,
        MySolarSettingsView
    }

    MyAppViews displayed_view;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        UpgradeManager.doUpgrade(this);

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        isFirstRun = sp.getBoolean(PREF_APP_FIRST_RUN, true);
        sp.edit().putBoolean(PREF_APP_FIRST_RUN, false).apply();

        super.onCreate(savedInstanceState);

        PreferenceManager.setDefaultValues(this, R.xml.main_preferences, false);
        PreferenceManager.setDefaultValues(this, R.xml.me_preferences, false);

        setKeepScreenOn(sp.getBoolean(getString(R.string.setting_keepscreenon), false));

        setContentView(R.layout.activity_main);

        mToolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(mToolbar);

        setUpNavigation();



        displayed_view = MyAppViews.MyElectricView;

        if (savedInstanceState != null) {
            displayed_view = MyAppViews.values()[savedInstanceState.getInt("displayed_fragment", 0)];
        }

        showFragment(displayed_view);

        getWindow().getDecorView().setOnSystemUiVisibilityChangeListener(mOnSystemUiVisibilityChangeListener);

        if (isFirstRun) {
            mDrawer.openDrawer(GravityCompat.START);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        setKeepScreenOn(sp.getBoolean(getString(R.string.setting_keepscreenon), false));
    }

    private void setUpNavigation() {

        mRecyclerView = (RecyclerView) findViewById(R.id.RecyclerView);
        if (mRecyclerView != null) {
            mRecyclerView.setHasFixedSize(true);
        }

        OnNavigationClick onNavClickListener = new OnNavigationClick() {
            @Override
            public void onClick(NavigationDrawerAdapter.MenuOption option) {
                mDrawer.closeDrawers();
                if (option.id.equals("settings")) {
                    openSettingsActivity();
                } else {
                    setCurrentAccount(option.id);
                }
            }
        };

        NavigationDrawerAdapter adapter = new NavigationDrawerAdapter(this, onNavClickListener);
        mRecyclerView.setAdapter(adapter);

        EmonApplication.get().addAccountChangeListener(adapter);

        RecyclerView.LayoutManager navLayoutManager = new LinearLayoutManager(this);

        mRecyclerView.setLayoutManager(navLayoutManager);

        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(this,mDrawer,mToolbar, R.string.open, R.string.close);

        mDrawer.addDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt("displayed_fragment", displayed_view.ordinal());
        super.onSaveInstanceState(outState);
    }

    private void setCurrentAccount(String accountId) {
        EmonApplication.get().setCurrentAccount(accountId);
        showFragment(MyAppViews.MyElectricView);

    }

    public boolean setFullScreen() {

        if (fullScreenRequested)
            mFullscreenHandler.removeCallbacksAndMessages(null);
        else
            mFullscreenHandler.post(mSetFullScreenRunner);

        fullScreenRequested = !fullScreenRequested;

        return fullScreenRequested;
    }

    private Runnable mSetFullScreenRunner = new Runnable()
    {
        @Override
        public void run()
        {
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            {
                getWindow().getDecorView().setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_FULLSCREEN |
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                        View.SYSTEM_UI_FLAG_IMMERSIVE);
            }
            else if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
            {
                getWindow().getDecorView().setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_FULLSCREEN |
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
            }
            else
            {
                getWindow().getDecorView().setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_LOW_PROFILE |
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
            }
        }
    };

    public void setKeepScreenOn(boolean keep_screen_on) {
        if (keep_screen_on)
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        else
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    private View.OnSystemUiVisibilityChangeListener mOnSystemUiVisibilityChangeListener = new View.OnSystemUiVisibilityChangeListener() {
        @Override
        public void onSystemUiVisibilityChange(int visibility) {
            ActionBar ab = getSupportActionBar();
            if (ab == null)
                return;

            if ((visibility & View.SYSTEM_UI_FLAG_HIDE_NAVIGATION) == View.VISIBLE)
            {
                mToolbar.startAnimation(AnimationUtils.loadAnimation(MainActivity.this, R.anim.slide_down));
                ab.show();
                if (fullScreenRequested)
                    mFullscreenHandler.postDelayed(mSetFullScreenRunner, 5000);
            }
            else
            {
                mToolbar.startAnimation(AnimationUtils.loadAnimation(MainActivity.this, R.anim.slide_up));
                ab.hide();
            }
        }
    };

    @Override
    public void onBackPressed() {

        if (getFragmentManager().findFragmentByTag(getResources().getString(R.string.tag_me_fragment)) == null)
        {
            showFragment(MyAppViews.MyElectricView);

           // setSelectedNavigationItem(0);
        }
        else
            super.onBackPressed();
    }

    private void openSettingsActivity() {
        Intent intent = new Intent(this,SettingsActivity.class);
        startActivity(intent);
    }

    public void showFragment(MyAppViews appView) {
        Fragment frag;
        String tag;
        displayed_view = appView;

        switch (appView) {
            case MyElectricSettingsView:
                tag = getResources().getString(R.string.tag_me_settings_fragment);
                frag = getFragmentManager().findFragmentByTag(tag);
                if (frag == null)
                    frag = new MyElectricSettingsFragment();
                break;

            default:
                tag = getResources().getString(R.string.tag_me_fragment) + "_" + EmonApplication.get().getCurrentAccount();;
                frag = getFragmentManager().findFragmentByTag(tag);
                if (frag == null)
                    frag = new MyElectricMainFragment();
                break;
        }

        if (fullScreenRequested)
        {
            mFullscreenHandler.removeCallbacksAndMessages(null);
            fullScreenRequested = false;
            getWindow().getDecorView().setSystemUiVisibility(0);
        }
        getFragmentManager().beginTransaction()
                .replace(R.id.container, frag, tag)
                .commit();
    }
}