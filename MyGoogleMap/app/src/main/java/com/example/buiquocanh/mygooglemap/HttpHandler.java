/*Acknoledgement: https://www.youtube.com/playlist?list=PLgCYzUzKIBE-vInwQhGSdnbyJ62nixHCt*/
package com.example.buiquocanh.mygooglemap;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class HttpHandler {

    public static String getJson(String urlStr) {
        HttpURLConnection httpURLConnection = null;
        StringBuilder stringBuilder = new StringBuilder();

        try {
            URL url = new URL(urlStr);
            httpURLConnection = (HttpURLConnection) url.openConnection();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));

            String line = "";
            while ((line = bufferedReader.readLine()) != null)
                stringBuilder.append(line);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stringBuilder.toString();
    }

    public static String postJson(String id, String leadPhysician, String impression, String content, String urlStr) {
        String status = "";
        try {
            URL url = new URL(urlStr);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
            httpURLConnection.setRequestProperty("Accept", "application/json");
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setDoInput(true);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("id", id);
            jsonObject.put("leadPhysician", leadPhysician);
            jsonObject.put("impression", impression);
            jsonObject.put("content", content);
            Log.d("JSON","JSON: " + jsonObject.toString());
            DataOutputStream dataOutputStream = new DataOutputStream(httpURLConnection.getOutputStream());
            dataOutputStream.writeBytes(jsonObject.toString());
            status = httpURLConnection.getResponseCode() + ": " + httpURLConnection.getResponseMessage();
            dataOutputStream.flush();
            dataOutputStream.close();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return status;
    }
}
