package com.meyersj.mobilesurveyor.app.login;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.meyersj.mobilesurveyor.app.R;
import com.meyersj.mobilesurveyor.app.menu.SettingsActivity;
import com.meyersj.mobilesurveyor.app.util.Cons;
import com.meyersj.mobilesurveyor.app.util.Utils;

import java.util.Properties;


public class LoginActivity extends Activity implements Login.LoginCallback {

    private static final String TAG = "LoginActivity";
    private final String SETLINE = "com.meyersj.mobilesurveyor.app.SETLINE";

    private Context context;
    private EditText username;
    private EditText password;
    private Button loginButton, skip_login;
    private Properties prop;
    private Login login;

    private static final int RESULT_SETTINGS = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        context = getApplicationContext();
        //loadPreferences(context);

        username = (EditText) findViewById(R.id.username);
        password = (EditText) findViewById(R.id.password);
        loginButton = (Button) findViewById(R.id.login);
        skip_login = (Button) findViewById(R.id.skip_login);
        prop = Utils.getProperties(getApplicationContext(), Cons.PROPERTIES);
        final String test_user = prop.getProperty(Cons.TEST_USER);

        login = new Login(Utils.getUrlApi(context) + "/verifyUser", this);

        loginButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //close keypad
                InputMethodManager inputManager = (InputMethodManager)
                        getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                        InputMethodManager.HIDE_NOT_ALWAYS);

                // send username and password to server
                // onAuthenticate callback will be called with the response
                String name = username.getText().toString().trim();
                String pass = password.getText().toString().trim();
                login.authenticate(name, pass);
            }
        });

        password.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) ||
                        (actionId == EditorInfo.IME_ACTION_DONE)) {
                    Log.d(TAG,"Enter pressed");
                    loginButton.performClick();
                }
                return false;
            }
        });

        skip_login.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startCollection(test_user);
            }
        });
    }

    @Override
    public void onAuthenticate(LoginResponse response) {
        if (!response.isValidUser()) {
            Log.d(TAG, "username did not match");
            Utils.shortToastCenter(context,
                    "No record of that user, please re-enter username.");
        }
        else if (!response.isValidPassword()) {
            Log.d(TAG, "password not correct");
            Utils.shortToastCenter(context,
                    "Incorrect password, please re-enter.");
            password.setText("");
        }
        else if (response.getUserID() != null){
            //user and password match
            //move user to SetLineActivity
            password.setText("");
            startCollection(response.getUserID());
        }
        // TODO handle case where userID is null
    }

    @Override
    public boolean onKeyDown(int keycode, KeyEvent e) {
        switch(keycode) {
            //start settings activity
            case KeyEvent.KEYCODE_MENU:
                Intent i = new Intent(context, SettingsActivity.class);
                startActivityForResult(i, RESULT_SETTINGS);
                return true;
            default:
                return super.onKeyDown(keycode, e);
        }
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

    protected void startCollection(String username) {
        Intent intent = new Intent(SETLINE);
        intent.putExtra(Cons.USER_ID, username);
        startActivity(intent);
    }
}
