package com.example.andreyu.bta2dp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.widget.TextView;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

public class BTA2DPMainActivity extends AppCompatActivity implements BluetoothBroadcastReceiver.Callback, BluetoothA2DPRequester.Callback {

    public TextView textLog;
    public boolean connectBT;

    /**
     * This is the name of the device to connect to. You can replace this with the name of
     * your device.
     */
    private static final String CAR_MEDIA = "ST DISCO R58";

    /**
     * Local reference to the device's BluetoothAdapter
     */
    private BluetoothAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bta2dp_main);

        connectBT = true;

        textLog = (TextView) findViewById(R.id.textViewLog);

        assert textLog != null;
        textLog.append("\nBTA2DPMainActivity->onCreate");

        //Store a local reference to the BluetoothAdapter
        mAdapter = BluetoothAdapter.getDefaultAdapter();

        //Already connected, skip the rest
        if (mAdapter.isEnabled()) {
            textLog.append("\nBTA2DPMainActivity->isEnabled");
            onBluetoothConnected();
            return;
        }

        //Check if we're allowed to enable Bluetooth. If so, listen for a
        //successful enabling
        if (mAdapter.enable()) {
            textLog.append("\nBTA2DPMainActivity->register");
            BluetoothBroadcastReceiver.register(this, this);
        } else {
            textLog.append("\nUnable to enable Bluetooth. Is Airplane Mode enabled?");
        }
    }

    @Override
    public void onBluetoothError() {
        textLog.append("\nThere was an error enabling the Bluetooth Adapter.");
    }

    @Override
    public void onBluetoothConnected() {
        new BluetoothA2DPRequester(this, this).request(this, mAdapter);
    }

    @Override
    public void onA2DPProxyReceived(BluetoothA2dp proxy) {

        Method connect = getConnectMethod();

        textLog.append("\nBTA2DPMainActivity->findBondedDeviceByName");
        BluetoothDevice device = findBondedDeviceByName(mAdapter, CAR_MEDIA);
        if(device != null){
            textLog.append("\nDevice found:" + device.getName());
        }

        for (BluetoothDevice connectedDevice : proxy.getConnectedDevices()) {
            if (connectedDevice != null) {
                connectBT = false;
                textLog.append("\nConnected Device: " + connectedDevice.getName());
            }
        }

        //If either is null, just return. The errors have already been logged
        if (connect == null || device == null) {
            textLog.append("\nconnect == null or device == null");
            return;
        }

        if (connectBT) {
            try {

                textLog.append("\nBTA2DPMainActivity->setAccessible");
                connect.setAccessible(true);
                textLog.append("\nBTA2DPMainActivity->invoke");
                connect.invoke(proxy, device);

            } catch (InvocationTargetException ex) {
                textLog.append("\nUnable to invoke connect(BluetoothDevice) method on proxy. ");
            } catch (IllegalAccessException ex) {
                textLog.append("\nIllegal Access! ");
            }
        }
    }

    /**
     * Wrapper around some reflection code to get the hidden 'connect()' method
     *
     * @return the connect(BluetoothDevice) method, or null if it could not be found
     */
    private Method getConnectMethod() {
        try {
            textLog.append("\nBTA2DPMainActivity->getConnectMethod");
            return BluetoothA2dp.class.getDeclaredMethod("connect", BluetoothDevice.class);
        } catch (NoSuchMethodException ex) {
            textLog.append("\nUnable to find connect(BluetoothDevice) method in BluetoothA2dp proxy.");
            return null;
        }
    }

    /**
     * Search the set of bonded devices in the BluetoothAdapter for one that matches
     * the given name
     *
     * @param adapter the BluetoothAdapter whose bonded devices should be queried
     * @param name    the name of the device to search for
     * @return the BluetoothDevice by the given name (if found); null if it was not found
     */
    private static BluetoothDevice findBondedDeviceByName(BluetoothAdapter adapter, String name) {

        for (BluetoothDevice device : getBondedDevices(adapter)) {
            if (name.matches(device.getName())) {
                return device;
            }
        }
        return null;
    }

    /**
     * Safety wrapper around BluetoothAdapter#getBondedDevices() that is guaranteed
     * to return a non-null result
     *
     * @param adapter the BluetoothAdapter whose bonded devices should be obtained
     * @return the set of all bonded devices to the adapter; an empty set if there was an error
     */
    private static Set<BluetoothDevice> getBondedDevices(BluetoothAdapter adapter) {
        Set<BluetoothDevice> results = adapter.getBondedDevices();
        if (results == null) {
            results = new HashSet<>();
        }
        return results;
    }
}