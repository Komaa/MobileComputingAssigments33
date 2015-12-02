package com.example.koma.straxcalendar;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashSet;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by Koma on 28/11/15.
 */
public class DailyEventsActivity extends Activity  {

    ListView l1;

    //test on server
    //private static String apiURL = "http://130.233.42.94:8080/api/";

    //testing locally
    //Najeefa = 192.168.0.101  , Pietro = 192.168.43.30
    private static String apiURL = "http://192.168.1.4:8080/api/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dailyevents_activity);
        l1=(ListView)findViewById(R.id.list);
        l1.setAdapter(new dataListAdapter(CalendarActivity.daily_events));



    }

    class dataListAdapter extends BaseAdapter {
        JSONObject[] events_adapter;


        dataListAdapter() {
            events_adapter = null;

        }

        public dataListAdapter(HashSet<JSONObject> events) {

            events_adapter = events.toArray(new JSONObject[events.size()]);
            System.out.println(events_adapter.length);

        }

        public int getCount() {
            // TODO Auto-generated method stub
            return events_adapter.length;
        }

        public Object getItem(int arg0) {
            // TODO Auto-generated method stub
            return events_adapter[arg0];
        }

        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {

            LayoutInflater inflater = getLayoutInflater();
            View row;
            row = inflater.inflate(R.layout.events_display, parent, false);
            TextView name, description, start_event, end_event,repetition_event,when_repetition_event,
                    alert_event, when_alert_event, type_event;
            Button edit, delete;
            name = (TextView) row.findViewById(R.id.text_name_event);
            description = (TextView) row.findViewById(R.id.text_description_event);
            start_event = (TextView) row.findViewById(R.id.text_start_event);
            end_event = (TextView) row.findViewById(R.id.text_end_event);
            repetition_event= (TextView) row.findViewById(R.id.text_repetition_event);
            when_repetition_event= (TextView) row.findViewById(R.id.text_when_repetition_event);
            alert_event = (TextView) row.findViewById(R.id.text_alert_event);
            when_alert_event = (TextView) row.findViewById(R.id.text_when_alert_event);
            type_event= (TextView) row.findViewById(R.id.text_type_event);
            edit= (Button) row.findViewById(R.id.button_edit);
            delete= (Button) row.findViewById(R.id.button_delete);
            DateTimeFormatter dtf = DateTimeFormat.forPattern("dd/MM/yyyy HH:mm:ss");
            try {
                name.setText(events_adapter[position].getString("name"));
                description.setText(events_adapter[position].getString("description"));
                start_event.setText(dtf.print(new DateTime(DateTime.parse(events_adapter[position].getString("start_event")))));
                end_event.setText(dtf.print(new DateTime(DateTime.parse(events_adapter[position].getString("end_event")))));
                repetition_event.setText(events_adapter[position].getString("repetition"));
                System.out.println(events_adapter[position].getString("when_repetition"));
                if (events_adapter[position].getString("when_repetition") != null && !events_adapter[position].getString("when_repetition").equals("null"))
                    when_repetition_event.setText(dtf.print(new DateTime(DateTime.parse(events_adapter[position].getString("when_repetition")))));
                else
                    when_repetition_event.setText("");
                when_alert_event.setText(events_adapter[position].getString("when_alert"));
                alert_event.setText(events_adapter[position].getString("alert"));
                type_event.setText(events_adapter[position].getString("type"));
                edit.setTag(events_adapter[position].getString("_id"));
                delete.setTag(events_adapter[position].getString("_id"));
            } catch (JSONException e) {
                e.printStackTrace();
            }

            edit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    editEvent(v);
                }
            });

            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    deleteEvent(v);
                }
            });

            return (row);
        }


        public void editEvent(View v){
            Log.i("boh", "editEvent method");
            Intent intent = new Intent(DailyEventsActivity.this, ModifyEventActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
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
}