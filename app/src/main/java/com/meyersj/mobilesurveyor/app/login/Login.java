package com.meyersj.mobilesurveyor.app.login;

import android.os.AsyncTask;
import android.util.Log;

import com.meyersj.mobilesurveyor.app.util.Cons;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;


public class Login {

    private String TAG = getClass().getCanonicalName();
    private String endpoint;
    private HttpClient client;
    private LoginCallback callback;


    public interface LoginCallback {
        void onAuthenticate(LoginResponse response);
    }

    public Login(String endpoint, LoginCallback callback) {
        this.endpoint = endpoint;
        this.callback = callback;

        client = new DefaultHttpClient();
        //10 second timeout
        HttpParams httpParams = client.getParams();
        HttpConnectionParams.setConnectionTimeout(httpParams, 10 * 1000);
        HttpConnectionParams.setSoTimeout(httpParams, 10 * 1000);
    }

    public void authenticate(String name, String pass) {
        if (name == null || name.isEmpty() || pass == null || pass.isEmpty()) {
            return;
        }
        VerifyLoginTask task = new VerifyLoginTask();
        task.execute(buildParams(name, pass));
    }

    class VerifyLoginTask extends AsyncTask<String[], Void, String> {

        @Override
        protected String doInBackground(String[]... inParams) {
            String[] params = inParams[0];
            Log.d(TAG, "url:" + params[0]);
            Log.d(TAG, "data:" + params[1]);
            return post(params);
        }
        @Override
        protected void onPostExecute(String jsonResponse) {
            if (jsonResponse != null) {
                LoginResponse response = new LoginResponse(jsonResponse);
                if (response.isValid()) {
                    callback.onAuthenticate(response);
                }
            }
        }
    }

    protected String[] buildParams(String name, String pass) {
        JSONObject json = new JSONObject();
        json.put(Cons.USER_NAME, name);
        json.put(Cons.PASSWORD, pass);
        String credentials = json.toJSONString();
        String[] params = new String[2];
        params[0] = endpoint;
        params[1] = credentials;
        return params;
    }

    protected String post(String[] params) {
        String responseString = null;
        HttpPost post = new HttpPost(params[0]);

        ArrayList<NameValuePair> postParam = new ArrayList<NameValuePair>();
        postParam.add(new BasicNameValuePair(Cons.CRED, params[1]));

        try {
            post.setEntity(new UrlEncodedFormEntity(postParam));
            HttpResponse response = client.execute(post);
            HttpEntity entityR = response.getEntity();
            responseString = EntityUtils.toString(entityR);
            Log.d(TAG, responseString);
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "UnsupportedEncodingException");
            Log.e(TAG, e.toString());
        } catch (ClientProtocolException e) {
            Log.e(TAG, "ClientProtocolException: " + e.toString());
            Log.e(TAG, e.toString());
        } catch (IOException e) {
            Log.e(TAG, "IOException: " + e.toString());
            Log.e(TAG, e.toString());
        }
        return responseString;
    }
}
