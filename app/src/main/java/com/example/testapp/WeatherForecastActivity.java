package com.example.testapp;


import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WeatherForecastActivity extends AppCompatActivity {

    private ArrayList<String> dates = new ArrayList<>();
    private ArrayList<String> weathers = new ArrayList<>();
    private Boolean isFromCache = false;
    SQLiteDatabase cachedData;

    private void jsonParse (String jsonString) {
        TextView cityTextView = findViewById(R.id.cityTextView);
        Pattern pattern = Pattern.compile("^(.*-(\\d{2})) (.{5})");
        Integer dateForCompare = null;
        StringBuilder stringForWeatherFill = new StringBuilder();
        try {
            JSONObject json = new JSONObject(jsonString);
            JSONObject city = json.getJSONObject("city");
            Log.i("Weather content", city.getString("name"));
            if (!isFromCache) {
                String sql = "INSERT INTO weather (city, jsonString) VALUES (?, ?)";
                SQLiteStatement statement = cachedData.compileStatement(sql);
                statement.bindString(1, city.getString("name"));
                statement.bindString(2, jsonString);
                statement.execute();
            }
            cityTextView.setText(city.getString("name"));
            JSONArray arr = json.getJSONArray("list");
            for (int i = 0; i < arr.length(); i++) {
                JSONObject list = arr.getJSONObject(i);
                Matcher matcher = pattern.matcher(list.getString("dt_txt"));
                matcher.find();
                if (dateForCompare == null || dateForCompare != Integer.parseInt(matcher.group(2))) {
                    if (dateForCompare != null){
                        dates.add(matcher.group(1));
                        weathers.add(stringForWeatherFill.toString());
                    }
                    stringForWeatherFill.setLength(0);
                    dateForCompare = Integer.parseInt(matcher.group(2));
                    stringForWeatherFill.append(matcher.group(3))
                            .append("\n")
                            .append(list.getJSONArray("weather").getJSONObject(0).getString("description"))
                            .append("\n")
                            .append("Temp ").append(String.format("%1.0f", list.getJSONObject("main").getDouble("temp")))
                            .append("\u2103"+"\n").append("Pressure ")
                            .append(String.format("%1.0f", list.getJSONObject("main").getDouble("grnd_level")))
                            .append(" hPa\n").append("Humidity ")
                            .append(list.getJSONObject("main").getString("humidity"))
                            .append("%\n")
                            .append("Wind speed ").append(String.format("%1.0f", list.getJSONObject("wind").getDouble("speed")))
                            .append(" m/s\n\n");
                } else {
                    stringForWeatherFill.append(matcher.group(3))
                            .append("\n")
                            .append(list.getJSONArray("weather").getJSONObject(0).getString("description"))
                            .append("\n")
                            .append("Temp ").append(String.format("%1.0f", list.getJSONObject("main").getDouble("temp")))
                            .append("\u2103"+"\n").append("Pressure ")
                            .append(String.format("%1.0f", list.getJSONObject("main").getDouble("grnd_level")))
                            .append(" hPa\n").append("Humidity ")
                            .append(list.getJSONObject("main").getString("humidity"))
                            .append("%\n")
                            .append("Wind speed ").append(String.format("%1.0f", list.getJSONObject("wind").getDouble("speed")))
                            .append(" m/s\n\n");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        initRecyclerView();

    }

    private void initRecyclerView(){
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(layoutManager);
        RecyclerViewAdapter adapter = new RecyclerViewAdapter(this, dates, weathers);
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather_forecast);
        Intent intent = getIntent();
        String data = intent.getStringExtra("data");
        isFromCache = intent.getBooleanExtra("isFromCache", false);
        cachedData = this.openOrCreateDatabase("Weather", MODE_PRIVATE, null);
        cachedData.execSQL("CREATE TABLE IF NOT EXISTS weather (city VARCHAR, jsonString VARCHAR)");
        jsonParse(data);
    }
}
