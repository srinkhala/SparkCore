package com.example.android.healthybee;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity {
        // Set actvity name as debug tag
        public static final String TAG = HttpsClient.class.getSimpleName();

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);

            // Declare and assign our buttons and text
            final Button getButton = (Button) findViewById(R.id.getButton);
            final Button postButton = (Button) findViewById(R.id.postButton);

            View.OnClickListener getListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Update the <> with your CoreID and your AccessToken from https://build.spark.io
                    Toast.makeText(MainActivity.this, "GET foobar", Toast.LENGTH_SHORT).show();
                    new HttpsClient().execute("https://api.spark.io/v1/devices/53ff74065075535130461187/events/?access_token=58f985ac4413fe05a5b9b985e2ab8142e359b4cc");
                }
            };
            getButton.setOnClickListener(getListener);

            View.OnClickListener postListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Post d7,HIGH to spark core
                    Toast.makeText(MainActivity.this, "POST d7,HIGH", Toast.LENGTH_SHORT).show();
                    new PostClient().execute("HIGH");
                }
            };
            postButton.setOnClickListener(postListener);


        }


        /*
         * GET EXAMPLE
         */
        class HttpsClient extends AsyncTask<String, Void, String> {
            private Exception exception;
            public String doInBackground(String... urls) {

                try {
                    Log.d(TAG, "*******************    Open Connection    *****************************");
                    URL url = new URL(urls[0]);
                    Log.d(TAG, "Received URL:  " + url);

                    HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
                    Log.d(TAG, "Con Status: " + con);

                    InputStream in = con.getInputStream();
                    Log.d(TAG, "GetInputStream:  " + in);

                    Log.d(TAG, "*******************    String Builder     *****************************");
                    String line = null;

                    BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));

                    Data data = new Data();

                    while ((line = br.readLine()) != null) {
                        if (line.contains("event")) {
                            //do nothing since the event tag is of no interest
                            Log.d(TAG, "Failed fetching needed values.");
                            return null;
                        }
                        if (line.contains("data: ")) {
                            //convert to JSON (stripping the beginning "data: "
                            JSONObject jObject = new JSONObject(line.substring(6));
                            String json_data = (String) jObject.get("data");
                            //convert again
                            jObject = new JSONObject(json_data);

                            //reading photocell
                            if (jObject.has("baz")) {
                                data.setFoobar(jObject.getString("baz"));
                            }

                        }
                        //check if we have all needed data
                        if (data.isReady()) {
                            //exit endless connection
                            Log.d(TAG, "*******************    Data received    *****************************");
                            Log.d(TAG, "data:  " + data);
                            break;
                        }
                    }

                    // Creation of finalized containers for UI usage
                    final String gBaz = data.getFoobar();
                    // To access the findViewById we need this to runOnUiThread
                    runOnUiThread(new Runnable(){
                        public void run() {
                            Log.d(TAG, "*******************    Run UI Thread     *****************************");
                            Log.d(TAG, "gFoobar:   " + gBaz);
                            // Assign and declare
                            final TextView updateGetExample  = (TextView) findViewById(R.id.getTextView);
                            // Update the TextViews
                            Log.d(TAG, "*******************    Update TextView       *************************");
                            updateGetExample.setText(gBaz);

                        }

                    });
                    // Closing the stream
                    Log.d(TAG, "*******************  Stream closed, exiting     ******************************");
                    br.close();
                } catch (Exception e) {
                    this.exception = e;
                    return null;
                }
                return null; }

        }

        /*
         * POST EXAMPLE
         */
        // We must do this as a background task, elsewhere our app crashes
        class PostClient extends AsyncTask<String, Void, String> {
            public String doInBackground(String... IO) {

                // Predefine variables
                String io = new String(IO[0]);
                URL url;

                try {
                    // Stuff variables
                    url = new URL("https://api.spark.io/v1/devices/53ff74065075535130461187/SCL/");
                    String param = "access_token=58f985ac4413fe05a5b9b985e2ab8142e359b4cc&params=d7,"+io;
                    Log.d(TAG, "param:" + param);

                    // Open a connection using HttpURLConnection
                    HttpsURLConnection con = (HttpsURLConnection) url.openConnection();

                    con.setReadTimeout(7000);
                    con.setConnectTimeout(7000);
                    con.setDoOutput(true);
                    con.setDoInput(true);
                    con.setInstanceFollowRedirects(false);
                    con.setRequestMethod("POST");
                    con.setFixedLengthStreamingMode(param.getBytes().length);
                    con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                    // Send
                    PrintWriter out = new PrintWriter(con.getOutputStream());
                    out.print(param);
                    out.close();

                    con.connect();

                    BufferedReader in = null;
                    if (con.getResponseCode() != 200) {
                        in = new BufferedReader(new InputStreamReader(con.getErrorStream()));
                        Log.d(TAG, "!=200: " + in);
                    } else {
                        in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                        Log.d(TAG, "POST request send successful: " + in);
                    };


                } catch (Exception e) {
                    Log.d(TAG, "Exception");
                    e.printStackTrace();
                    return null;
                }
                // Set null and we´e good to go
                return null;
            }
        }
    }
