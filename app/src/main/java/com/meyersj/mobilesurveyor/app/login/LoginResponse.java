package com.meyersj.mobilesurveyor.app.login;

import android.util.Log;

import com.meyersj.mobilesurveyor.app.util.Cons;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


public class LoginResponse {

    private String TAG = getClass().getCanonicalName();
    private String userMatch;
    private String passwordMatch;
    private String userID;
    private Boolean valid;

    public LoginResponse(String jsonInput) {
        parse(jsonInput);
    }

    private void parse(String jsonInput) {
        JSONParser parser = new JSONParser();

        try {
            Object obj = parser.parse(jsonInput);
            JSONObject results = (JSONObject) obj;
            userMatch = results.get(Cons.USER_MATCH).toString();
            passwordMatch = results.get(Cons.PASS_MATCH).toString();
            userID = results.get(Cons.USER_ID).toString();
            valid = true;
        } catch(ParseException pe){
            Log.e(TAG, pe.toString());
            valid = false;
        }
    }

    public Boolean isValid() {
        return valid;
    }

    public Boolean isValidUser() {
        if (userMatch.equals("false")) {
            return false;
        }
        return true;
    }

    public Boolean isValidPassword() {
        if (passwordMatch.equals("false")) {
            return false;
        }
        return true;
    }

    public String getUserID() {
        return userID != null && !userID.isEmpty() ? userID : null;
    }
}
