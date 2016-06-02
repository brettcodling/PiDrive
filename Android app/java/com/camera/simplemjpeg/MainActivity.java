package com.camera.simplemjpeg;

import android.content.Context;
import android.util.Log;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Handler;
import android.view.MenuInflater;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URI;
import java.net.UnknownHostException;


public class MainActivity extends Activity {

    //Global declarations
    public Connection conn;
    private static final String TAG = "MJPEG";

    private MjpegView mv = null;
    String URL;

    // for settings (network and resolution)
    private static final int REQUEST_SETTINGS = 0;

    //Mjpeg size
    private int width = 640;
    private int height = 480;

    //IP and port
    private static int ip_ad1 = 172;
    private static int ip_ad2 = 20;
    private static int ip_ad3 = 10;
    private static int ip_ad4 = 3;
    private int ip_port = 8080;
    private String ip_command = "/?action=stream";
    private boolean suspending = false;
    final Handler handler = new Handler();

    TextView forwardBtn;
    TextView reverseBtn;
    TextView leftBtn;
    TextView rightBtn;
    private boolean moving;
    public CheckBox cruiseCBox;

    //Method called when view is created
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Creating URL for camera stream
        StringBuilder sb = new StringBuilder();
        String s_http = "http://";
        String s_dot = ".";
        String s_colon = ":";
        sb.append(s_http);
        sb.append(ip_ad1);
        sb.append(s_dot);
        sb.append(ip_ad2);
        sb.append(s_dot);
        sb.append(ip_ad3);
        sb.append(s_dot);
        sb.append(ip_ad4);
        sb.append(s_colon);
        sb.append(ip_port);
        sb.append(ip_command);
        URL = new String(sb);

        //Initializing video object
        setContentView(R.layout.activity_main);
        mv = (MjpegView)findViewById(R.id.mv);
        if (mv != null) {
            mv.setResolution(width, height);
        }
        setTitle(R.string.title_connecting);
        //Starting connection to video stream
        new DoRead().execute(URL);
        //Initializing connection for mobility controls
        conn = new Connection();
        conn.setIP(ip_ad1, ip_ad2, ip_ad3, ip_ad4);
        Log.d("result",conn.getIP());
        //Creating new thread and starting mobility connection
        final Thread t0 = new Thread(conn);
        t0.start();
        final Context context = getApplicationContext();
        final int duration = Toast.LENGTH_SHORT;

        cruiseCBox = (CheckBox)findViewById(R.id.cruiseCBox);
        forwardBtn = (TextView)findViewById(R.id.forwardBtn);

        //onClickListeners for control buttons
        forwardBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_POINTER_DOWN) {
                    //Passing character to Connection class
                    conn.send('1');
                    //Marking that the car is moving
                    moving = true;
                }
                if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_POINTER_UP) {
                    conn.send('2');
                    moving = false;
                }
                return true;
            }
        });
        reverseBtn = (TextView)findViewById(R.id.reverseBtn);
        reverseBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_POINTER_DOWN) {
                    conn.send('3');
                    moving = true;
                }
                else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_POINTER_UP) {
                    conn.send('2');
                    moving = false;
                }
                return true;
            }
        });
        leftBtn = (TextView)findViewById(R.id.leftBtn);
        leftBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_POINTER_DOWN) {
                    conn.send('6');
                }
                else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_POINTER_UP) {
                    conn.send('5');
                }
                return true;
            }
        });
        rightBtn = (TextView)findViewById(R.id.rightBtn);
        rightBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_POINTER_DOWN) {
                    conn.send('4');
                }
                else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_POINTER_UP) {
                    conn.send('5');
                }
                return true;
            }
        });
        //Button to try and reconnect if connection is lost
        TextView connectBtn = (TextView)findViewById(R.id.connectBtn);
        connectBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_POINTER_DOWN) {
                    if (!conn.getResult()) {
                        t0.run();
                    }
                    //If connection fails tell the user
                    response();
                }
                return true;
            }
        });
    }

    //Checks when speed radio buttons are checked
    public void onRadioButtonClicked(View view) {
        boolean checked = ((RadioButton)view).isChecked();
        switch(view.getId()) {
            case R.id.fastRBtn:
                if (checked)
                    //Sends data to connection to set speed
                    conn.send('7');
                break;
            case R.id.mediumRBtn:
                if (checked)
                    conn.send('9');
                break;
            case R.id.slowRBtn:
                if (checked)
                    conn.send('0');
                break;
        }
    }

    //Checks if cruise control checkbox is checked
    public void onCheckBoxSelect (View view) {
        boolean checked = ((CheckBox)view).isChecked();
        if (checked) {
            if (moving) {
                //Checks checkbox if a movement button is pressed
                conn.send('a');
            } else {
                cruiseCBox.setChecked(false);
            }
        } else if (!checked) {
            if (moving) {
                //Unchecks box but doesn't stop motors because movement button is pressed
                conn.send('c');
            } else {
                //Unchecks box and stops motors
                conn.send('b');
            }
        }
    }

    //Sets response text box value
    public void response() {
        final TextView response = (TextView)findViewById(R.id.response);
        if (!conn.getResult()) {
            response.setText("Not connected");
        }else {
            response.setText("Connected to: " + conn.getIP());
        }
    }

    //Sets IP from log in
    public static void setIP(int ip_1, int ip_2, int ip_3, int ip_4) {
        ip_ad1 = ip_1;
        ip_ad2 = ip_2;
        ip_ad3 = ip_3;
        ip_ad4 = ip_4;
    }

    //Handles resuming of app after pause or stop
    public void onResume() {
        conn.run();
        RadioButton rb = (RadioButton)findViewById(R.id.slowRBtn);
        rb.setSelected(true);
        super.onResume();
        if (mv != null) {
            if (suspending) {
                new DoRead().execute(URL);
                suspending = false;
            }
        }

    }

    //Handles back button presses
    public void onBackPressed() {
        conn.send('8');
        super.onBackPressed();
        if (mv != null) {
            if (mv.isStreaming()) {
                mv.stopPlayback();
                suspending = true;
            }
        }
    }

    //Handles pausing
    public void onPause() {
        conn.send('8');
        super.onPause();
        if (mv != null) {
            if (mv.isStreaming()) {
                mv.stopPlayback();
                suspending = true;
            }
        }
    }

    //Handles stopping
    public void onStop() {
        conn.send('8');
        super.onStop();
        if (mv != null) {
            if (mv.isStreaming()) {
                mv.stopPlayback();
                suspending = true;
            }
        }
    }

    //Handles app being destroyed i.e. closed
    public void onDestroy() {
        conn.send('8');
        if (mv != null) {
            mv.freeCameraMemory();
        }
        super.onDestroy();
    }

    //Initializes options menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.layout.option_menu, menu);
        return true;
    }

    //Runs when options menu is selected and sends in IP data
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings:
                Intent settings_intent = new Intent(MainActivity.this, SettingsActivity.class);
                settings_intent.putExtra("ip_ad1", ip_ad1);
                settings_intent.putExtra("ip_ad2", ip_ad2);
                settings_intent.putExtra("ip_ad3", ip_ad3);
                settings_intent.putExtra("ip_ad4", ip_ad4);
                startActivityForResult(settings_intent, REQUEST_SETTINGS);
                return true;
        }
        return false;
    }

    //Handles result returned from an activity such as options menu
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_SETTINGS:
                if (resultCode == Activity.RESULT_OK) {
                    //Sets array values
                    width = data.getIntExtra("width", width);
                    height = data.getIntExtra("height", height);
                    ip_ad1 = data.getIntExtra("ip_ad1", ip_ad1);
                    ip_ad2 = data.getIntExtra("ip_ad2", ip_ad2);
                    ip_ad3 = data.getIntExtra("ip_ad3", ip_ad3);
                    ip_ad4 = data.getIntExtra("ip_ad4", ip_ad4);
                    ip_port = data.getIntExtra("ip_port", ip_port);
                    ip_command = data.getStringExtra("ip_command");

                    if (mv != null) {
                        mv.setResolution(width, height);
                    }
                    SharedPreferences preferences = getSharedPreferences("SAVED_VALUES", MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putInt("width", width);
                    editor.putInt("height", height);
                    editor.putInt("ip_ad1", ip_ad1);
                    editor.putInt("ip_ad2", ip_ad2);
                    editor.putInt("ip_ad3", ip_ad3);
                    editor.putInt("ip_ad4", ip_ad4);
                    editor.putInt("ip_port", ip_port);
                    editor.putString("ip_command", ip_command);

                    editor.commit();

                    new RestartApp().execute();
                }
                break;
        }
    }

    //Shows error in title bar when images cant be processed i.e. invalid address
    public void setImageError() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                setTitle(R.string.title_imageerror);
                return;
            }
        });
    }

    //Creates connection to video stream
    public class DoRead extends AsyncTask<String, Void, MjpegInputStream> {
        //Sets connection ready to run
        protected MjpegInputStream doInBackground(String... url) {
            HttpResponse res = null;
            DefaultHttpClient httpclient = new DefaultHttpClient();
            HttpParams httpParams = httpclient.getParams();
            HttpConnectionParams.setConnectionTimeout(httpParams, 5 * 1000);
            HttpConnectionParams.setSoTimeout(httpParams, 5 * 1000);
            try {
                res = httpclient.execute(new HttpGet(URI.create(url[0])));
                if (res.getStatusLine().getStatusCode() == 401) {
                    return null;
                }
                return new MjpegInputStream(res.getEntity().getContent());
            }
            catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        //Runs after connection is set and shows connection in view
        protected void onPostExecute(MjpegInputStream result) {
            mv.setSource(result);
            if (result != null) {
                result.setSkip(1);
                setTitle(R.string.app_name);
            } else {
                setTitle(R.string.title_disconnected);
            }
            mv.setDisplayMode(MjpegView.SIZE_BEST_FIT);
            //Chose whether to show FPS
            mv.showFps(false);
        }
    }

    //Restarts app
    public class RestartApp extends AsyncTask<Void, Void, Void> {
        protected Void doInBackground(Void... v) {
            //Closes activity
            MainActivity.this.finish();
            return null;
        }

        protected void onPostExecute(Void v) {
            //Starts activity
            startActivity((new Intent(MainActivity.this, MainActivity.class)));
        }
    }
}