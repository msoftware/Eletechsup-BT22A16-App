package com.jentsch.bluetooth.relay.board.bt22a16;

import android.app.Activity;
import android.net.LinkAddress;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.IOException;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    MenuItem connectedIcon;
    MenuItem disconnectedIcon;

    int pulseDuration = 0; //pulse duration in ms
    String address = null;
    private ProgressDialog progress;
    BluetoothAdapter myBluetooth = null;
    BluetoothSocket btSocket = null;
    private boolean isBtConnected = false;
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    SharedPreferences.Editor editor;
    SharedPreferences sharedPreferences;
    String changeText = "";
    private LinearLayout mainLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        addLayoutChannels();

        sharedPreferences = getSharedPreferences("attinybluetoothio", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        IntentFilter disconnectedFilter = new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        this.registerReceiver(mReceiver, disconnectedFilter);

        address = sharedPreferences.getString("address", "undefined");
        if (!address.equals("undefined"))
            new ConnectBT().execute();
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
        String data = (String)view.getTag();
        btSendData(data);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        //menu icons ((dis)connected)
        connectedIcon = menu.findItem(R.id.connected);
        disconnectedIcon = menu.findItem(R.id.disconnected);
        connectedIcon.setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.connectBluetooth) {
            if (!isBtConnected) {
                Intent i = new Intent(this, DeviceList.class);
                startActivityForResult(i, 1);
            }
            else
                msg("Bluetooth is already connected.");
        }

        if (id == R.id.disconnectBluetooth) {
            Disconnect();
        }

        if (id == R.id.settings) {
            Intent i = new Intent(this, Settings.class);
            i.putExtra("pulseDuration", pulseDuration);
            //msg(String.valueOf(pulseDuration));
            startActivityForResult(i, 2);


        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 1) { //deviceList
            if(resultCode == Activity.RESULT_OK){
                address = data.getStringExtra(DeviceList.address);
                editor.putString("address", address).commit();
                new ConnectBT().execute();
            }
            if (resultCode == Activity.RESULT_CANCELED)
                msg("Canceled.");
        }

        if (requestCode == 2) { //settings

            if ((resultCode & 1) == 1) //pulseDuration changed
            {
                if (isBtConnected) {
                    try {
                        pulseDuration = Integer.parseInt(data.getStringExtra(Settings.pulseDurationValue));
                        btSocket.getOutputStream().write(("d" + String.valueOf(pulseDuration) + "\n").getBytes());
                        //msg("d" + String.valueOf(pulseDuration));
                        try {
                            Thread.sleep(200);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                else
                    msg("Bluetooth is not connected.");
            }
            if (((resultCode >> 1) & 1) == 1) //pin changed
            {
                if (isBtConnected) {
                    try {
                        btSocket.getOutputStream().write(("p" + data.getStringExtra(Settings.pinCode) + "\n").getBytes());
                        //msg("p" + data.getStringExtra(settings.pinCode));
                        Disconnect();
                        changeText = "Changing pin code...";
                        new changingSomething().execute();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                else
                    msg("Bluetooth is not connected.");
            }
            if (((resultCode >> 2) & 1) == 1) //device name changed
            {
                if (isBtConnected) {
                    try {
                        btSocket.getOutputStream().write(("n" + data.getStringExtra(Settings.deviceName) + "\n").getBytes());
                        //msg("n" + data.getStringExtra(settings.deviceName));
                        Disconnect();
                        changeText = "Changing name...";
                        new changingSomething().execute();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                else
                    msg("Bluetooth is not connected.");
            }
        }
    }

    private void btSendData(String s) {
        String lineBreak = "\n";
        if (isBtConnected) {
            try {
                btSocket.getOutputStream().write((s+lineBreak).getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            msg("Bluetooth is not connected.");
        }
    }

    private void msg(String s)
    {
        Toast.makeText(getApplicationContext(),s,Toast.LENGTH_LONG).show();
    }

    private class changingSomething extends AsyncTask<Void, Void, Void>  // UI thread
    {
        @Override
        protected void onPreExecute()
        {
            progress = ProgressDialog.show(MainActivity.this, changeText, "Please wait.");
        }

        @Override
        protected Void doInBackground(Void... devices)
        {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            progress.dismiss();
            new ConnectBT().execute();
        }
    }

    private class ConnectBT extends AsyncTask<Void, Void, Void>  // UI thread
    {
        private boolean ConnectSuccess = true;
        @Override
        protected void onPreExecute()
        {
            progress = ProgressDialog.show(MainActivity.this, "Connecting...", "Please wait.");
        }

        @Override
        protected Void doInBackground(Void... devices)
        {
            try
            {
                if (btSocket == null || !isBtConnected)
                {
                    myBluetooth = BluetoothAdapter.getDefaultAdapter();
                    BluetoothDevice btService = myBluetooth.getRemoteDevice(address);
                    btSocket = btService.createInsecureRfcommSocketToServiceRecord(myUUID);
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    btSocket.connect();
                }
            }
            catch (IOException e)
            {
                ConnectSuccess = false;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result)
        {
            super.onPostExecute(result);
            if (!ConnectSuccess || btSocket == null) {
                msg("Connection Failed.");
            }
            else {
                msg("Connected.");
                isBtConnected = true;
                connectedIcon.setVisible(true);
                disconnectedIcon.setVisible(false);
                try {
                    btSocket.getOutputStream().write("c\n".getBytes());
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (NullPointerException e) {
                    Disconnect();
                    new ConnectBT().execute();
                }
            }
            progress.dismiss();
        }
    }

    private void Disconnect() {
        if (btSocket != null) //If the btSocket is busy
        {
            try {
                btSocket.getInputStream().close();
                btSocket.getOutputStream().close();
                btSocket.close(); //close connection
                disconnected(false);
            } catch (IOException e) {
                msg("Error.");
            }
        }
        else
        {
            msg("Bluetooth is not connected.");
            disconnected(false);
        }
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(mReceiver);
        Disconnect();
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }

    //The BroadcastReceiver that listens for bluetooth broadcasts
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(BluetoothDevice.ACTION_ACL_DISCONNECTED)) {
                disconnected(true);
            }
        }
    };

    private void disconnected (boolean notify)
    {
        if (notify)
            msg("Disconnected.");
        isBtConnected = false;
        btSocket = null;
        disconnectedIcon.setVisible(true);
        connectedIcon.setVisible(false);
    }
}


