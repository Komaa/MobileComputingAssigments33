package com.example.koma.straxcalendar;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashSet;

/**
 * Created by Koma on 28/11/15.
 */
public class DailyEventsActivity extends Activity {

    ListView l1;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dailyevents_activity);
        l1=(ListView)findViewById(R.id.list);
        l1.setAdapter(new dataListAdapter(CalendarActivity.events));

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
            System.out.println("qui");
            LayoutInflater inflater = getLayoutInflater();
            View row;
            row = inflater.inflate(R.layout.events_display, parent, false);
            TextView name, description;
            name = (TextView) row.findViewById(R.id.text_name_event);
            description = (TextView) row.findViewById(R.id.text_description_event);
            try {
                name.setText(events_adapter[position].getString("name"));
                description.setText(events_adapter[position].getString("description"));
                System.out.println(events_adapter[position].getString("description"));
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return (row);
        }
    }
}