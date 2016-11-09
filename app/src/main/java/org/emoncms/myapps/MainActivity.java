package org.emoncms.myapps;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.viewpagerindicator.PageIndicator;

import org.emoncms.myapps.settings.SettingsActivity;

/**
 * Handles navigation, account changing and pager
 */
public class MainActivity extends BaseActivity  {
    private Toolbar mToolbar;
    private DrawerLayout mDrawer;

    private TextView accountSelector;
    private RecyclerView navAccountView;
    private RecyclerView navPageView;
    private MyPagerAdapter pagerAdapter;
    private ViewPager vpPager;

    private boolean fullScreenRequested;
    private boolean isFirstRun;
    private boolean accountListVisible = false;

    private Handler mFullscreenHandler = new Handler();

    private static final String PREF_APP_FIRST_RUN = "app_first_run";

    public static class MyPagerAdapter extends FragmentStatePagerAdapter {

        public MyPagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }


        @Override
        public int getCount() {
            return EmonApplication.get().getPages().size();
        }

        // Returns the fragment to display for that page
        @Override
        public Fragment getItem(int position) {
            Log.d("pager","making page " + position);
            return MyElectricMainFragment.newInstance(EmonApplication.get().getPages().get(position));
        }

        // Returns the page title for the top indicator
        @Override
        public CharSequence getPageTitle(int position) {
            return EmonApplication.get().getPages().get(position).getName();
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
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

    /**
     * Switch drawer layout contents between accounts and apps
     */
    private void toggleNavigation() {
        if (accountListVisible) {
            navAccountView.setVisibility(View.GONE);
            navPageView.setVisibility(View.VISIBLE);
            accountSelector.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_drop_down_black_24dp, 0);
            accountListVisible = false;
        } else {
            navAccountView.setVisibility(View.VISIBLE);
            navPageView.setVisibility(View.GONE);
            accountSelector.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_drop_up_black_24dp, 0);
            accountListVisible = true;
        }
    }

    private void setUpNavigation() {
        accountSelector = (TextView) findViewById(R.id.selectAccount);

        accountSelector.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleNavigation();
            }
        });
        accountSelector.setText(EmonApplication.get().getAccounts().get(EmonApplication.get().getCurrentAccount()));

        //account list
        navAccountView = (RecyclerView) findViewById(R.id.accountMenu);
        navAccountView.setVisibility(View.INVISIBLE);

        if (navAccountView != null) {
            navAccountView.setHasFixedSize(true);
        }

        OnNavigationClick onAccountClickListener = new OnNavigationClick() {
            @Override
            public void onClick(String id) {
                mDrawer.closeDrawers();
                if (id.equals("settings")) {
                    openSettingsActivity();
                } else {
                    toggleNavigation();
                    setCurrentAccount(id);
                }
            }
        };

        MenuAccountAdapter accountAdapter = new MenuAccountAdapter(this, onAccountClickListener);
        navAccountView.setAdapter(accountAdapter);
        EmonApplication.get().addAccountChangeListener(accountAdapter);
        RecyclerView.LayoutManager navLayoutManager = new LinearLayoutManager(this);
        navAccountView.setLayoutManager(navLayoutManager);

        //account list
        navPageView = (RecyclerView) findViewById(R.id.appMenu);
        navPageView.setVisibility(View.VISIBLE);

        if (navPageView != null) {
            navPageView.setHasFixedSize(true);
        }

        setUpPages();

        RecyclerView.LayoutManager navAppLayoutManager = new LinearLayoutManager(this);
        navPageView.setLayoutManager(navAppLayoutManager);


        //drawer toggle
        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(this, mDrawer, mToolbar, R.string.open, R.string.close);

        mDrawer.addDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();

    }

    private void setUpPages() {

        OnNavigationClick onPageClickListener = new OnNavigationClick() {
            @Override
            public void onClick(String id) {
                mDrawer.closeDrawers();
                if (id.equals("new")) {
                    openPageSettings();
                } else {
                    vpPager.setCurrentItem(Integer.valueOf(id),true);
                }
            }
        };

        MenuPageAdaptor appAdapter = new MenuPageAdaptor(this, onPageClickListener);
        navPageView.setAdapter(appAdapter);

        vpPager = (ViewPager) findViewById(R.id.vpPager);

        if (pagerAdapter != null) {
            //this will wipe the fragments already associated with the pager
            vpPager.setAdapter(null);

        }

        pagerAdapter = new MyPagerAdapter(getSupportFragmentManager());
        pagerAdapter.notifyDataSetChanged();
        vpPager.setAdapter(pagerAdapter);

        PageIndicator indicator = (PageIndicator) findViewById(R.id.indicator);
        indicator.setViewPager(vpPager);


    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            openPageSettings();
            return true;
        } else if (id == R.id.full_screen) {
            boolean fullScreen = setFullScreen();
            if (fullScreen)
                item.setIcon(R.drawable.ic_fullscreen_exit_white_24dp);
            else
                item.setIcon(R.drawable.ic_fullscreen_white_24dp);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void openPageSettings() {
        Intent intent = new Intent(this, MyElectricSettingsActivity.class);
        //FIXME send current page number
        startActivity(intent);
    }

    private void setCurrentAccount(String accountId) {
        EmonApplication.get().setCurrentAccount(accountId);
        accountSelector.setText(EmonApplication.get().getAccounts().get(EmonApplication.get().getCurrentAccount()));
        setUpPages();

    }

    public boolean setFullScreen() {

        if (fullScreenRequested)
            mFullscreenHandler.removeCallbacksAndMessages(null);
        else
            mFullscreenHandler.post(mSetFullScreenRunner);

        fullScreenRequested = !fullScreenRequested;

        return fullScreenRequested;
    }

    private Runnable mSetFullScreenRunner = new Runnable() {
        @Override
        public void run() {
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                getWindow().getDecorView().setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_FULLSCREEN |
                                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                                View.SYSTEM_UI_FLAG_IMMERSIVE);
            } else if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                getWindow().getDecorView().setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_FULLSCREEN |
                                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
            } else {
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

            if ((visibility & View.SYSTEM_UI_FLAG_HIDE_NAVIGATION) == View.VISIBLE) {
                mToolbar.startAnimation(AnimationUtils.loadAnimation(MainActivity.this, R.anim.slide_down));
                ab.show();
                if (fullScreenRequested)
                    mFullscreenHandler.postDelayed(mSetFullScreenRunner, 5000);
            } else {
                mToolbar.startAnimation(AnimationUtils.loadAnimation(MainActivity.this, R.anim.slide_up));
                ab.hide();
            }
        }
    };

    private void openSettingsActivity() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }


}