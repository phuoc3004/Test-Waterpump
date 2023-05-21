package com.example.phuoc_water1;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private static final String ESP8266_IP_ADDRESS = "192.168.1.218";  // Replace with your ESP8266 IP address

    private Button btnOn;
    private Button btnOff;
    private Button btn2Seconds;
    private Button btn5Seconds;
    private Button btn10Seconds;
    private TextView tvSensorValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnOn = findViewById(R.id.btnOn);
        btnOff = findViewById(R.id.btnOff);
        btn2Seconds = findViewById(R.id.btn2Seconds);
        btn5Seconds = findViewById(R.id.btn5Seconds);
        btn10Seconds = findViewById(R.id.btn10Seconds);
        tvSensorValue = findViewById(R.id.tvSensorValue);

        btnOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendRequest("/on");
            }
        });

        btnOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendRequest("/off");
            }
        });

        btn2Seconds.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendRequest("/2seconds");
            }
        });

        btn5Seconds.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendRequest("/5seconds");
            }
        });

        btn10Seconds.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendRequest("/10seconds");
            }
        });

        startSensorValueUpdates();
    }

    private void sendRequest(String endpoint) {
        String url = "http://" + ESP8266_IP_ADDRESS + endpoint;
        new HttpRequestTask().execute(url);
    }

    private void startSensorValueUpdates() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        Thread.sleep(1000);  // Update every 1 second
                        String url = "http://" + ESP8266_IP_ADDRESS + "/sensor";
                        new HttpRequestTask().execute(url);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    private class HttpRequestTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            if (params.length == 0)
                return null;

            String urlString = params[0];
            StringBuilder result = new StringBuilder();

            try {
                URL url = new URL(urlString);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }

                reader.close();
                connection.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return result.toString();
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                if (result.startsWith("Moisture Level:")) {
                    String sensorValue = result.replace("Moisture Level:", "").trim();
                    tvSensorValue.setText("Sensor Value: " + sensorValue);
                }
            }
        }
    }
}
