package com.meyersj.mobilesurveyor.app.survey;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.meyersj.mobilesurveyor.app.util.Cons;
import com.meyersj.mobilesurveyor.app.util.Utils;
import com.newrelic.agent.android.NewRelic;

import java.util.Properties;

public class SurveyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Properties prop = Utils.getProperties(this, "config.properties");
        String token = prop.getProperty(Cons.NEWRELIC, "");
        Log.d("SurveyApplication", "token: " + token);
        if (!token.isEmpty()) {
            NewRelic.withApplicationToken(token).start(this);
        }
        loadPreferences(getApplicationContext());
    }

    private void loadPreferences(Context context) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);

        //this should only execute after program was installed for first time
        //grab default urls from properties and update sharedprefs with those
        if(!sharedPref.contains(Cons.SET_PREFS)) {
            Properties prop = Utils.getProperties(context, Cons.PROPERTIES);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putBoolean(Cons.SET_PREFS, true);
            editor.putString(Cons.BASE_URL, prop.getProperty(Cons.BASE_URL));
            editor.putString(Cons.SOLR_URL, prop.getProperty(Cons.SOLR_URL));
            editor.putString(Cons.LONG_TABS, prop.getProperty(Cons.LONG_TABS));
            editor.putString(Cons.LONG_HEADERS, prop.getProperty(Cons.LONG_HEADERS));
            //Log.d(TAG, prop.getProperty(Cons.MAP_RTES));
            editor.putString(Cons.MAP_RTES, prop.getProperty(Cons.MAP_RTES));
            editor.commit();
        }
    }

}
