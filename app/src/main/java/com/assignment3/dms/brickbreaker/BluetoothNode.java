package com.assignment3.dms.brickbreaker;

import android.bluetooth.BluetoothDevice;
import java.io.Serializable;
import java.util.UUID;

public interface BluetoothNode extends Runnable, Serializable {
    //uuid for the Bluetooth application
    public static final UUID SERVICE_UUID
            = UUID.fromString("aa7e561f-591f-4767-bf26-e4bff3f0895f");
    //name for the Bluetooth application
    public static final String SERVICE_NAME = "BrickBreaker";
    // forward a Ball object to all chat nodes in the Bluetooth network
    public void forward(Ball ball);
    // stop all communication and clean up
    public void stop();
    // registers or unregisters (if null) a MultiPlayer for dealing with BT communication
    public void registerActivity(MultiPlayer multiPlayer);
    // connects a bluetooth device to another if they have same UUID
    public void establishConnection(BluetoothDevice device);
    // returns status of bluetooth client connection
    public String getConnectionStat();
}
