package com.sebastiendui.mobitimego.screens;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.sebastiendui.mobitimego.R;
import com.sebastiendui.mobitimego.core.BaseActivity;

/**
 * Class for Keyboard Screen
 */
public class RFIDbadgeKeypadD extends BaseActivity implements View.OnClickListener {

    private static final String TAG = "RFID Keypad";
    private TextView oldNumberEditText;

    //string, which we see in EditText
    private StringBuilder stringBuilder = new StringBuilder(10);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.rfidbadge_keypad_d);

        //digits

        findViewById(R.id.button_one).setOnClickListener(this);
        findViewById(R.id.button_two).setOnClickListener(this);
        findViewById(R.id.button_three).setOnClickListener(this);
        findViewById(R.id.button_four).setOnClickListener(this);
        findViewById(R.id.button_five).setOnClickListener(this);
        findViewById(R.id.button_six).setOnClickListener(this);
        findViewById(R.id.button_seven).setOnClickListener(this);
        findViewById(R.id.button_eight).setOnClickListener(this);
        findViewById(R.id.button_nine).setOnClickListener(this);
        findViewById(R.id.button_zero).setOnClickListener(this);

        //chars

        findViewById(R.id.button_A).setOnClickListener(this);
        findViewById(R.id.button_B).setOnClickListener(this);
        findViewById(R.id.button_C).setOnClickListener(this);
        findViewById(R.id.button_D).setOnClickListener(this);
        findViewById(R.id.button_E).setOnClickListener(this);
        findViewById(R.id.button_F).setOnClickListener(this);


        //edit text
        oldNumberEditText = findViewById(R.id.type_edit_text);

        //cancel button
        findViewById(R.id.rfid_cancel_button).setOnClickListener(this);

        //delete button
        findViewById(R.id.rfid_delete_button).setOnClickListener(this);

        //ok button
        findViewById(R.id.rfid_ok_button).setOnClickListener(this);

    }


    @Override
    public void onClick(View v) {

        int id = v.getId();

        if (stringBuilder.length() < 10) {
            switch (id) {

                //digits
                case R.id.button_one:
                    stringBuilder.append(getString(R.string._1));
                    break;
                case R.id.button_two:
                    stringBuilder.append(getString(R.string._2));
                    break;

                case R.id.button_three:
                    stringBuilder.append(getString(R.string._3));
                    break;
                case R.id.button_four:
                    stringBuilder.append(getString(R.string._4));
                    break;
                case R.id.button_five:
                    stringBuilder.append(getString(R.string._5));
                    break;
                case R.id.button_six:
                    stringBuilder.append(getString(R.string._6));
                    break;
                case R.id.button_seven:
                    stringBuilder.append(getString(R.string._7));
                    break;
                case R.id.button_eight:
                    stringBuilder.append(getString(R.string._8));
                    break;
                case R.id.button_nine:
                    stringBuilder.append(getString(R.string._9));
                    break;
                case R.id.button_zero:
                    stringBuilder.append(getString(R.string._0));
                    break;

                //chars
                case R.id.button_A:
                    stringBuilder.append(getString(R.string.A));
                    break;
                case R.id.button_B:
                    stringBuilder.append(getString(R.string.B));
                    break;
                case R.id.button_C:
                    stringBuilder.append(getString(R.string.C));
                    break;
                case R.id.button_D:
                    stringBuilder.append(getString(R.string.D));
                    break;
                case R.id.button_E:
                    stringBuilder.append(getString(R.string.E));
                    break;
                case R.id.button_F:
                    stringBuilder.append(getString(R.string.F));
                    break;

                default:
                    break;


            }


        }

        //delete char

        if (stringBuilder.length() > 0 && id == R.id.rfid_delete_button) {

            stringBuilder.deleteCharAt(stringBuilder.length() - 1);

        }

        //cancel button

        if (id == R.id.rfid_cancel_button) {

            Intent intent = new Intent(this, MainScreenB.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                    | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);

        }

        //ok button

        if (id == R.id.rfid_ok_button) {

            Intent intent = new Intent(this, MainScreenB.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                    | Intent.FLAG_ACTIVITY_SINGLE_TOP);

            String oldNumber = stringBuilder.toString();
            intent.putExtra(getString(R.string.rfidref_json), oldNumber);
            intent.putExtra(getString(R.string.data_read_json), getIntent().getStringExtra(getString(R.string.data_read_json)));
            intent.putExtra(getString(R.string.direction_json), getIntent().getStringExtra(getString(R.string.direction_json)));
            intent.putExtra(getString(R.string.data_source_json), getIntent().getStringExtra(getString(R.string.data_source_json)));
            intent.putExtra(
                    getString(R.string.relay_time_seconds_json),
                    getIntent().getIntExtra(getString(R.string.relay_time_seconds_json), 0)
            );

            intent.putExtra(
                    getString(R.string.conf_screens_seconds_json),
                    getIntent().getIntExtra(getString(R.string.conf_screens_seconds_json), 0)
            );

            intent.putExtra(
                    getString(R.string.relay_feature_json),
                    getIntent().getBooleanExtra(getString(R.string.relay_feature_json), true)
            );

            startActivity(intent);
        }

        oldNumberEditText.setText(stringBuilder.toString());

    }

}

