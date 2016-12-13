/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.bluetoothlegatt;

import android.app.Activity;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 * For a given BLE device, this Activity provides the user interface to connect, display data,
 * and display GATT services and characteristics supported by the device.  The Activity
 * communicates with {@code BluetoothLeService}, which in turn interacts with the
 * Bluetooth LE API.
 */
public class DeviceControlActivity extends Activity {
    private final static String TAG = DeviceControlActivity.class.getSimpleName();

    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";

    private TextView mConnectionState;
    private TextView mDataField;
    private String mDeviceName;
    private String mDeviceAddress;
    private BluetoothLeService mBluetoothLeService;
    private boolean mConnected = false;
    private boolean mConnectReq = true;
    private BluetoothGattCharacteristic mNotifyCharacteristic;

    private final String LIST_NAME = "NAME";
    private final String LIST_UUID = "UUID";

    Button btnOpen;
    Button btnClose;
    Button btnLight;
    Button btnStop;


    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
            mBluetoothLeService.connect(mDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
    //                        or notification operations.
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;
                updateConnectionState(mConnected);
                invalidateOptionsMenu();
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                updateConnectionState(mConnected);
                invalidateOptionsMenu();
             } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the user interface.
                getRemoteControlService(mBluetoothLeService.getSupportedGattServices());
//                displayGattServices(mBluetoothLeService.getSupportedGattServices());
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                displayData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
            }
        }
    };


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gatt_services_characteristics);

        final Intent intent = getIntent();
        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);

        // Sets up UI references.
     //   ((TextView) findViewById(R.id.device_address)).setText(mDeviceAddress);
     //   mConnectionState = (TextView) findViewById(R.id.connection_state);
     //   mDataField = (TextView) findViewById(R.id.data_value);

        btnOpen = (Button) this.findViewById(R.id.btn_open);
        btnOpen.setOnClickListener(new ClickEvent());
        btnClose = (Button) this.findViewById(R.id.btn_close);
        btnClose.setOnClickListener(new ClickEvent());
        btnLight = (Button) this.findViewById(R.id.btn_light);
        btnLight.setOnClickListener(new ClickEvent());
        btnStop = (Button) this.findViewById(R.id.btn_stop);
        btnStop.setOnClickListener(new ClickEvent());

        getActionBar().setTitle(mDeviceName);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
            Log.d(TAG, "Connect request result=" + result);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
        mBluetoothLeService = null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // TODO get rid of connect/disconnect button
        getMenuInflater().inflate(R.menu.gatt_services, menu);
        if (mConnected) {
            menu.findItem(R.id.menu_connect).setVisible(false);
            menu.findItem(R.id.menu_disconnect).setVisible(true);
            menu.findItem(R.id.menu_refresh).setShowAsAction((MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW));
            menu.findItem(R.id.menu_refresh).setActionView(null);
        } else {
            menu.findItem(R.id.menu_connect).setVisible(true);
            menu.findItem(R.id.menu_disconnect).setVisible(false);
            if (mConnectReq) {
                mConnectReq = false;
                menu.findItem(R.id.menu_refresh).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
                menu.findItem(R.id.menu_refresh).setActionView(
                        R.layout.actionbar_indeterminate_progress);
            }


        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            // TODO call this in the oncreate method
            case R.id.menu_connect:
                mBluetoothLeService.connect(mDeviceAddress);
                mConnectReq = true;
                invalidateOptionsMenu();
                return true;
            case R.id.menu_disconnect:
                mBluetoothLeService.disconnect();
                return true;
            case R.id.menu_refresh:
                Intent intent = new Intent();
                intent.putExtra("BTRefresh", true);
                setResult(RESULT_OK, intent);
                onBackPressed();
                return true;
            case android.R.id.home:
                intent = new Intent();
                intent.putExtra("BTRefresh", false);
                setResult(RESULT_OK, intent);
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateConnectionState(boolean state) {
        // TODO do something with UI to indicate connection status
        btnOpen.setEnabled(state);
        btnClose.setEnabled(state);
        btnStop.setEnabled(state);
        btnLight.setEnabled(state);
/*        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mConnectionState.setText(resourceId);
            }
        });*/
    }

    private void displayData(String data) {
        if (data != null) {
            mDataField.setText(data);
        }
    }

    class ClickEvent implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            if (v == btnOpen) {
                if(!mConnected) return;
                WriteValue("open\r\n");
            }
            else if (v == btnClose) {
                if(!mConnected) return;
                WriteValue("close\r\n");
            }
            else if (v == btnStop) {
                if(!mConnected) return;
                WriteValue("stop\r\n");
            }
            else if (v == btnLight) {
                if(!mConnected) return;
                WriteValue("lamp\r\n");
            }
        }

    }

    private void WriteValue(String strValue)
    {
        mNotifyCharacteristic.setValue(strValue.getBytes());
        mBluetoothLeService.writeCharacteristic(mNotifyCharacteristic);
    }


    // Demonstrates how to iterate through the supported GATT Services/Characteristics.
    // In this sample, we populate the data structure that is bound to the ExpandableListView
    // on the UI.
    private void getRemoteControlService(List<BluetoothGattService> gattServices) {
        if (gattServices == null) return;

        // Loops through available GATT Services.
        for (BluetoothGattService gattService : gattServices) {
            if(gattService.getUuid().toString().equals(SampleGattAttributes.REMOTE_CONTROL_SERVICE))

            // Loops through available Characteristics.
            for (BluetoothGattCharacteristic gattCharacteristic : gattService.getCharacteristics()) {
                if (gattCharacteristic.getUuid().toString().equals(SampleGattAttributes.REMOTE_CONTROL_MESSAGE)) {
                    mNotifyCharacteristic = gattCharacteristic;
                }
            }
        }

    }


    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }
}
