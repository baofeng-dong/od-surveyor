package com.meyersj.locationsurvey.app.scans;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

import com.meyersj.locationsurvey.app.util.Cons;
import com.meyersj.locationsurvey.app.util.Utils;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;


public class StopLookup {

    private final String TAG = "StopLookup";
    private Context context;
    private String url;
    private String line;
    private String dir;
    private TextView stopText;
    //private StopLookupTask currentTask;
    HttpClient client;

    public StopLookup(Context context, TextView stopText, String url, String line, String dir) {
        this.context = context;
        this.stopText = stopText;
        this.url = url;
        this.line = line;
        this.dir = dir;


        this.client = new DefaultHttpClient();
        HttpParams httpParams = client.getParams();

        //10 second timeout
        HttpConnectionParams.setConnectionTimeout(httpParams, 10 * 1000);
        HttpConnectionParams.setSoTimeout(httpParams, 10 * 1000);
        httpParams.setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);


    }

    public void findStop(String lat, String lon) {
        /*
        if( (currentTask != null ) &&
                (currentTask.getStatus() == AsyncTask.Status.RUNNING)) {
            currentTask.cancel(true);
            Log.d(TAG, "Cancel Current");
        }
        */

        StopLookupTask task = new StopLookupTask();
        //currentTask = task;
        //Log.d(TAG, "Start");
        task.execute(buildParams(lat, lon));

        //stopText.setText(lat + " " + lon);
    }

    private Response query(String[] params) {
        Integer code = 0;
        String responseString = null;

        HttpPost post = new HttpPost(params[0]);
        Log.d(TAG, params[0]);
        ArrayList<NameValuePair> postParam = new ArrayList<NameValuePair>();
        postParam.add(new BasicNameValuePair(Cons.DATA, params[1]));

        try {
            //Log.d(TAG, "execute");
            post.setEntity(new UrlEncodedFormEntity(postParam));
            HttpResponse response = client.execute(post);

            HttpEntity entityR = response.getEntity();
            responseString = EntityUtils.toString(entityR);
            Log.d(TAG, responseString);
            //Log.d(TAG, "End");

        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "UnsupportedEncodingException: " + e.toString());
            code = 1;
        } catch (ClientProtocolException e) {
            Log.e(TAG, "ClientProtocolException: " + e.toString());
            code = 2;
        } catch (IOException e) {
            Log.e(TAG, "IOException: " + e.toString());
            code = 3;
        }
        return new Response(code, responseString);
    }

    private String buildMessage(String message) {
        return Cons.NEAR_STOP + ": " + message;
    }

    private void updateDisplay(Response response) {
        String message;
        switch(response.code) {
            case 0:
                message = buildMessage(parseResponse(response.response));
                break;
            case 1:
                message = buildMessage("UnsupportedEncodingException");
                break;
            case 2:
                message = buildMessage("ClientProtocolException");
                break;
            case 3:
                message = buildMessage("IOException");
                break;
            default:
                message = buildMessage("Error");
        }
        stopText.setText(message);
    }

    private String[] buildParams(String lat, String lon) {
        String[] params = new String[2];
        JSONObject json = new JSONObject();
        json.put(Cons.LINE, line);
        json.put(Cons.DIR, dir);
        json.put(Cons.LAT, lat);
        json.put(Cons.LON, lon);
        params[0] = this.url;
        params[1] = json.toJSONString();
        return params;
    }

    private String parseResponse(String response) {
        String message = "error finding near stop";
        JSONParser parser = new JSONParser();

        try{
            Object obj = parser.parse(response);
            JSONObject results = (JSONObject) obj;
            String error = results.get("error").toString();
            String stopName = results.get("stop_name").toString();
            message = error.equals("false") ? stopName : message;

        } catch(ParseException pe){
            Log.e(TAG, pe.toString());
        }

        return message;
    }

    public class Response {
        public Integer code;
        public String response;

        public Response(Integer code, String response) {
            this.code = code;
            this.response = response;
        }
    }

    class StopLookupTask extends AsyncTask<String[], Void, Response> {

        @Override
        protected Response doInBackground(String[]... inParams) {
            String[] params = inParams[0];
            Log.d(TAG, params[1]);
            return query(params);
        }
        @Override
        protected void onPostExecute(Response response) {
            if(response != null) {
                updateDisplay(response);
            }
        }


    }
}