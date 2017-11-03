package com.tonimustikka.testiijsonreguest;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FloatingActionButton btnHit = (FloatingActionButton) findViewById(R.id.btnHit);
        final EditText lineNum = (EditText) findViewById(R.id.lineNum);


        final Spinner typeSpinner = (Spinner) findViewById(R.id.type_spinner);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.type_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        typeSpinner.setAdapter(adapter);


        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapView);
        mapFragment.getMapAsync(this);

        btnHit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {



                String line = lineNum.getText().toString();
                if (line != null) {
                    Log.d("sa", typeSpinner.getSelectedItem().toString());
                    new JSONTask().execute("https://api.digitransit.fi/realtime/vehicle-positions/v1/hfp/journey/" + typeSpinner.getSelectedItem().toString() + "/+/" + line + "/");
                } else {
                    new JSONTask().execute("https://api.digitransit.fi/realtime/vehicle-positions/v1/hfp/journey/" + typeSpinner.getSelectedItem().toString() + "/+/+/");
                }


                Log.d("Deebug", "Worked!!");
            }
        });

    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 200);
        }
        mMap.setMyLocationEnabled(true);
        // Add a marker in Sydney and move the camera
        LatLng helsinki = new LatLng(60.169856,24.938379);
        //mMap.addMarker(new MarkerOptions().position(helsinki).title("Marker in helsinki"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(helsinki, (float) 12.5));
    }


    public class JSONTask extends AsyncTask<String,String, String > {

        @Override
        protected String doInBackground(String... params) {
            HttpURLConnection connection = null;
            BufferedReader reader = null;
            try {
                URL url = new URL(params[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                InputStream stream = connection.getInputStream();

                reader = new BufferedReader(new InputStreamReader(stream));

                StringBuffer buffer = new StringBuffer();

                String line = "";
                while ((line = reader.readLine()) != null){
                    buffer.append(line);
                }
                Log.d("Deebug", "Hi");
                return buffer.toString();

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if(connection != null) {connection.disconnect();}
                try {
                    if (reader != null){reader.close();}
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            mMap.clear();
            try {
                JSONObject reader = new JSONObject(result);
                JSONArray vehicles = reader.names();
                if (vehicles != null) {
                    for (int i = 0; i < vehicles.length(); i++) {
                        JSONObject x = reader.getJSONObject(vehicles.get(i).toString()).getJSONObject("VP");


                        try {
                            LatLng temp = new LatLng(x.getDouble("lat"), x.getDouble("long"));
                            String desi = x.getString("desi");

                            String start = null;
                            try {
                                start = x.getString("start");
                            } catch (JSONException e) {
                                //e.printStackTrace();
                            }

                            String dly = null;
                            try {
                                dly = x.getString("dl");
                            } catch (JSONException e) {
                                //e.printStackTrace();
                            }

                            String snippet = start + " | " + dly;

                            mMap.addMarker(new MarkerOptions().position(temp).title(desi).snippet(snippet));
                        } catch (JSONException e) {
                            //e.printStackTrace();
                        }


                    }
                }




            } catch (JSONException e) {
                e.printStackTrace();
            }


            Log.d("Deebug", "Updated!");
        }
    }




}

