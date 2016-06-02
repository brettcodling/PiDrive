package com.camera.simplemjpeg;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView.BufferType;
import android.widget.Toast;

public class SettingsActivity extends Activity {


    //Global declarations
    Button settings_done;

    EditText address1_input;
    EditText address2_input;
    EditText address3_input;
    EditText address4_input;

    Button address1_increment;
    Button address2_increment;
    Button address3_increment;
    Button address4_increment;

    Button address1_decrement;
    Button address2_decrement;
    Button address3_decrement;
    Button address4_decrement;

    int ip_ad1;
    int ip_ad2;
    int ip_ad3;
    int ip_ad4;

    //Method called when view is created
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);

        //Getting arrays
        Bundle extras = getIntent().getExtras();

        final Context context = getApplicationContext();
        final int duration = Toast.LENGTH_SHORT;

        //Initializing components
        address1_input = (EditText) findViewById(R.id.address1_input);
        address2_input = (EditText) findViewById(R.id.address2_input);
        address3_input = (EditText) findViewById(R.id.address3_input);
        address4_input = (EditText) findViewById(R.id.address4_input);

        //Setting values from array
        if (extras != null) {
            ip_ad1 = extras.getInt("ip_ad1", ip_ad1);
            ip_ad2 = extras.getInt("ip_ad2", ip_ad2);
            ip_ad3 = extras.getInt("ip_ad3", ip_ad3);
            ip_ad4 = extras.getInt("ip_ad4", ip_ad4);

            address1_input.setText(String.valueOf(ip_ad1));
            address2_input.setText(String.valueOf(ip_ad2));
            address3_input.setText(String.valueOf(ip_ad3));
            address4_input.setText(String.valueOf(ip_ad4));
        }

        //onClickListeners for IP buttons
        address1_increment = (Button) findViewById(R.id.address1_increment);
        address1_increment.setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View view) {
                        //Set s to value in text box
                        String s = address1_input.getText().toString();
                        int val = ip_ad1;
                        //Check s is not empty
                        if (!"".equals(s)) {
                            val = Integer.parseInt(s);
                        }
                        //If value is 0 or bigger but less than 255 then add 1
                        if (val >= 0 && val < 255) {
                            val += 1;
                        //If value is less than 0 set it to 0
                        } else if (val < 0) {
                            val = 0;
                        //If value is more than or equal to 255 set it to 255
                        } else if (val >= 255) {
                            val = 255;
                        }

                        ip_ad1 = val;
                        //Set text box value to new value
                        address1_input.setText(String.valueOf(ip_ad1), BufferType.NORMAL);

                    }
                }
        );
        address2_increment = (Button) findViewById(R.id.address2_increment);
        address2_increment.setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View view) {
                        String s = address2_input.getText().toString();
                        int val = ip_ad2;
                        if (!"".equals(s)) {
                            val = Integer.parseInt(s);
                        }
                        if (val >= 0 && val < 255) {
                            val += 1;
                        } else if (val < 0) {
                            val = 0;
                        } else if (val >= 255) {
                            val = 255;
                        }

                        ip_ad2 = val;
                        address2_input.setText(String.valueOf(ip_ad2), BufferType.NORMAL);

                    }
                }
        );
        address3_increment = (Button) findViewById(R.id.address3_increment);
        address3_increment.setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View view) {
                        String s = address3_input.getText().toString();
                        int val = ip_ad3;
                        if (!"".equals(s)) {
                            val = Integer.parseInt(s);
                        }
                        if (val >= 0 && val < 255) {
                            val += 1;
                        } else if (val < 0) {
                            val = 0;
                        } else if (val >= 255) {
                            val = 255;
                        }

                        ip_ad3 = val;
                        address3_input.setText(String.valueOf(ip_ad3), BufferType.NORMAL);

                    }
                }
        );
        address4_increment = (Button) findViewById(R.id.address4_increment);
        address4_increment.setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View view) {
                        String s = address4_input.getText().toString();
                        int val = ip_ad4;
                        if (!"".equals(s)) {
                            val = Integer.parseInt(s);
                        }
                        if (val >= 0 && val < 255) {
                            val += 1;
                        } else if (val < 0) {
                            val = 0;
                        } else if (val >= 255) {
                            val = 255;
                        }

                        ip_ad4 = val;
                        address4_input.setText(String.valueOf(ip_ad4), BufferType.NORMAL);

                    }
                }
        );

        address1_decrement = (Button) findViewById(R.id.address1_decrement);
        address1_decrement.setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View view) {
                        String s = address1_input.getText().toString();
                        int val = ip_ad1;
                        if (!"".equals(s)) {
                            val = Integer.parseInt(s);
                        }
                        if (val > 0 && val <= 255) {
                            val -= 1;
                        } else if (val <= 0) {
                            val = 0;
                        } else if (val > 255) {
                            val = 255;
                        }

                        ip_ad1 = val;
                        address1_input.setText(String.valueOf(ip_ad1), BufferType.NORMAL);

                    }
                }
        );

        address2_decrement = (Button) findViewById(R.id.address2_decrement);
        address2_decrement.setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View view) {
                        String s = address2_input.getText().toString();
                        int val = ip_ad2;
                        if (!"".equals(s)) {
                            val = Integer.parseInt(s);
                        }
                        if (val > 0 && val <= 255) {
                            val -= 1;
                        } else if (val <= 0) {
                            val = 0;
                        } else if (val > 255) {
                            val = 255;
                        }

                        ip_ad2 = val;
                        address2_input.setText(String.valueOf(ip_ad2), BufferType.NORMAL);

                    }
                }
        );
        address3_decrement = (Button) findViewById(R.id.address3_decrement);
        address3_decrement.setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View view) {
                        String s = address3_input.getText().toString();
                        int val = ip_ad3;
                        if (!"".equals(s)) {
                            val = Integer.parseInt(s);
                        }
                        if (val > 0 && val <= 255) {
                            val -= 1;
                        } else if (val <= 0) {
                            val = 0;
                        } else if (val > 255) {
                            val = 255;
                        }

                        ip_ad3 = val;
                        address3_input.setText(String.valueOf(ip_ad3), BufferType.NORMAL);

                    }
                }
        );
        address4_decrement = (Button) findViewById(R.id.address4_decrement);
        address4_decrement.setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View view) {
                        String s = address4_input.getText().toString();
                        int val = ip_ad4;
                        if (!"".equals(s)) {
                            val = Integer.parseInt(s);
                        }
                        if (val > 0 && val <= 255) {
                            val -= 1;
                        } else if (val <= 0) {
                            val = 0;
                        } else if (val > 255) {
                            val = 255;
                        }

                        ip_ad4 = val;
                        address4_input.setText(String.valueOf(ip_ad4), BufferType.NORMAL);

                    }
                }
        );

        settings_done = (Button) findViewById(R.id.settings_done);
        settings_done.setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View view) {

                        String s;

                        //Setting values to send to next activity
                        s = address1_input.getText().toString();
                        if (!"".equals(s)) {
                            ip_ad1 = Integer.parseInt(s);
                        } else {
                            ip_ad1 = -1;
                        }
                        s = address2_input.getText().toString();
                        if (!"".equals(s)) {
                            ip_ad2 = Integer.parseInt(s);
                        } else {
                            ip_ad2 = -1;
                        }
                        s = address3_input.getText().toString();
                        if (!"".equals(s)) {
                            ip_ad3 = Integer.parseInt(s);
                        } else {
                            ip_ad3 = -1;
                        }
                        s = address4_input.getText().toString();
                        if (!"".equals(s)) {
                            ip_ad4 = Integer.parseInt(s);
                        } else {
                            ip_ad4 = -1;
                        }

                        //Validates input values
                        if (ip_ad1 < 0 || ip_ad1 > 255 || ip_ad2 < 0 || ip_ad2 > 255 || ip_ad3 < 0 || ip_ad3 > 255 || ip_ad4 < 0 || ip_ad4 > 255) {
                            Toast toast = Toast.makeText(context, "Values must be between 0 and 255 inclusive", duration);
                            toast.show();
                        } else {
                            //Setting new values in array to send back to MainActivity
                            Intent intent = new Intent();
                            intent.putExtra("ip_ad1", ip_ad1);
                            intent.putExtra("ip_ad2", ip_ad2);
                            intent.putExtra("ip_ad3", ip_ad3);
                            intent.putExtra("ip_ad4", ip_ad4);

                            setResult(RESULT_OK, intent);
                            //Ending activity
                            finish();
                        }
                    }
                }
        );
    }
}
