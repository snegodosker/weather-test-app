package com.example.testapp;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


// openweatherapikey = dcd6dac2cd77a84783894270dc26d6eb


public class MainActivity extends AppCompatActivity implements OnMapClickListener,OnMapReadyCallback {
    private GoogleMap mMap;
    private LatLng weatherPoint;
    SharedPreferences sharedPreferences;
    private Boolean startFromLastPoint = true;
    private static final String API_URL = "http://api.openweathermap.org/data/2.5/forecast?lat=%s&lon=%s&units=metric&APPID=dcd6dac2cd77a84783894270dc26d6eb";


    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(getApplicationContext().CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }

    public void showforecast (View viev){
        if (weatherPoint != null) {
            DownloadWeather downloadWeather = new DownloadWeather();
            Double lat = weatherPoint.latitude;
            Double lng = weatherPoint.longitude;
            sharedPreferences.edit().putString("lat", lat.toString()).apply();
            sharedPreferences.edit().putString("lng", lng.toString()).apply();
            if (isNetworkConnected()) {
                downloadWeather.execute(String.format(API_URL, lat.toString(), lng.toString()));
            } else {
                Toast.makeText(getApplicationContext(),"Have not network access", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getApplicationContext(), "Please select point on map", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        sharedPreferences = this.getSharedPreferences("com.example.testapp", Context.MODE_PRIVATE);
        startFromLastPoint = sharedPreferences.getBoolean("startFromLastPoint", true);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case R.id.lastPoint:
                new AlertDialog.Builder(this)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle("Start from last point")
                        .setMessage("Starting app with last searched point")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                startFromLastPoint = true;
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                startFromLastPoint = false;
                                sharedPreferences.edit().putBoolean("startFromLastPoint", false).apply();
                            }
                        }).show();
                break;
            case R.id.cachedData:
                Intent intent = new Intent(getApplicationContext(), CachedList.class);
                startActivity(intent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMapClickListener(this);
        if (startFromLastPoint) {
            String lat = sharedPreferences.getString("lat", "");
            String lng = sharedPreferences.getString("lng", "");
            if (lat != "" && lng != "") {
                LatLng point = new LatLng(Double.parseDouble(lat), Double.parseDouble(lng));
                mMap.addMarker(new MarkerOptions().position(point));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(point, 6));
            }
        }
    }

    @Override
    public void onMapClick(LatLng point) {
        mMap.clear();
        mMap.addMarker(new MarkerOptions().position(point));
        weatherPoint = point;
    }

    public class DownloadWeather extends AsyncTask<String, Integer, String> {
        private ProgressDialog progressDialog;

        @Override
        protected String doInBackground(String... urls) {
            String result = "";
            URL url;
            HttpURLConnection urlConnection;
            try {
                url = new URL(urls[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setConnectTimeout(5000);
                int lof = urlConnection.getContentLength();
                InputStream in = urlConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(in);
                int data = reader.read();
                long total = 0;
                while (data != -1) {
                    char current = (char) data;
                    result += current;
                    total += data;
                    data = reader.read();
                    publishProgress((int) (total*100 / lof));
                }
                in.close();
                return result;
            } catch (Exception e) {
                e.printStackTrace();
            }


            return null;
        }


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            this.progressDialog = new ProgressDialog(MainActivity.this);
            this.progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            this.progressDialog.setCancelable(false);
            this.progressDialog.show();
        }


        protected void onProgressUpdate(Integer... progress){
            super.onProgressUpdate();
            progressDialog.setProgress(progress[0]);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            this.progressDialog.dismiss();
            if (s == null){
                Toast.makeText(getApplicationContext(), "Can't connect to server, try later or use cached data", Toast.LENGTH_LONG).show();
            }else if (!s.equals("")) {
                Intent intent = new Intent(getApplicationContext(), WeatherForecastActivity.class);
                intent.putExtra("data", s);
                startActivity(intent);
            }

        }
    }

}
