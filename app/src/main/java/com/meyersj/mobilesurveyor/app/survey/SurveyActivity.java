package com.meyersj.mobilesurveyor.app.survey;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;

import com.meyersj.mobilesurveyor.app.R;
import com.meyersj.mobilesurveyor.app.survey.Confirm.ConfirmFragment;
import com.meyersj.mobilesurveyor.app.survey.Location.PickLocationFragment;
import com.meyersj.mobilesurveyor.app.survey.OnOff.OnOffFragment;
import com.meyersj.mobilesurveyor.app.survey.Transfer.TransfersMapFragment;
import com.meyersj.mobilesurveyor.app.util.Cons;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SurveyActivity extends FragmentActivity implements ActionBar.TabListener {

    public enum TabTypes { ORIGIN, DESTINATION, ON_OFF, TRANSFERS }

    private final String TAG = "SurveyActivity";
    protected static Integer fragmentCount = null;
    protected final String CONFIRM = "Confirm";
    protected final String ODK_ACTION = "com.meyersj.mobilesurveyor.app.ODK_SURVEY";
    protected static List<String> headers = null;

    protected AppSectionsPagerAdapter mAppSectionsPagerAdapter;
    protected ViewPager mViewPager;
    protected Button previousBtn;
    protected Button nextBtn;
    protected static SurveyManager manager;
    protected static Fragment[] fragments;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_survey);
        previousBtn = (Button) this.findViewById(R.id.previous_fragment);
        nextBtn = (Button) this.findViewById(R.id.next_fragment);
        mAppSectionsPagerAdapter = new AppSectionsPagerAdapter(getSupportFragmentManager());
        final ActionBar actionBar = getActionBar();
        Bundle extras = getODKExtras();
        String line = "";
        if(extras != null) {
            line = extras.getString("rte", "");
            Log.d(TAG, line);
        }
        manager = new SurveyManager(getApplicationContext(), this, line);

        String[] tabTypes = fetchTabTypes(getApplicationContext());
        String[] configHeaders = fetchHeaders(getApplicationContext());

        if (tabTypes.length != configHeaders.length) {
            throw new RuntimeException("tabs and headers in config.properties are different lengths");
        }

        mViewPager = (ViewPager) findViewById(R.id.survey_pager);

        fragmentCount = initializeTabs(tabTypes, extras) + 1;

        headers = new ArrayList<String>(Arrays.asList(CONFIRM));
        headers.addAll(0, Arrays.asList(configHeaders));

        fragments[fragmentCount - 1] = new ConfirmFragment();
        ((ConfirmFragment) fragments[fragmentCount - 1]).setParams(this, manager, mViewPager);

        actionBar.setHomeButtonEnabled(false);
        actionBar.setTitle("TransitSurveyor");
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        mViewPager.setAdapter(mAppSectionsPagerAdapter);
        mViewPager.setOffscreenPageLimit(fragmentCount);
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
                MapFragment fragment = (MapFragment) fragments[position];
                fragment.updateView(manager);
                toggleNavButtons(mViewPager.getCurrentItem());
            }
        });

        for (int i = 0; i < fragmentCount; i++) {
            actionBar.addTab(actionBar.newTab().setText(headers.get(i)).setTabListener(this));
        }
        toggleNavButtons(mViewPager.getCurrentItem());
        previousBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mViewPager.setCurrentItem(mViewPager.getCurrentItem() - 1, true);
                toggleNavButtons(mViewPager.getCurrentItem());
            }
        });
        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mViewPager.setCurrentItem(mViewPager.getCurrentItem() + 1, true);
                toggleNavButtons(mViewPager.getCurrentItem());
            }
        });
    }

    protected void toggleNavButtons(int item) {
        previousBtn.setEnabled(true);
        nextBtn.setEnabled(true);
        if(item == 0)
            previousBtn.setEnabled(false);
        if(item == fragmentCount - 1)
            nextBtn.setEnabled(false);
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {}

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {}

    public static class AppSectionsPagerAdapter extends FragmentPagerAdapter {

        public AppSectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            return fragments[i];
        }

        @Override
        public int getCount() {
            return fragmentCount;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return "Section " + (position + 1);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)){
            manager.unfinishedExit(this);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    protected Bundle getODKExtras() {
        Intent intent = this.getIntent();
        String action = intent.getAction();
        if (action.equals(ODK_ACTION)) {
            return intent.getExtras();
        }
        return null;
    }

    public static String[] fetchTabTypes(Context context) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        String tabs = sharedPref.getString(Cons.LONG_TABS, "");
        if (tabs != null) {
            return tabs.split(",");
        }
        return new String[]{};
    }

    private String[] fetchHeaders(Context context) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        String headers = sharedPref.getString(Cons.LONG_HEADERS, "");
        if (headers != null) {
            return headers.split(",");
        }
        return new String[]{};
    }

    private Integer initializeTabs(String[] tabTypes, Bundle extras) {
        if (tabTypes == null || tabTypes.length == 0 || tabTypes[0].isEmpty()) {
            fragments = new Fragment[1];
            return 0;
        }
        fragments = new Fragment[tabTypes.length + 1];
        Integer tabIndex = 0;
        for (; tabIndex < tabTypes.length; tabIndex++) {
            if (compareType(tabTypes[tabIndex], TabTypes.ORIGIN)) {
                Log.d(TAG, "we have an origin!");
                fragments[tabIndex] = new PickLocationFragment();
                ((PickLocationFragment) fragments[tabIndex]).initialize(manager, "origin", extras);
            } else if (compareType(tabTypes[tabIndex], TabTypes.DESTINATION)) {
                Log.d(TAG, "we have an destination!");
                fragments[tabIndex] = new PickLocationFragment();
                ((PickLocationFragment) fragments[tabIndex]).initialize(manager, "destination", extras);
            } else if (compareType(tabTypes[tabIndex], TabTypes.ON_OFF)) {
                Log.d(TAG, "we have an onoff!");
                fragments[tabIndex] = new OnOffFragment();
                ((OnOffFragment) fragments[tabIndex]).initialize(manager, extras);
            } else if (compareType(tabTypes[tabIndex], TabTypes.TRANSFERS)) {
                Log.d(TAG, "we have an transfer!");
                fragments[tabIndex] = new TransfersMapFragment();
                ((TransfersMapFragment) fragments[tabIndex]).initialize(manager, mViewPager, extras);
            } else {
                // this is an error
                throw new RuntimeException("unexpected tab type " + tabTypes[tabIndex]);
            }
        }
        return tabIndex;
    }

    public static boolean compareType(String tab, TabTypes type) {
        return tab.equalsIgnoreCase(type.name());
    }
}