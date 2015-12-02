package com.example.koma.straxcalendar;

/**
 * Created by Koma on 25/11/15.
 */

import android.Manifest;
import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Debug;
import android.provider.CalendarContract;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;

import javax.net.ssl.HttpsURLConnection;


public class CalendarActivity extends AppCompatActivity {

    //test on server
    //private static String apiURL = "http://130.233.42.94:8080/api/";

    //testing locally
    //Najeefa = 192.168.0.101  , Pietro = 192.168.43.30
    private static String apiURL = "http://192.168.43.30:8080/api/";

    private static HashSet<Date> date_events = new HashSet<>();
    public static HashSet<JSONObject> events = new HashSet<JSONObject>();
    public static HashSet<JSONObject> daily_events = new HashSet<JSONObject>();
    private Button syncfrom, syncto;
    // Projection array. Creating indices for this array instead of doing
// dynamic lookups improves performance.
    public static final String[] EVENT_PROJECTION = new String[]{
            CalendarContract.Calendars._ID,                           // 0
            CalendarContract.Calendars.ACCOUNT_NAME,                  // 1
            CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,         // 2
            CalendarContract.Calendars.OWNER_ACCOUNT                  // 3
    };

    // The indices for the projection array above.
    private static final int PROJECTION_ID_INDEX = 0;
    private static final int PROJECTION_ACCOUNT_NAME_INDEX = 1;
    private static final int PROJECTION_DISPLAY_NAME_INDEX = 2;
    private static final int PROJECTION_OWNER_ACCOUNT_INDEX = 3;
    private static final int MY_PERMISSIONS_REQUEST_READ_CALENDAR =1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        daily_events.clear();
        events.clear();
        daily_events.clear();
        setContentView(R.layout.activity_calendar);
        SharedPreferences sharedpreferences = getSharedPreferences("session", Context.MODE_PRIVATE);
        setupVariables();
        //System.out.println(apiURL + "events/" + sharedpreferences.getString("id","").replace("\"", ""));
        Log.i("boh : Calendar activity", sharedpreferences.getString("id", "").replace("\"", ""));
        new Getevents().execute(new String[]{apiURL + "events/" + sharedpreferences.getString("id", "").replace("\"", "")});
    }


    @TargetApi(Build.VERSION_CODES.M)
    public void requestReadPermission(){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_CALENDAR},
                    MY_PERMISSIONS_REQUEST_READ_CALENDAR);

        }else{
            getcalendar();
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_CALENDAR: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                   getcalendar();

                } else {

                    Toast.makeText(getApplicationContext(), "Permissions not granted",
                            Toast.LENGTH_SHORT).show();
                }
                return;
            }

            // other 'switch' lines to check for other
            // permissions this app might request
        }
    }

    private void setupVariables() {
        syncfrom = (Button) findViewById(R.id.syncfrombutton);
        syncto = (Button) findViewById(R.id.synctobutton);
    }


    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    public void getcalendar() {
        // Run query
        Cursor cur = null;
        ContentResolver cr = getContentResolver();
        Uri uri = CalendarContract.Calendars.CONTENT_URI;
        String selection = "(" + CalendarContract.Calendars.ACCOUNT_TYPE + " = ?)";
        String[] selectionArgs = new String[]{"com.google"};



        cur = cr.query(uri, EVENT_PROJECTION, selection, selectionArgs, null);

        while (cur.moveToNext()) {
            long calID = 0;
            String displayName = null;
            String accountName = null;
            String ownerName = null;

            // Get the field values
            calID = cur.getLong(PROJECTION_ID_INDEX);
            displayName = cur.getString(PROJECTION_DISPLAY_NAME_INDEX);
            accountName = cur.getString(PROJECTION_ACCOUNT_NAME_INDEX);
            ownerName = cur.getString(PROJECTION_OWNER_ACCOUNT_INDEX);

            // Do something with the values...

            Toast.makeText(getApplicationContext(), calID + displayName + accountName + ownerName + "  ",
                    Toast.LENGTH_SHORT).show();
        }
    }

    public void syncfrom(View view) {
        requestReadPermission();
        Toast.makeText(getApplicationContext(), "Synchronized from the mobile calendar!",
                Toast.LENGTH_SHORT).show();
    }

    public void syncto(View view) {
        getcalendar();
        Toast.makeText(getApplicationContext(), "Synchronized to the mobile calendar!",
                Toast.LENGTH_SHORT).show();
    }

        private class Getevents extends AsyncTask<String, Void, String> {
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
                JSONArray jArray = null;
                try {
                    Log.d("sperem", "The response is: " + result);

                    jArray = new JSONArray(result);

                    for(int ii=0; ii < jArray.length(); ii++) {
                        //System.out.println(jArray.getString(ii));
                        date_events.add(new DateTime(new JSONObject(jArray.getString(ii)).getString("start_event")).toDate());
                        events.add(new JSONObject(jArray.getString(ii)));
                    }

                    MyCalendarView cv = ((MyCalendarView)findViewById(R.id.calendar_view));

                    cv.updateCalendar(date_events);
                    // assign event handler
                    cv.setEventHandler(new MyCalendarView.EventHandler() {
                        @Override
                        public void onDayLongPress(Date date) {
                            // show returned day
                            daily_events.clear();
                            DateTimeFormatter fmt = DateTimeFormat.forPattern("dd-MMM-yy").withLocale(Locale.US);
                            String day = fmt.print(new DateTime(date));
                            for (JSONObject event : events) {
                                String daytoconfront="";
                                try {
                                    daytoconfront = fmt.print(new DateTime(event.getString("start_event")));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                System.out.println(daytoconfront);
                                if(day.equals(daytoconfront))
                                    daily_events.add(event);

                            }
                            Intent intent = new Intent(CalendarActivity.this, DailyEventsActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            getApplicationContext().startActivity(intent);
                        }


                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                }


            }
        }

        // Given a URL, establishes an HttpUrlConnection and retrieves
        // the web page content as a InputStream, which it returns as
        // a string.
    private String getcall(String myurl) throws IOException {
        InputStream is = null;
        // Only display the first 500 characters of the retrieved
        // web page content.


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
            String contentAsString = readIt(is);
            return contentAsString;

            // Makes sure that the InputStream is closed after the app is
            // finished using it.
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }

    // Reads an InputStream and converts it to a String.
    public String readIt(InputStream stream) throws IOException, UnsupportedEncodingException {

        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        StringBuilder out = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            out.append(line);
        }
        return out.toString();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_calendar, menu);
        return true;
    }

    public void addNewEventPage(View view) {
        Log.i("boh", "addNewEvenPage method");
        Intent intent = new Intent(this, AddEventActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    public void logoutApp(View view){
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            Log.i("boh", apiURL);
            new LogOutEvent().execute(new String[]{apiURL+ "users/logout"});
        } else {
            Toast.makeText(getApplicationContext(), "No internet connection!",
                    Toast.LENGTH_SHORT).show();
        }

    }

    private class LogOutEvent extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            // params comes from the execute() call: params[0] is the url.
            try {
                return postcall(urls[0]);
            } catch (IOException e) {
                return "Unable to retrieve the web page.";
            }
        }

        protected void onPostExecute(String result) {
            Log.i("boh", result);
            if(result.equals("true")){
                Toast.makeText(getApplicationContext(), "You are logged out",
                        Toast.LENGTH_SHORT).show();
                super.onPostExecute(result);
                Intent intent = new Intent(CalendarActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                getApplicationContext().startActivity(intent);

            }else{
                Toast.makeText(getApplicationContext(), "Cannot logout",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private String postcall(String myurl) throws IOException {
        int len = 500;
        String response = "";
        try {
            Log.i("boh", "logout called");
            URL url = new URL(myurl);
            Log.i("boh", "logout: "+url);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000 /* milliseconds */);
            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);

            OutputStream os = conn.getOutputStream();

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



    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings)
        {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
//...........................................................................
// Test delete Start (move everything to appropriate activity)

//call deleteEvent(View view) on button click
    public void deleteEvent(View view){
        String event_id= "5658a3e2c1b0e13e09565cd0";//hardcoded for now.
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            Log.i("boh", apiURL);
            new DeleteEvents().execute(apiURL+"events/"+event_id);
        } else {
            Toast.makeText(getApplicationContext(), "No internet connection!",
                    Toast.LENGTH_SHORT).show();
        }

    }

    private class DeleteEvents extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            try {
               return delete(urls[0]);
            } catch (IOException e) {
                return "Unable to retrieve the web page.";
            }
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            Log.i("boh :onPostExecute", result);

            if (result.contains("Event successfully deleted")){
                Log.i("boh", "event deleted");
                Toast.makeText(getApplicationContext(), "Event successfully deleted",
                        Toast.LENGTH_SHORT).show();
                super.onPostExecute(result);
               // Intent intent = new Intent(AddEventActivity.this, CalendarActivity.class);
             //   intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
               // getApplicationContext().startActivity(intent);

            }else{
                Log.i("boh", "Cannot delete event");
                Toast.makeText(getApplicationContext(), "Cannot delete event",
                        Toast.LENGTH_SHORT).show();
            }



        }
    }

    // Given a URL, establishes an HttpUrlConnection and retrieves
    // the web page content as a InputStream, which it returns as
    // a string.
    private String delete(String myurl) throws IOException {
        int len = 500;
        String response = "";
        try {
            Log.i("boh", "here");
            URL url = new URL(myurl);
            Log.i("boh", ""+url);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000 /* milliseconds */);
            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setRequestMethod("DELETE");
            //conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.connect();
            //OutputStream os = conn.getOutputStream();
            //BufferedWriter writer = new BufferedWriter(
                  //  new OutputStreamWriter(os, "UTF-8"));
           // writer.write(getPostDataString());

          //  writer.flush();
           // writer.close();
           // os.close();
            int responseCode=conn.getResponseCode();

            if (responseCode == HttpsURLConnection.HTTP_OK) {
                Log.i("boh", "response code ok");
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





    //Test delete End (move everything to appropriate activity)
//...........................................................................

}