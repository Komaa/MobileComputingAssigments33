package com.example.koma.straxcalendar;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.*;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

public class ModifyEventActivity extends AppCompatActivity {

    private EditText name;
    private EditText description;
    private EditText start_date;
    private EditText end_date;
    private CheckBox alert;
    private boolean isAlert;
    private EditText when_alert;
    private Button update;
    private Intent intent;

    private String user_id;

    private String event_id;

    //test on server
    //private static String apiURL = "http://130.233.42.94:8080/api/";

    //testing locally
    //Najeefa = 192.168.0.101  , Pietro = 192.168.43.30
    private static String apiURL = "http://192.168.0.101:8080/api/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify_event);
        setupVariables();

    }

    //initialize variables
    private void setupVariables() {
        intent =getIntent();
        SharedPreferences sharedpreferences = getSharedPreferences("session", Context.MODE_PRIVATE);
        user_id = sharedpreferences.getString("id", "").replace("\"", "");
        event_id = intent.getStringExtra("event_id");
        Log.i("boh", "event "+user_id +", "+event_id);
        name = (EditText) findViewById(R.id.name);
        description = (EditText) findViewById(R.id.descr);
        start_date = (EditText) findViewById(R.id.strtdate);
        end_date = (EditText) findViewById(R.id.enddate);
        alert = (CheckBox) findViewById(R.id.alert);
        when_alert = (EditText) findViewById(R.id.whenalert);
        update = (Button) findViewById(R.id.update);
        isAlert=false;
        getEvent();

    }

    //call to get the event details to modify
    public void getEvent(){
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            Log.i("boh:getEvent()", apiURL);
            new FetchEvent().execute(new String[]{apiURL+ "events/search/"+user_id+"?id="+event_id});
        } else {
            Toast.makeText(getApplicationContext(), "No internet connection!",
                    Toast.LENGTH_SHORT).show();
        }
    }

    //call when update button is pressed to modify event
    public void updateEvent(View view){
        String eventname = name.getText().toString();
        String eventdescr = description.getText().toString();
        String start = start_date.getText().toString();
        String end = end_date.getText().toString();
        String alertDate = when_alert.getText().toString();
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            Log.i("boh", apiURL);
            new ModifyEvent().execute(new String[]{apiURL+ "events/"+event_id, eventname, eventdescr, start, end, ""+isAlert, alertDate });
        } else {
            Toast.makeText(getApplicationContext(), "No internet connection!",
                    Toast.LENGTH_SHORT).show();
        }
    }

    public void onAlertClicked(View view){
        isAlert =  alert.isChecked();

    }

    private class FetchEvent extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            try {
                return getcall(urls[0]);
            } catch (IOException e) {
                return "Unable to retrieve the web page.";
            }
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            Log.i("boh:onPostExecute() ", "FetchEvent: "+result);
            try {
                JSONObject obj = new JSONObject(result);
                String eventName = obj.getString("name");
                String eventDescription = obj.getString("description");
                String start_event = obj.getString("start_event");
                String end_event = obj.getString("end_event");
                Boolean eventAlert = obj.getBoolean("alert");
                String alertWhen = obj.getString("when_alert");
                isAlert=eventAlert;
                DateTimeFormatter dtf = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");

                name.setText(eventName);
                description.setText(eventDescription);
                start_date.setText(dtf.print(new DateTime(DateTime.parse(start_event))));
                end_date.setText(dtf.print(new DateTime(DateTime.parse(end_event))));
                alert.setChecked(eventAlert);
                when_alert.setText(dtf.print(new DateTime(DateTime.parse(alertWhen))));
            }catch(Exception e){

            }

        }
    }

    private String getcall(String myurl) throws IOException {
        InputStream is = null;
        // Only display the first 500 characters of the retrieved
        // web page content.

        Log.i("boh:getcall() ", myurl);
        try {
            URL url = new URL(myurl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000 /* milliseconds */);
            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            // Starts the query
            conn.connect();
            int response = conn.getResponseCode();
            // Log.d("sperem", "The response is: " + response);
            is = conn.getInputStream();

            // Convert the InputStream into a string
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            StringBuilder out = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                out.append(line);
            }

            String contentAsString =out.toString();
            return contentAsString;

            // Makes sure that the InputStream is closed after the app is
            // finished using it.
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }
    // Uses AsyncTask to create a task away from the main UI thread. This task takes a
    // URL string and uses it to create an HttpUrlConnection. Once the connection
    // has been established, the AsyncTask downloads the contents of the webpage as
    // an InputStream. Finally, the InputStream is converted into a string, which is
    // displayed in the UI by the AsyncTask's onPostExecute method.
    private class ModifyEvent extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            HashMap<String, String> inputmap= new HashMap<String, String>();
            inputmap.put("name", urls[1]);
            inputmap.put("description", urls[2]);
            inputmap.put("start_event", urls[3]);
            inputmap.put("end_event", urls[4]);
            inputmap.put("alert", urls[5]);
            inputmap.put("when_alert", urls[6]);
            // params comes from the execute() call: params[0] is the url.
            try {
                return postcall(urls[0], inputmap);
            } catch (IOException e) {
                return "Unable to retrieve the web page.";
            }
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            Log.i("boh:onPostExecute()", result);
            Log.i("boh:onPostExecute()","no result");
            if(result.equals("Event Updated")){
                Log.i("boh", "Event Updated");
                Toast.makeText(getApplicationContext(), "Event Is Updated",
                        Toast.LENGTH_SHORT).show();
                super.onPostExecute(result);
                Intent intent = new Intent(ModifyEventActivity.this, CalendarActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                getApplicationContext().startActivity(intent);

            }else{
                Log.i("boh", "Cannot update event");
                Toast.makeText(getApplicationContext(), "Cannot Update Event",
                        Toast.LENGTH_SHORT).show();
            }

        }
    }

    // Given a URL, establishes an HttpUrlConnection and retrieves
    // the web page content as a InputStream, which it returns as
    // a string.
    private String postcall(String myurl, HashMap<String, String> postDataParams) throws IOException {

        // Only display the first 500 characters of the retrieved
        // web page content.
        int len = 500;
        String response = "";
        try {
            Log.i("boh", "postcall");
            URL url = new URL(myurl);
            Log.i("boh", ""+url);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000 /* milliseconds */);
            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setRequestMethod("PUT");
            conn.setDoInput(true);
            conn.setDoOutput(true);

            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(os, "UTF-8"));
            writer.write(getPostDataString(postDataParams));

            writer.flush();
            writer.close();
            os.close();
            int responseCode=conn.getResponseCode();

            if (responseCode == HttpsURLConnection.HTTP_OK) {
                String line;
                BufferedReader br=new BufferedReader(new InputStreamReader(conn.getInputStream()));
                while ((line=br.readLine()) != null) {
                    response+=line;
                }
            }
            else {
                response="";

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.i("boh:", response);
        return response;
    }

    private String getPostDataString(HashMap<String, String> params) throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for(Map.Entry<String, String> entry : params.entrySet()){
            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
        }

        return result.toString();
    }


}
