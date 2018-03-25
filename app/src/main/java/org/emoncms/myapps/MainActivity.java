package org.emoncms.myapps;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.viewpagerindicator.PageIndicator;

import org.emoncms.myapps.myelectric.MyElectricSettings;
import org.emoncms.myapps.settings.AccountSettingsActivity;
import org.emoncms.myapps.settings.SettingsActivity;


/**
 * Handles navigation, account changing and pager
 */
public class MainActivity extends BaseActivity implements AccountListChangeListener {
    private Toolbar mToolbar;
    private DrawerLayout mDrawer;

    private TextView accountSelector;
    private RecyclerView navAccountView;
    private RecyclerView navPageView;
    private MyPagerAdapter pagerAdapter;
    private ViewPager vpPager;

    private boolean fullScreenRequested;

    private boolean accountListVisible = false;

    private Handler mFullscreenHandler = new Handler();


    public static class MyPagerAdapter extends FragmentStatePagerAdapter implements PageChangeListener {


        public MyPagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);

            EmonApplication.get().addPageChangeListener(this);
        }

        @Override
        public int getCount() {
            return EmonApplication.get().getPages().size();
        }

        // Returns the fragment to display for that page
        @Override
        public Fragment getItem(int position) {
            Log.d("emon", "making page " + position);
            MyElectricMainFragment frag = MyElectricMainFragment.newInstance(EmonApplication.get().getPages().get(position));
            if (position == 0) {
                frag.setUserVisibleHint(true);
            }

            return frag;
        }

        @Override
        public int getItemPosition(Object object) {
            //will cause all cached fragments to be recreated. no problem.
            return POSITION_NONE;
        }

        // Returns the page title for the top indicator
        @Override
        public CharSequence getPageTitle(int position) {
            return EmonApplication.get().getPages().get(position).getName();
        }

        @Override
        public void onAddPage(MyElectricSettings settings) {
            notifyDataSetChanged();
        }

        @Override
        public void onDeletePage(MyElectricSettings settings) {
            notifyDataSetChanged();
        }

        @Override
        public void onUpdatePage(MyElectricSettings settings) {
            notifyDataSetChanged();
        }

        @Override
        public void finishUpdate(ViewGroup container) {
            try{
                super.finishUpdate(container);
            } catch (NullPointerException nullPointerException){
                System.out.println("Catch the NullPointerException in FragmentPagerAdapter.finishUpdate");
            }
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UpgradeManager.doUpgrade(this);

        PreferenceManager.setDefaultValues(this, R.xml.main_preferences, false);
        PreferenceManager.setDefaultValues(this, R.xml.me_preferences, false);

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

        setKeepScreenOn(sp.getBoolean(getString(R.string.setting_keepscreenon), false));

        setContentView(R.layout.activity_main);

        mToolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(mToolbar);

        setUpNavigation();


        getWindow().getDecorView().setOnSystemUiVisibilityChangeListener(mOnSystemUiVisibilityChangeListener);

        EmonApplication.get().addAccountChangeListener(this);

     }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        setKeepScreenOn(sp.getBoolean(getString(R.string.setting_keepscreenon), false));

        if (EmonApplication.get().getAccounts().isEmpty()) {
            openSettingsActivity();
        }

        //we could have just got back from PageSettings, so set page title if it changed
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null && !EmonApplication.get().getPages().isEmpty() && vpPager != null) {
            Log.d("emon-main","Resumed setting title to " + EmonApplication.get().getPages().get(vpPager.getCurrentItem()).getName());
            actionBar.setTitle(EmonApplication.get().getAccounts().get(EmonApplication.get().getCurrentAccount()));
        }

        //we could have just got back from adding first account.
        if (!EmonApplication.get().getAccounts().isEmpty() && EmonApplication.get().getPages().isEmpty()) {
            accountSelector.setText(EmonApplication.get().getAccounts().get(EmonApplication.get().getCurrentAccount()));
            EmonApplication.get().addFirstPage();
        }

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
                if (id.equals("new")) {
                    addNewAccount();

                } else {
                    toggleNavigation();
                    Log.d("main", "Account " + id);
                    setCurrentAccount(id);
                    ActionBar actionBar = getSupportActionBar();
                    actionBar.setTitle(EmonApplication.get().getAccounts().get(id));
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

    private void addNewAccount() {


        String newAccountId = EmonApplication.get().addAccount();

        Log.d("emon-main", "Opening New account " + newAccountId);

        Intent intent = new Intent(this, AccountSettingsActivity.class);
        intent.putExtra("account", newAccountId);
        startActivity(intent);
    }

    private void openAccountSettings() {
        Intent intent = new Intent(this, AccountSettingsActivity.class);
        intent.putExtra("account", EmonApplication.get().getCurrentAccount());
        startActivity(intent);
    }


    private void setUpPages() {

        OnNavigationClick onPageClickListener = new OnNavigationClick() {
            @Override
            public void onClick(String id) {
                mDrawer.closeDrawers();
                if (id.equals("new")) {
                    openNewPageSettings();
                } else if (id.equals("settings")) {
                    openSettingsActivity();
                } else {
                    vpPager.setCurrentItem(Integer.valueOf(id), true);
                }
            }
        };

        MenuPageAdaptor appAdapter = new MenuPageAdaptor(this, onPageClickListener);
        navPageView.setAdapter(appAdapter);

        vpPager = (ViewPager) findViewById(R.id.vpPager);

        if (pagerAdapter != null) {
            //this will wipe the fragments already associated with the pager
            vpPager.setAdapter(null);
            EmonApplication.get().removePageChangeListener(pagerAdapter);

        }

        pagerAdapter = new MyPagerAdapter(getSupportFragmentManager());

        // When swiping between different sections, select the corresponding tab
        vpPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                Log.d("emon-main","Page Changed to " + EmonApplication.get().getPages().get(position).getId());
                ActionBar actionBar = getSupportActionBar();
                if (actionBar != null && !EmonApplication.get().getPages().isEmpty()) {
                    EmonApplication.get().currentPageIndex = position;
                }
            }
        });

        pagerAdapter.notifyDataSetChanged();
        vpPager.setAdapter(pagerAdapter);

        PageIndicator indicator = (PageIndicator) findViewById(R.id.indicator);
        indicator.setViewPager(vpPager);


    }

    private void setFullScreenIcon(MenuItem item, boolean fullScreen) {
        if (item != null) {
            if (fullScreen) {
                item.setIcon(R.drawable.ic_fullscreen_exit_white_24dp);
            } else {
                item.setIcon(R.drawable.ic_fullscreen_white_24dp);
            }
        }
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
            setFullScreenIcon(item,fullScreen);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        setFullScreenIcon(menu.findItem(R.id.full_screen),fullScreenRequested);
        return true;
    }

    private void openPageSettings() {
        Intent intent = new Intent(this, MyElectricSettingsActivity.class);
        int index = vpPager.getCurrentItem();
        MyElectricSettings settings = EmonApplication.get().getPages().get(index);
        intent.putExtra("settings", settings);
        startActivity(intent);
    }

    private void openNewPageSettings() {
        Intent intent = new Intent(this, MyElectricSettingsActivity.class);
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

    @Override
    public void onAddAccount(String id, String name) {
        if (id.equals(EmonApplication.get().getCurrentAccount())) {
            accountSelector.setText(EmonApplication.get().getAccounts().get(EmonApplication.get().getCurrentAccount()));
        }
    }

    @Override
    public void onDeleteAccount(String id) {

    }

    @Override
    public void onUpdateAccount(String id, String name) {
        if (id.equals(EmonApplication.get().getCurrentAccount())) {
            accountSelector.setText(EmonApplication.get().getAccounts().get(EmonApplication.get().getCurrentAccount()));
        }
    }
}