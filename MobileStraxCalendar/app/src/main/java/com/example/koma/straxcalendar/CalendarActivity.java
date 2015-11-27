package com.example.koma.straxcalendar;

/**
 * Created by Koma on 25/11/15.
 */

    import android.os.AsyncTask;
    import android.os.Bundle;
    import android.support.v7.app.AppCompatActivity;
    import android.widget.CalendarView;
    import android.widget.CalendarView.OnDateChangeListener;
    import android.widget.Toast;
    import android.app.Activity;
    import android.content.Intent;
    import android.view.View;
    import android.util.Log;

    import java.io.BufferedReader;
    import java.io.BufferedWriter;
    import java.io.IOException;
    import java.io.InputStream;
    import java.io.InputStreamReader;
    import java.io.OutputStream;
    import java.io.OutputStreamWriter;
    import java.io.Reader;
    import java.io.UnsupportedEncodingException;
    import java.net.HttpURLConnection;
    import java.net.URL;
    import java.text.DateFormat;
    import java.text.SimpleDateFormat;
    import java.util.Date;
    import java.util.HashMap;
    import java.util.HashSet;

    import android.support.v7.app.ActionBarActivity;
    import android.os.Bundle;
    import android.view.Menu;
    import android.view.MenuItem;
    import android.widget.Toast;

    import org.joda.time.DateTime;
    import org.json.JSONArray;
    import org.json.JSONException;
    import org.json.JSONObject;

    import java.text.DateFormat;
    import java.text.SimpleDateFormat;
    import java.util.Date;
    import java.util.HashSet;
    import java.util.Iterator;

    import javax.net.ssl.HttpsURLConnection;


public class CalendarActivity extends AppCompatActivity
{

    private static String apiURL = "http://192.168.43.30:8080/api/";
    private static HashSet<Date> events = new HashSet<>();
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);
        new Getevents().execute(new String[]{apiURL + "events"});

        MyCalendarView cv = ((MyCalendarView)findViewById(R.id.calendar_view));

        cv.updateCalendar(events);

        // assign event handler
        cv.setEventHandler(new MyCalendarView.EventHandler()
        {
            @Override
            public void onDayLongPress(Date date)
            {
                // show returned day
                DateFormat df = SimpleDateFormat.getDateInstance();
                Toast.makeText(CalendarActivity.this, df.format(date), Toast.LENGTH_SHORT).show();
            }
        });
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
                        events.add(new DateTime(new JSONObject(jArray.getString(ii)).getString("start_event")).toDate());

                    }

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
            Log.d("sperem", "The response is: " + response);
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
        Log.i("boh", "addNewEvenPaget method");
        Intent intent = new Intent(this, AddEventActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
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
}