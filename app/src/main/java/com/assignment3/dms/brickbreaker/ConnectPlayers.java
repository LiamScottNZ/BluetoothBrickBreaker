package com.assignment3.dms.brickbreaker;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class ConnectPlayers extends Activity implements OnClickListener {

    private Button discoverableButton,serverStartButton,clientStartButton;
    private TextView statusTextView;
    private BroadcastReceiver bluetoothStatusBroadcastReceiver;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect_players);
        discoverableButton = (Button)findViewById(R.id.discoverable_button);
        discoverableButton.setOnClickListener(this);
        serverStartButton=(Button)findViewById(R.id.server_start_button);
        serverStartButton.setOnClickListener(this);
        clientStartButton=(Button)findViewById(R.id.client_start_button);
        clientStartButton.setOnClickListener(this);
        statusTextView = (TextView)findViewById(R.id.status_textview);
        //For android 6 devices: must enable location permissions
        if (ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION}, 1001);
        }
        SharedPreferences sharedpreferences = getSharedPreferences("SharedPreferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putString("GameMode", "Multi");
        editor.commit();
    }

    public void onStart() {
        super.onStart();
        // check whether device supports Bluetooth
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null)
            statusTextView.setText(R.string.status_unsupported);
        else {
            // create a broadcast receiver notified when Bluetooth status changes
            if (bluetoothStatusBroadcastReceiver==null)
                bluetoothStatusBroadcastReceiver = new BluetoothStatusBroadcastReceiver();
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
            intentFilter.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
            registerReceiver(bluetoothStatusBroadcastReceiver,intentFilter);
            if (!bluetoothAdapter.isEnabled()) {
                statusTextView.setText(R.string.status_off);
                // try to enable Bluetooth on device
                Intent enableBluetoothIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivity(enableBluetoothIntent);
            } else
                statusTextView.setText(R.string.status_on);
        }
    }

    /** Called when the activity is stopped. */
    public void onStop()
    {  super.onStop();
        if (bluetoothStatusBroadcastReceiver != null)
            unregisterReceiver(bluetoothStatusBroadcastReceiver);
    }

    //Implementation of OnClickListener method
    public void onClick(View view) {
        if (view == discoverableButton) {
            //Make the device discoverable
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION,300); //5 min
            startActivity(discoverableIntent);
        } else if (view == serverStartButton) {
            //Start as a server
            BluetoothNode bluetoothNode = new BluetoothServer();
            Intent intent = new Intent(this, MultiPlayer.class);
            intent.putExtra(BluetoothNode.class.getName(), bluetoothNode);
            intent.putExtra("Color", Color.CYAN);
            startActivity(intent);
        } else if (view == clientStartButton) {
            //Start as a client
            BluetoothNode bluetoothNode = new BluetoothClient();
            Intent intent = new Intent(this, MultiPlayer.class);
            intent.putExtra(BluetoothNode.class.getName(), bluetoothNode);
            intent.putExtra("Color", Color.MAGENTA);
            startActivity(intent);
        }
    }

    //Inner class that receives Bluetooth state and scan mode changes
    public class BluetoothStatusBroadcastReceiver extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                int newState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);
                switch (newState){
                    case BluetoothAdapter.STATE_OFF:
                        statusTextView.setText(R.string.status_off);
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        statusTextView.setText(R.string.status_turning_on);
                        break;
                    case BluetoothAdapter.STATE_ON:
                        statusTextView.setText(R.string.status_on);
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        statusTextView.setText(R.string.status_turning_off);
                        break;
                }
            } else if (intent.getAction().equals(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED)) {
                int newScanMode = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, -1);
                switch (newScanMode) {
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE:
                        statusTextView.setText(R.string.status_connectable_discoverable);
                        break;
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE:
                        statusTextView.setText(R.string.status_connectable);
                        break;
                    case BluetoothAdapter.SCAN_MODE_NONE:
                        statusTextView.setText(R.string.status_neither_connectable_discoverable);
                        break;
                }
            }
        }
    }
}