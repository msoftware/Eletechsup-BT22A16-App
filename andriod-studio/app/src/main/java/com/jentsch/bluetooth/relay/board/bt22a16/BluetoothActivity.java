package com.jentsch.bluetooth.relay.board.bt22a16;

import android.app.ProgressDialog;
import android.support.v7.app.AppCompatActivity;

abstract class BluetoothActivity extends AppCompatActivity {

    public abstract void connected (boolean notify);

    public abstract void disconnected (boolean notify);

    public abstract void msg(String s);

    public abstract void setProgress(ProgressDialog show);

    public abstract void dismissProgress();
}
