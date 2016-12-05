package com.assignment3.dms.brickbreaker;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.widget.Toast;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

public class BluetoothServer implements BluetoothNode {
    private boolean stopRequested;
    private List<ClientHandler> clientConnections;
    private List<Ball> balls;
    private MultiPlayer multiPlayer;
    private static final long serialVersionUID = 1;

    public BluetoothServer(){
        multiPlayer = null;
        clientConnections = new ArrayList<>();
        balls = new ArrayList<>();
    }

    //Method not needed in Server class
    public void establishConnection(BluetoothDevice device) {
        return;
    }

    //Method not needed in Server class
    public String getConnectionStat() {
        return "";
    }

    public void run() {
        stopRequested = false;
        clientConnections.clear();
        balls.clear();
        BluetoothServerSocket serverSocket = null;
        try {
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            serverSocket = bluetoothAdapter.listenUsingRfcommWithServiceRecord
                    (BluetoothNode.SERVICE_NAME, BluetoothNode.SERVICE_UUID);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        // prepare the mailer that will handle outgoing messages
        OutgoingThread outgoing = new OutgoingThread();
        Thread outgoingThread = new Thread(outgoing);
        outgoingThread.start();
        // listen for connections
        while(!stopRequested) {
            try {
                //block upto 500ms timeout to get incoming connected socket
                BluetoothSocket socket = serverSocket.accept(500);
                multiPlayer.showMessage("Client connected");
            // handle the client connection in a separate thread
            ClientHandler clientHandler = new ClientHandler(socket);
            clientConnections.add(clientHandler);
            Thread clientThread = new Thread(clientHandler);
            clientThread.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void forward(Ball ball) {
        synchronized (balls) {
            balls.add(ball);
            //Notify waiting threads that there is a new ball to send
            balls.notifyAll();
        }
    }

    public void stop() {
        stopRequested = true;
        synchronized (balls) {
            balls.notifyAll();
        }
        for (ClientHandler clientConnection : clientConnections)
            clientConnection.closeConnection();
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

    public void registerActivity(MultiPlayer multiPlayer) {
        this.multiPlayer = multiPlayer;
    }

    // inner class that handles incoming communication with a client
    private class ClientHandler implements Runnable {
        private BluetoothSocket socket;
        private ObjectOutputStream oos;

        public ClientHandler(BluetoothSocket socket) {
            this.socket = socket;
            try {
                oos= new ObjectOutputStream(socket.getOutputStream());
            } catch(IOException e) {
                e.printStackTrace();
            }
        }

        //Listens for incoming balls
        public void run() {
            try {
                ObjectInputStream ois= new ObjectInputStream(socket.getInputStream());
                // loop until the connection closes or stop requested
                while (!stopRequested) {
                    Ball ballUpdate = (Ball) ois.readObject();
                    multiPlayer.receiveBall(ballUpdate);
                }
            } catch (IOException e) {
                e.printStackTrace();
                if(multiPlayer!=null) {
                    createToast("Client disconnected");
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        public void send(Ball ballUpdate) throws IOException {
            oos.writeObject(ballUpdate);
            oos.flush();
        }

        public void closeConnection() {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            clientConnections.remove(this);
        }
    }

    //Inner class handles sending messages to all client chat nodes
    private class OutgoingThread implements Runnable {
        public void run() {
            while (!stopRequested) {
                Ball ballUpdate;
                synchronized (balls) {
                    while (balls.size()==0){
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
                if (multiPlayer != null) {
                    //Send balls to the client
                    for (ClientHandler clientHandler : clientConnections) {
                        try {
                            clientHandler.send(ballUpdate);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }
}