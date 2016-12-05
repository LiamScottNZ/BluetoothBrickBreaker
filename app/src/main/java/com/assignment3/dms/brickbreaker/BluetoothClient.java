package com.assignment3.dms.brickbreaker;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.widget.Toast;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

public class BluetoothClient implements BluetoothNode {

    private boolean stopRequested;
    public List<BluetoothDevice> devices;
    private BluetoothSocket socket;
    private List<Ball> balls;
    private MultiPlayer multiPlayer;
    private BroadcastReceiver deviceDiscoveryBroadcastReceiver;
    private static final long serialVersionUID = 1;
    public BluetoothAdapter bluetoothAdapter;
    public String connectionStat;

    public BluetoothClient() {
        devices = new ArrayList<>();
        socket = null;
        balls = new ArrayList<>();
        multiPlayer = null;
        deviceDiscoveryBroadcastReceiver = null;
    }

    public void scanDevices() {
        deviceDiscoveryBroadcastReceiver = new DeviceDiscoveryBroadcastReceiver();
        IntentFilter discoveryIntentFilter = new IntentFilter();
        discoveryIntentFilter.addAction(BluetoothDevice.ACTION_FOUND);
        discoveryIntentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        discoveryIntentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        multiPlayer.registerReceiver(deviceDiscoveryBroadcastReceiver, discoveryIntentFilter);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        bluetoothAdapter.startDiscovery();
    }

    public void run() {
        stopRequested = false;
        devices.clear();
        balls.clear();
        scanDevices();
        synchronized (devices) {
            try {
                devices.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (devices.size()==0 && !stopRequested) {
            createToast("No devices discovered, restart activity");
            stopRequested = true;
            return;
        }
    }

    public String getConnectionStat() {
        return connectionStat;
    }

    //Used to connect to a specific bluetooth device
    public void establishConnection(BluetoothDevice device) {
        bluetoothAdapter.cancelDiscovery();
        socket = null;
            //Open a connection to device using UUID
            try {
                socket = device.createRfcommSocketToServiceRecord(BluetoothNode.SERVICE_UUID);
                //Open Connection
                socket.connect();
            }
            catch (IOException e) {
                socket = null;
                e.printStackTrace();
            } catch(NullPointerException e) {
                e.printStackTrace();
                stop();
                return;
            }
        if (socket==null) {
            try {
                createToast("No server found, restart activity");
            } catch(NullPointerException e) {
                e.printStackTrace();
                stop();
                return;
            }
            connectionStat = "No server found";
            stopRequested = true;
            return;
        }
        multiPlayer.showMessage("Chat server found");
        connectionStat = "Connection Successful";
        OutgoingThread outgoing = new OutgoingThread();
        Thread outgoingThread = new Thread(outgoing);
        outgoingThread.start();
        IncomingThread incoming = new IncomingThread();
        Thread incomingThread = new Thread(incoming);
        incomingThread.start();
    }

    //Inner class that handles sending balls to the server
    private class OutgoingThread implements Runnable {
        public void run() {
            ObjectOutputStream oos=null;
            try {
                oos= new ObjectOutputStream(socket.getOutputStream());
            } catch (IOException e) {
                e.printStackTrace();
                stop();
            }
            while (!stopRequested) {
                Ball ballUpdate;
                synchronized (balls) {
                    while (balls.size() == 0) {
                        try {
                            balls.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        if (stopRequested)
                            return;
                    }
                    ballUpdate = balls.remove(0);
                }
                //Send Ball to server
                try {
                    oos.writeObject(ballUpdate);
                    oos.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //Thread used to handle incoming messages
    private class IncomingThread implements Runnable {
        public void run() {
            //Listen for incoming messages
            try {
                ObjectInputStream oi= new ObjectInputStream(socket.getInputStream());
                //Loop until the connection closes or stop requested
                while (!stopRequested) {
                    Ball ballUpdate = (Ball) oi.readObject();
                    if (multiPlayer != null) {
                        multiPlayer.receiveBall(ballUpdate);
                    }
                }
            }
            catch (IOException e) {
                e.printStackTrace();
                if(multiPlayer!=null) {
                    createToast("Client disconnected");
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    //Create a toast message in the MultiPlayer activity
    public void createToast(final String text) {
        multiPlayer.runOnUiThread(new Runnable() {
            public void run() {
                try {
                    Toast.makeText(multiPlayer.getApplicationContext(), text, Toast.LENGTH_LONG).show();
                } catch(NullPointerException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    //Send a ball to a bluetooth device
    public void forward(Ball ball) {
        synchronized (balls) {
            balls.add(ball);
            balls.notifyAll();
        }
    }

    //Stop all bluetooth communication
    public void stop() {
        stopRequested = true;
        if (deviceDiscoveryBroadcastReceiver != null) {
            multiPlayer.unregisterReceiver(deviceDiscoveryBroadcastReceiver);
            deviceDiscoveryBroadcastReceiver = null;
        }
        synchronized (devices) {
            devices.notifyAll();
        }
        synchronized (balls) {
            balls.notifyAll();
        }
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //Provide local instance of MultiPlayer activity
    public void registerActivity(MultiPlayer multiPlayer) {
        this.multiPlayer = multiPlayer;
    }

    //Inner class that receives device discovery changes
    public class DeviceDiscoveryBroadcastReceiver extends BroadcastReceiver
    {
        public void onReceive(Context context, Intent intent)
        {  String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                //Device found
                BluetoothDevice device = intent.getParcelableExtra
                        (BluetoothDevice.EXTRA_DEVICE);
                synchronized (devices) {
                    if(device!=null) {
                        devices.add(device);
                        MultiPlayer.BTDevices.add(device);
                        multiPlayer.updateList();
                    }
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                createToast("Device discovery started");
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                //Notify that device discovery has finished
                synchronized (devices) {
                    devices.notifyAll();
                }
            }
        }
    }
}