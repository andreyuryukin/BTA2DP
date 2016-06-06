package com.example.andreyu.bta2dp;

import android.app.Activity;
import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.widget.TextView;

public class BluetoothA2DPRequester implements BluetoothProfile.ServiceListener {

    private Callback mCallback;
    public Context context;
    private TextView textLog;

    /**
     * Creates a new instance of an A2DP Proxy requester with the
     * callback that should receive the proxy once it is acquired
     *
     * @param callback the callback that should receive the proxy
     */
    public BluetoothA2DPRequester(Callback callback, Context c) {

        context = c;
        mCallback = callback;
        textLog = (TextView) ((Activity) context).findViewById(R.id.textViewLog);
    }

    /**
     * Start an asynchronous request to acquire the A2DP proxy. The callback
     * will be notified when the proxy is acquired
     *
     * @param c       the context used to obtain the proxy
     * @param adapter the BluetoothAdapter that should receive the request for proxy
     */
    public void request(Context c, BluetoothAdapter adapter) {
        textLog.append("\nBluetoothA2DPRequester->request");
        adapter.getProfileProxy(c, this, BluetoothProfile.A2DP);
    }

    @Override
    public void onServiceConnected(int i, BluetoothProfile bluetoothProfile) {
        if (mCallback != null) {
            textLog.append("\nBluetoothA2DPRequester->onServiceConnected");
            mCallback.onA2DPProxyReceived((BluetoothA2dp) bluetoothProfile);
        }
    }

    @Override
    public void onServiceDisconnected(int i) {
        textLog.append("\nBluetoothA2DPRequester->onServiceDisconnected");
        //It's a one-off connection attempt; we don't care about the disconnection event.
    }

    public interface Callback {
        void onA2DPProxyReceived(BluetoothA2dp proxy);
    }
}
