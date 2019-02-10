package com.example.testapp;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

public class CachedList extends AppCompatActivity {
    ArrayList<String> cities = new ArrayList<>();
    ArrayList<String> jsonStrings = new ArrayList<>();

    ArrayAdapter arrayAdapter;
    SQLiteDatabase cachedData;

    public void clearAllCache (View view){
        cachedData.execSQL("DELETE FROM weather");
        updateList();
    }

    public void updateList() {
        Cursor c = cachedData.rawQuery("SELECT * FROM weather", null);
        int ciryIndex = c.getColumnIndex("city");
        int jsonStringIndex = c.getColumnIndex("jsonString");
        cities.clear();
        jsonStrings.clear();

        if (c.moveToFirst()){
            do {
                cities.add(c.getString(ciryIndex));
                jsonStrings.add(c.getString(jsonStringIndex));
            } while (c.moveToNext());

        }
        arrayAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cached_list);
        ListView listView = (ListView) findViewById(R.id.listView);
        arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, cities);
        listView.setAdapter(arrayAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(getApplicationContext(), WeatherForecastActivity.class);
                intent.putExtra("data", jsonStrings.get(i));
                intent.putExtra("isFromCache", true);
                startActivity(intent);
            }
        });
        cachedData = this.openOrCreateDatabase("Weather", MODE_PRIVATE, null);
        updateList();
    }
}
