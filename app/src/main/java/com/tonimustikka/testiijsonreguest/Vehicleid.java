package com.tonimustikka.testiijsonreguest;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Mustikka on 03/11/17.
 */

public class Vehicleid implements Runnable{
    private volatile String data = null;
    private volatile JSONArray arr = null;

    private volatile ArrayList<String> shortName = new ArrayList<>();
    private volatile ArrayList<String> gtfsId = new ArrayList<>();

    @Override
    public void run() {
        StringBuffer buffer;
        try {
            URL url = new URL("https://api.digitransit.fi/routing/v1/routers/finland/index/graphql");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
            conn.setRequestProperty("Accept","application/json");
            conn.setDoOutput(true);
            conn.setDoInput(true);


            //{"query":"query routes{agency (id:\"HSL\") {routes {gtfsId, type, longName,shortName}}}","variables":null}

            JSONObject jsonParam = new JSONObject("{\"query\":\"query routes{agency (id:\\\"HSL\\\") {routes {gtfsId, type, longName,shortName}}}\",\"variables\":null}");

            //Log.i("JSON", jsonParam.toString());
            DataOutputStream os = new DataOutputStream(conn.getOutputStream());
            //os.writeBytes(URLEncoder.encode(jsonParam.toString(), "UTF-8"));
            os.writeBytes(jsonParam.toString());

            os.flush();
            os.close();



            InputStream stream = conn.getInputStream();

            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));

            buffer = new StringBuffer();

            String line;
            while ((line = reader.readLine()) != null){
                buffer.append(line);
            }


            //Log.i("STATUS", String.valueOf(conn.getResponseCode()));
            //Log.i("MSG" , conn.getResponseMessage());
            //Log.i("CONTENT" , buffer.toString());

            data = buffer.toString();

            conn.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }



        try {
            JSONObject obj = new JSONObject(data);
            arr = obj.getJSONObject("data").getJSONObject("agency").getJSONArray("routes");
        } catch (JSONException e) {
            e.printStackTrace();
        }




        for(int x = 0; x<arr.length(); x++) {
            try {
                shortName.add(arr.getJSONObject(x).getString("shortName"));
                gtfsId.add(arr.getJSONObject(x).getString("gtfsId"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public ArrayList<String> getNames() {
        return shortName;
    }
    public ArrayList<String> getGtfsIds() {
        return gtfsId;
    }
}
