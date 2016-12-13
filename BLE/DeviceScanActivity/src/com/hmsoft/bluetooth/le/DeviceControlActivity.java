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

package com.hmsoft.bluetooth.le;

import java.lang.reflect.Array;

import com.example.bluetooth.le.R;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
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
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.CheckBox;


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
    public short msg_count = 0;


    private TextView mDataField;
    private String mDeviceName;
    private String mDeviceAddress;
    private BluetoothLeService mBluetoothLeService;
    private boolean mConnected = false;
    
    EditText edtSend;
	ScrollView svResult;
	Button btnSend;
	Button btnHex;
	Button btnOpen;
	Button btnClose;
	Button btnStop;
	Button btnLamp;
	CheckBox chkAutoReply;
	CheckBox chkCRLF;

    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            
            Log.e(TAG, "mBluetoothLeService is okay");
            // Automatically connects to the device upon successful start-up initialization.
            //mBluetoothLeService.connect(mDeviceAddress);
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
			byte tx_bytes[] = {02,0,0,0,0,0,0,03};
			byte rx_bytes[];
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) { //收到数据
            	Log.e(TAG, "RECV DATA");
            	rx_bytes = intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA);
            	String data = intent.getStringExtra(BluetoothLeService.EXTRA_DATA);
            	chkAutoReply = (CheckBox)findViewById(R.id.ckAutoReply);
            	if (Array.getLength(rx_bytes) != 0) {
/*            	if (data != null) {
                	if (mDataField.length() > 500)
                		mDataField.setText("");
                    mDataField.append(data); 
                    svResult.post(new Runnable() {
            			public void run() {
            				svResult.fullScroll(ScrollView.FOCUS_DOWN);
            			}
            		});  
                    // check if auto reply is enabled
*/                    
            		if (chkAutoReply.isChecked() && (mConnected == true)) {
                    	msg_count++;
                    	tx_bytes[1] = (byte)(msg_count >> 8);
                    	tx_bytes[2] = (byte)(msg_count);
                    	mBluetoothLeService.WriteBytes(tx_bytes);
                    }
                }
            } else if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {  //连接成功
            	Log.e(TAG, "Only gatt, just wait");
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) { //断开连接
                mConnected = false;
                invalidateOptionsMenu();
                btnSend.setEnabled(false);
                btnHex.setEnabled(false);
                btnOpen.setEnabled(false);
                btnClose.setEnabled(false);
                btnStop.setEnabled(false);
                btnLamp.setEnabled(false);
                clearUI();
            }else if(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) //可以开始干活了
            {
            	mConnected = true;
            	mDataField.setText("");
            	ShowDialog();
            	btnSend.setEnabled(true);
            	btnHex.setEnabled(true);
                btnOpen.setEnabled(true);
                btnClose.setEnabled(true);
                btnStop.setEnabled(true);
                btnLamp.setEnabled(true);
            	Log.e(TAG, "In what we need");
            	invalidateOptionsMenu();
            }
        }
    };

    private void clearUI() {
        mDataField.setText(R.string.no_data);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {                                        //初始化
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gatt_services_characteristics);

        final Intent intent = getIntent();
        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);

        // Sets up UI references.
        mDataField = (TextView) findViewById(R.id.data_value);
        edtSend = (EditText) this.findViewById(R.id.edtSend);
        edtSend.setText("Text to send");
        svResult = (ScrollView) this.findViewById(R.id.svResult);
        
        btnSend = (Button) this.findViewById(R.id.btnSend);
		btnSend.setOnClickListener(new ClickEvent());
		btnSend.setEnabled(false);
		
		btnHex = (Button) this.findViewById(R.id.btnHex);
		btnHex.setOnClickListener(new ClickEvent());
		btnHex.setEnabled(false);

		btnOpen = (Button) this.findViewById(R.id.btnOpen);
		btnOpen.setOnClickListener(new ClickEvent());
		btnOpen.setEnabled(false);

		btnClose = (Button) this.findViewById(R.id.btnClose);
		btnClose.setOnClickListener(new ClickEvent());
		btnClose.setEnabled(false);

		btnStop = (Button) this.findViewById(R.id.btnStop);
		btnStop.setOnClickListener(new ClickEvent());
		btnStop.setEnabled(false);

		btnLamp = (Button) this.findViewById(R.id.btnLamp);
		btnLamp.setOnClickListener(new ClickEvent());
		btnLamp.setEnabled(false);


        getActionBar().setTitle(mDeviceName);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        Log.d(TAG, "Try to bindService=" + bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE));
        
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
    }

    @Override
    protected void onResume() {
        super.onResume();
        /*registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
            Log.d(TAG, "Connect request result=" + result);
        }*/
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
        unbindService(mServiceConnection);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //this.unregisterReceiver(mGattUpdateReceiver);
        //unbindService(mServiceConnection);
        if(mBluetoothLeService != null)
        {
        	mBluetoothLeService.close();
        	mBluetoothLeService = null;
        }
        Log.d(TAG, "We are in destroy");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.gatt_services, menu);
        if (mConnected) {
            menu.findItem(R.id.menu_connect).setVisible(false);
            menu.findItem(R.id.menu_disconnect).setVisible(true);
        } else {
            menu.findItem(R.id.menu_connect).setVisible(true);
            menu.findItem(R.id.menu_disconnect).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {                              //点击按钮
        switch(item.getItemId()) {
            case R.id.menu_connect:
                mBluetoothLeService.connect(mDeviceAddress);
                return true;
            case R.id.menu_disconnect:
                mBluetoothLeService.disconnect();
                return true;
            case android.R.id.home:
            	if(mConnected)
            	{
            		mBluetoothLeService.disconnect();
            		mConnected = false;
            	}
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    private void ShowDialog()
    {
    	Toast.makeText(this, "Connected", Toast.LENGTH_SHORT).show();
    }

 // 按钮事件
	class ClickEvent implements View.OnClickListener {
		@Override
		public void onClick(View v) {
			byte bytes[] = {02,2,3,4,5,6,7,8,9,03};
			if(!mConnected) return;

			if (v == btnSend)
			{	
            	chkCRLF = (CheckBox)findViewById(R.id.ckCRLF);
				
				if (edtSend.length() < 1) {
					Toast.makeText(DeviceControlActivity.this, "Zero length!", Toast.LENGTH_SHORT).show();
					return;
				}
				if (chkCRLF.isChecked())
				{
					mBluetoothLeService.WriteValue(edtSend.getText().toString() + "\r\n");
				}
				else
				{
					mBluetoothLeService.WriteValue(edtSend.getText().toString());
				}
				
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				if(imm.isActive())
					imm.hideSoftInputFromWindow(edtSend.getWindowToken(), 0);
				//todo Send data
			}
			else if (v == btnHex)
			{
				
				mBluetoothLeService.WriteBytes(bytes);
			}
			else if (v == btnOpen)
			{
				mBluetoothLeService.WriteValue("open\r\n");
			}
			else if (v == btnClose)
			{
				mBluetoothLeService.WriteValue("close\r\n");
			}
			else if (v == btnStop)
			{
				mBluetoothLeService.WriteValue("stop\r\n");
			}
			else if (v == btnLamp)
			{
				mBluetoothLeService.WriteValue("lamp\r\n");
			}
		}

	}
	
    private static IntentFilter makeGattUpdateIntentFilter() {                        //注册接收的事件
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(BluetoothDevice.ACTION_UUID);
        return intentFilter;
    }
}
