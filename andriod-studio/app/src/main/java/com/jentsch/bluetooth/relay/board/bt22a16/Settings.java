package com.jentsch.bluetooth.relay.board.bt22a16;

import android.support.v7.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

public class Settings extends AppCompatActivity {

    static String pulseDurationValue = "newDuration";
    static String pinCode = "pin";
    static String deviceName = "deviceName";
    boolean pulseDurationChanged = false;
    int result = 0;

    Button okButton;
    SeekBar pulseDuration;
    TextView timeLabel;
    EditText pinText;
    EditText deviceNameText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Intent intent = getIntent();

        okButton = (Button)findViewById(R.id.okButton);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Settings.this, MainActivity.class);
                //Change the activity.
                Intent returnIntent = new Intent();
                if (pulseDurationChanged) {
                    //convert seekbar value to from 0-49 to 100-5000 and send the value
                    returnIntent.putExtra(pulseDurationValue, String.valueOf((pulseDuration.getProgress() + 1) * 100));
                    result |= 1; //set 1st bit
                }
                if (pinText.getText().toString().trim().length() > 0) {
                    returnIntent.putExtra(pinCode, pinText.getText().toString().trim());
                    result |= (1 << 1); //set 2nd bit
                }
                if (deviceNameText.getText().toString().trim().length() > 0) {
                    returnIntent.putExtra(deviceName, deviceNameText.getText().toString().trim());
                    result |= (1 << 2); //set 3rd bit
                }
                setResult(result, returnIntent);
                finish();
            }
        });

        timeLabel = (TextView)findViewById(R.id.timeLabel);
        timeLabel.setText(String.valueOf((intent.getIntExtra("pulseDuration", 0)) / (float)1000));
        pinText = (EditText)findViewById(R.id.pinText);
        deviceNameText = (EditText)findViewById(R.id.deviceNameText);
        pulseDuration = (SeekBar)findViewById(R.id.pulseDuration);
        pulseDuration.setProgress((intent.getIntExtra("pulseDuration", 0) / 100) - 1);
        pulseDuration.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                timeLabel.setText(String.valueOf((pulseDuration.getProgress() + 1) / (float) 10) + "s");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                pulseDurationChanged = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }
}