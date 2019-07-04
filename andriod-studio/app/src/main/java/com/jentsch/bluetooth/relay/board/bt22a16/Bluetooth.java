package com.jentsch.bluetooth.relay.board.bt22a16;

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
import android.preference.PreferenceManager;

import java.io.IOException;
import java.util.UUID;

public class Bluetooth {

    private BluetoothActivity bluetoothActivity;

    BluetoothAdapter myBluetooth = null;
    BluetoothSocket btSocket = null;

    public boolean isBtConnected() {
        return isBtConnected;
    }

    private boolean isBtConnected = false;
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    String address = null;

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    public Bluetooth(BluetoothActivity bluetoothActivity) {
        this.bluetoothActivity = bluetoothActivity;
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(bluetoothActivity);
        editor = sharedPreferences.edit();

        IntentFilter disconnectedFilter = new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        bluetoothActivity.registerReceiver(mReceiver, disconnectedFilter);

        address = sharedPreferences.getString("address", "undefined");
        if (!address.equals("undefined"))
            new ConnectBT().execute();
    }

    public void setAddress (String address)
    {
        this.address = address;
        editor.putString("address", address).commit();
        new ConnectBT().execute();
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

    public void btSendData(String s) {
        String lineBreak = "\n";
        if (isBtConnected) {
            try {
                btSocket.getOutputStream().write((s + lineBreak).getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            bluetoothActivity.msg("Bluetooth is not connected.");
        }
    }

    public void disconnect() {
        bluetoothActivity.unregisterReceiver(mReceiver);
        if (btSocket != null) {
            try {
                btSocket.getInputStream().close();
                btSocket.getOutputStream().close();
                btSocket.close(); //close connection
                disconnected(false);
            } catch (IOException e) {
                bluetoothActivity.msg("Error.");
            }
        } else {
            bluetoothActivity.msg("Bluetooth is not connected.");
            disconnected(false);
        }
    }

    private void disconnected (boolean notify)
    {
        isBtConnected = false;
        btSocket = null;
        bluetoothActivity.disconnected(notify);
    }

    private class ConnectBT extends AsyncTask<Void, Void, Void>  // UI thread
    {
        private boolean ConnectSuccess = true;

        @Override
        protected void onPreExecute() {
            ProgressDialog progress = ProgressDialog.show(bluetoothActivity, "Connecting...", "Please wait.");
            bluetoothActivity.setProgress(progress);
        }

        @Override
        protected Void doInBackground(Void... devices) {
            try {
                if (btSocket == null || !isBtConnected) {
                    myBluetooth = BluetoothAdapter.getDefaultAdapter();
                    BluetoothDevice btService = myBluetooth.getRemoteDevice(address);
                    btSocket = btService.createInsecureRfcommSocketToServiceRecord(myUUID);
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    btSocket.connect();
                }
            } catch (IOException e) {
                ConnectSuccess = false;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            if (!ConnectSuccess || btSocket == null) {
                bluetoothActivity.msg("Connection Failed.");
            } else {
                isBtConnected = true;
                bluetoothActivity.connected(true);
                try {
                    btSocket.getOutputStream().write("c\n".getBytes());
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (NullPointerException e) {
                    disconnect();
                    new ConnectBT().execute();
                }
            }
            bluetoothActivity.dismissProgress();
        }
    }
}
