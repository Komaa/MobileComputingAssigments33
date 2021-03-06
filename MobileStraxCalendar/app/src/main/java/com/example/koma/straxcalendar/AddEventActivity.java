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
import android.widget.EditText;
import android.widget.CheckBox;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Date;

import javax.net.ssl.HttpsURLConnection;


public class AddEventActivity extends AppCompatActivity {

    private EditText name;
    private EditText description;
    private EditText start_date;
    private EditText end_date;
    private CheckBox alert;
    private boolean isAlert;
    private EditText when_alert;
    private Button addEvent;

    //harcoded user_id for testing
    private String user_id;

    //test on server
    //private static String apiURL = "http://130.233.42.94:8080/api/";

    //testing locally
    //Najeefa = 192.168.0.101  , Pietro = 192.168.43.30
    private static String apiURL = "http://192.168.0.101:8080/api/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_event);
        setupVariables();
    }

    private void setupVariables() {
        SharedPreferences sharedpreferences = getSharedPreferences("session", Context.MODE_PRIVATE);
        user_id = sharedpreferences.getString("id", "").replace("\"", "");
        Log.i("boh", "Add event "+user_id);
        name = (EditText) findViewById(R.id.name);
        description = (EditText) findViewById(R.id.descr);
        start_date = (EditText) findViewById(R.id.strtdate);
        end_date = (EditText) findViewById(R.id.enddate);
        alert = (CheckBox) findViewById(R.id.alert);
        when_alert = (EditText) findViewById(R.id.whenalert);
        addEvent = (Button) findViewById(R.id.add);
        isAlert=false;

    }

    public void addNewEvent(View view) {
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
            new DownloadWebpageTask().execute(new String[]{apiURL+ "events/"+user_id, eventname, eventdescr, start, end, ""+isAlert, alertDate });
        } else {
            Toast.makeText(getApplicationContext(), "No internet connection!",
                    Toast.LENGTH_SHORT).show();
        }
    }

    public void onAlertClicked(View view){
        isAlert =  alert.isChecked();

    }

    // Uses AsyncTask to create a task away from the main UI thread. This task takes a
    // URL string and uses it to create an HttpUrlConnection. Once the connection
    // has been established, the AsyncTask downloads the contents of the webpage as
    // an InputStream. Finally, the InputStream is converted into a string, which is
    // displayed in the UI by the AsyncTask's onPostExecute method.
    private class DownloadWebpageTask extends AsyncTask<String, Void, String> {
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
            Log.i("boh", result);
            if(result.equals("Event Added")){
                Log.i("boh", "event added");
                Toast.makeText(getApplicationContext(), "New Event Added",
                        Toast.LENGTH_SHORT).show();
                super.onPostExecute(result);
                Intent intent = new Intent(AddEventActivity.this, CalendarActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                getApplicationContext().startActivity(intent);

            }else{
                Log.i("boh", "Cannot add event");
                Toast.makeText(getApplicationContext(), "Error adding event",
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
            Log.i("boh", "here");
            URL url = new URL(myurl);
            Log.i("boh", ""+url);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000 /* milliseconds */);
            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setRequestMethod("POST");
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
