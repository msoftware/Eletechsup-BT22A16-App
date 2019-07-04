package com.jentsch.bluetooth.relay.board.bt22a16;

import android.app.Activity;
import android.os.Bundle;
import android.app.ProgressDialog;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;


import static com.jentsch.bluetooth.relay.board.bt22a16.DeviceList.address;

public class MainActivity extends BluetoothActivity {

    MenuItem connectedIcon;
    MenuItem disconnectedIcon;
    private ProgressDialog progress;
    private LinearLayout mainLayout;
    private Bluetooth bluetooth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        addLayoutChannels();
        bluetooth = new Bluetooth(this);
    }

    private void addLayoutChannels() {
        mainLayout = findViewById(R.id.main_layout);
        addLayoutChannel("1", "A");
        addLayoutChannel("2", "B");
        addLayoutChannel("3", "C");
        addLayoutChannel("4", "D");
        addLayoutChannel("5", "E");
        addLayoutChannel("6", "F");
        addLayoutChannel("7", "G");
        addLayoutChannel("8", "H");
        addLayoutChannel("9", "I");
        addLayoutChannel("10", "J");
        addLayoutChannel("11", "K");
        addLayoutChannel("12", "L");
        addLayoutChannel("13", "M");
        addLayoutChannel("14", "N");
        addLayoutChannel("15", "O");
        addLayoutChannel("16", "P");

    }

    private void addLayoutChannel(String channelName, String letter) {
        LayoutInflater inflater = getLayoutInflater();
        View channel = inflater.inflate(R.layout.include_channel, mainLayout, false);
        initButton(R.id.b0, letter + 0, "C" + channelName + " " + letter + 0, channel);
        initButton(R.id.b1, letter + 1, "C" + channelName + " " + letter + 1, channel);
        initButton(R.id.b2, letter + 2, "C" + channelName + " " + letter + 2, channel);
        initButton(R.id.b3, letter + 3, "C" + channelName + " " + letter + 3, channel);
        initButton(R.id.b4, letter + 4, "C" + channelName + " " + letter + 4, channel);
        mainLayout.addView(channel);
    }

    private void initButton(int id, String tag, String text, View channel) {
        Button b0 = channel.findViewById(id);
        b0.setTag(tag);
        b0.setText(text);
    }

    public void onButtonClick(View view) {
        String data = (String) view.getTag();
        bluetooth.btSendData(data);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        connectedIcon = menu.findItem(R.id.connected);
        disconnectedIcon = menu.findItem(R.id.disconnected);
        connectedIcon.setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.connectBluetooth) {
            if (!bluetooth.isBtConnected()) {
                Intent i = new Intent(this, DeviceList.class);
                startActivityForResult(i, 1);
            } else
                msg("Bluetooth is already connected.");
        }

        if (id == R.id.disconnectBluetooth) {
            bluetooth.disconnect();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 1) { //deviceList
            if (resultCode == Activity.RESULT_OK) {
                address = data.getStringExtra(address);
                bluetooth.setAddress(address);
            }
            if (resultCode == Activity.RESULT_CANCELED)
                msg("Canceled.");
        } else {
            msg("Invalid request code");
        }
    }

    @Override
    public void onDestroy() {
        bluetooth.disconnect();
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }

    @Override
    public void msg(String s) {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
    }

    @Override
    public void setProgress(ProgressDialog progress) {
        this.progress = progress;
    }

    @Override
    public void dismissProgress() {
        progress.dismiss();
    }

    @Override
    public void connected(boolean notify) {
        if (notify)
            msg("Connected.");
        connectedIcon.setVisible(true);
        disconnectedIcon.setVisible(false);
    }

    @Override
    public void disconnected(boolean notify) {
        if (notify)
            msg("Disconnected.");
        disconnectedIcon.setVisible(true);
        connectedIcon.setVisible(false);
    }
}


