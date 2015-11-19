package com.meyersj.mobilesurveyor.app.menu;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Switch;

import com.meyersj.mobilesurveyor.app.R;
import com.meyersj.mobilesurveyor.app.util.Cons;
import com.meyersj.mobilesurveyor.app.util.DataLoader;
import com.meyersj.mobilesurveyor.app.util.Utils;

import java.util.List;
import java.util.Map;


public class SetLineActivity extends Activity {

    private final String TAG = "SetLineActivity";
    private final String SCANNER = "com.meyersj.mobilesurveyor.app.SCANNER";
    private final String ONOFFMAP = "com.meyersj.mobilesurveyor.app.ONOFFMAP";
    private static final int RESULT_SETTINGS = 1;

    private Context context;
    private Spinner line, dir;
    private String routeID;
    private String dirID;
    private String userID;
    private Boolean offMode = false;
    private Button record;
    private Button logout;
    private Switch modeSwitch;
    private Map<String, String> routeLookup;
    private Map<String, String[]> dirLookup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getApplicationContext();
        setContentView(R.layout.activity_set_line);

        routeLookup = DataLoader.getRoutesLookup(this);
        dirLookup = DataLoader.getDirLookup(this);
        List<String> routes = DataLoader.getRoutes(getApplicationContext());
        line = (Spinner)findViewById(R.id.line_spinner);
        dir = (Spinner)findViewById(R.id.dir_spinner);

        ArrayAdapter<String> routeAdapter = new ArrayAdapter<String>(this, R.layout.spinner, routes);
        line.setAdapter(routeAdapter);
        record = (Button) findViewById(R.id.record);
        logout = (Button) findViewById(R.id.logout);
        modeSwitch = (Switch) findViewById(R.id.offSwitch);

        getExtras();

        line.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent,
                                       View view, int pos, long id) {


                String selectedRouteString = line.getItemAtPosition(pos).toString();
                Log.d(TAG, selectedRouteString);
                routeID = routeLookup.get(selectedRouteString);
                Log.d(TAG, routeID);
                Boolean isMapRoute = false;

                for (String route: Utils.getMapRoutes(context)) {
                    if (route.equals(routeID)) {
                        isMapRoute = true;
                    }
                }

                if (isMapRoute) {
                    modeSwitch.setVisibility(View.INVISIBLE);
                }
                else {
                    modeSwitch.setVisibility(View.VISIBLE);
                }

                String[] directions = dirLookup.get(routeID);
                ArrayAdapter<String> dirAdapter = new ArrayAdapter<String>(context, R.layout.spinner, directions);
                dir.setAdapter(dirAdapter);
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub
            }
        });

        dir.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent,
                                       View view, int pos, long id) {
                dirID = String.valueOf(pos);
                String selectedDirString = dir.getItemAtPosition(pos).toString();
                //dirID = dirLookup.get(routeID)[pos];
                Log.d(TAG, selectedDirString);
                Log.d(TAG, "dirID: " + dirID);
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub

            }
        });

        record.setOnClickListener(new Button.OnClickListener() {
            @Override

            public void onClick(View v) {
                Intent intent;

                Log.d(TAG, "routeID for intent:" + routeID + ":end");

                if (ifMapRoute(routeID)) {
                    intent = new Intent(ONOFFMAP);
                    Log.d(TAG, "start map for selection");
                }
                else {
                    intent = new Intent(SCANNER);
                    Log.d(TAG, "start barcode scanner");
                }

                intent.putExtra(Cons.USER_ID, userID);
                intent.putExtra(Cons.OFF_MODE, offMode);
                Log.d(TAG, "user: " + userID);
                intent.putExtra(Cons.LINE, routeID);
                intent.putExtra(Cons.DIR, dirID);
                startActivity(intent);
            }
        });

        logout.setOnClickListener(new Button.OnClickListener() {
            @Override

            public void onClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(SetLineActivity.this);
                builder.setMessage("Are you sure you want to logout?")
                        //.setMessage(message)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                finish();
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //do nothing
                            }
                        });

                AlertDialog select = builder.create();
                select.show();
            }
        });
    }

    public void onSwitchClicked(View view) {

        Boolean on = ((Switch) view).isChecked();

        if (on) {
            Log.d(TAG, "switched on");
            offMode = true;
        }
        else {
            Log.d(TAG, "switched off");
            offMode = false;
        }
    }


    protected Boolean ifMapRoute(String route) {
        Boolean ifMapRoute = false;

        for(String x : Utils.getMapRoutes(context)){
            if(x.equals(route)) {
                ifMapRoute = true;
                break;
            }
        }
        return ifMapRoute;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.settings_action, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_settings:
                Intent i = new Intent(context, SettingsActivity.class);
                startActivityForResult(i, RESULT_SETTINGS);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK))
        {
            logout.performClick();
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    protected void getExtras() {
        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            if(extras.containsKey(Cons.USER_ID)) {
                userID = extras.getString(Cons.USER_ID);
                Log.d(TAG, extras.getString(Cons.USER_ID));
            }
        }
    }
}