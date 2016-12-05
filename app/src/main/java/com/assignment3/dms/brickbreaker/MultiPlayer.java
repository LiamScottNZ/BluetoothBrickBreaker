package com.assignment3.dms.brickbreaker;

import android.app.PendingIntent;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.IntentFilter;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcAdapter.CreateNdefMessageCallback;
import android.nfc.NfcEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.assignment3.dms.brickbreaker.levels.*;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class MultiPlayer extends Activity implements CreateNdefMessageCallback,View.OnClickListener, SensorEventListener, Serializable {

    private SensorManager sensorManager;
    private Sensor accelerometer;
    NfcAdapter mNfcAdapter;
    PendingIntent nfcPendingIntent;
    IntentFilter[] intentFiltersArray;
    TextView livesText, scoreText, status;
    public static BluetoothNode bluetoothNode;
    private Button startButton, addButton;
    private DrawView drawView;
    private boolean gameStart;
    public static int currentColour;
    public static int lives, score;
    public  static boolean hasWon;
    public int numExtraBalls;
    private boolean viewChanged;
    private ListView devicesList;
    public static List<BluetoothDevice> BTDevices;
    private List<String> deviceNames;
    private ArrayAdapter<String> adapter;
    private ArrayList<LevelBase> levels;
    private int currentLevel;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multi_player);
        BTDevices = new ArrayList<>();
        levels = new ArrayList<>();
        currentLevel = 0;
        loadLevels();
        lives = 10;
        score = 0;
        numExtraBalls=20;
        gameStart = false;
        hasWon=false;
        viewChanged=false;
        status = (TextView)findViewById(R.id.status);
        deviceNames = new ArrayList<>();
        devicesList = (ListView)findViewById(R.id.listView);
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1, deviceNames);
        devicesList.setAdapter(adapter);
        devicesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int index = position;
                String name = (String) devicesList.getItemAtPosition(index);
                BluetoothDevice selectedDevice = null;
                for(BluetoothDevice current: BTDevices) {
                    try {
                        if (current.getName().equalsIgnoreCase(name)) {
                            selectedDevice = current;
                            break;
                        }
                    } catch(NullPointerException e) {}
                }
                bluetoothNode.establishConnection(selectedDevice);
                status.setText(bluetoothNode.getConnectionStat());
            }
        });

        Intent bluetoothIntent = getIntent();
        currentColour = (int)bluetoothIntent.getExtras().get("Color");
        bluetoothNode = (BluetoothNode) bluetoothIntent.getExtras().get
                (BluetoothNode.class.getName());
        startButton = (Button) findViewById(R.id.start_game_button);
        startButton.setOnClickListener(this);
        startButton.setEnabled(false);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
        Intent nfcIntent = new Intent(this, getClass());
        nfcIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        nfcPendingIntent = PendingIntent.getActivity(this, 0, nfcIntent, 0);
        IntentFilter tagIntentFilter = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
        try {
            tagIntentFilter.addDataType("text/plain");
            intentFiltersArray = new IntentFilter[]{tagIntentFilter};
        } catch (Throwable t) {
            t.printStackTrace();
        }
        // Check for available NFC Adapter
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (mNfcAdapter == null) {
            Toast.makeText(this, "NFC is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        // Register callback
        mNfcAdapter.setNdefPushMessageCallback(this, this);
    }

    //Method to update the ListView with bluetooth devices
    public void updateList() {
        for(BluetoothDevice current: BTDevices) {
            try {
                if (!deviceNames.contains(current.getName())) {
                    try {
                        if(current.getName()!=null){
                            deviceNames.add(current.getName());
                        }

                    }catch(NullPointerException e) {}
                }
            } catch(NullPointerException e) {}
        }
        adapter.notifyDataSetChanged();
    }

    public NdefMessage createNdefMessage(NfcEvent event) {
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        restartGame();
        return null;
    }

    public void loadLevels() {
        LevelBase one = new Level1();
        LevelBase two = new Level2();
        LevelBase three = new Level3();
        LevelBase four = new Level4();
        LevelBase five = new Level5();
        levels.add(one);
        levels.add(two);
        levels.add(three);
        levels.add(four);
        levels.add(five);
    }

    public void restartGame() {
        lives = 10;
        score = 0;
        numExtraBalls=20;
        hasWon=false;
        addButton.post(new Runnable()
        {
            public void run()
            {  addButton.setEnabled(true);
            }
        });
        Ball ball = new Ball(drawView.posX, (drawView.posY - (2 * drawView.baseHeight)));
        ball.giveViewParameters(drawView.viewWidth, drawView.viewHeight);
        ball.resetBall(drawView.posX, drawView.posY);
        if(currentLevel<4)
            currentLevel++;
        else
            currentLevel = 0;
        drawView.loadLevel(levels.get(currentLevel));
        drawView.balls.add(ball);
    }

    public void onStart() {
        super.onStart();
        bluetoothNode.registerActivity(this);
        Thread thread = new Thread(bluetoothNode);
        thread.start();
    }


    public void onStop()
    {  super.onStop();
        bluetoothNode.stop();
        bluetoothNode.registerActivity(null);
    }

    protected void onPause() {
        super.onPause();
        mNfcAdapter.disableForegroundDispatch(this);
        try {
            drawView.destroy();
            finish();
        } catch(NullPointerException e) {
            e.printStackTrace();
        }
    }

    public void onResume() {
        super.onResume();
        // Check to see that the Activity started due to an Android Beam
        mNfcAdapter.enableForegroundDispatch(this, nfcPendingIntent, intentFiltersArray, null);
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent().getAction())) {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            restartGame();
            //    processIntent(getIntent());
        }
    }

    public void onRestart() {
        super.onRestart();
        Intent goHome = new Intent(this, MainActivity.class);
        goHome.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(goHome);
    }

    public void onNewIntent(Intent intent) {
        //onResume gets called after this to handle the intent
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        setIntent(intent);
    }

    //Creates a custom MIME type encapsulated in an NDEF record
    public NdefRecord createMimeRecord(String mimeType, byte[] payload) {
        byte[] mimeBytes = mimeType.getBytes(Charset.forName("US-ASCII"));
        NdefRecord mimeRecord = new NdefRecord(NdefRecord.TNF_MIME_MEDIA, mimeBytes, new byte[0], payload);
        return mimeRecord;
    }

    public synchronized void showMessage(String message) {
        if(message.equalsIgnoreCase("Chat server found") || message.contains("Client connected") ){
            status.post(new Runnable()
            {
                public void run()
                {  status.setText("Connection Successful: Press Below to Start");
                }
            });
            startButton.post(new Runnable()
            {
                public void run()
                {  startButton.setEnabled(true);
                }
            });
        }
    }


    public void onClick(View view) {
        if (view == startButton) {
            Ball ball= new Ball(0,0);
            ball.message= "isReady";
            sendBall(ball);
            gameStart = true;

        } else if(view==addButton) {
            if(numExtraBalls>0) {
                Ball ball = new Ball(drawView.posX, (drawView.posY - (2 * drawView.baseHeight)));
                ball.giveViewParameters(drawView.viewWidth, drawView.viewHeight);
                ball.resetBall(drawView.posX, drawView.posY);
                drawView.balls.add(ball);
                numExtraBalls--;
            }
            else {
                addButton.setEnabled(false);
            }
        }
    }

    public void setGameView(){
        setContentView(R.layout.activity_game_multiplayer);
        drawView = (DrawView)findViewById(R.id.ball);
        drawView.loadLevel(levels.get(currentLevel));
        drawView.resume();
        addButton = (Button)findViewById(R.id.ballButton);
        addButton.setOnClickListener(this);
        livesText = (TextView)findViewById(R.id.livestext);
        scoreText = (TextView)findViewById(R.id.scoretext);
    }

    public static void sendBall(Ball ball) {
        if(ball.color== Color.WHITE)
            ball.color = currentColour;
        bluetoothNode.forward(ball);
    }

    public void receiveBall(Ball ball) {
        if(ball.message.equalsIgnoreCase("noLives")){
            hasWon=true;
        }
        else if (ball.message.equalsIgnoreCase("isReady")){
            gameStart=true;
        }
        else {
            Ball newBall = ball;
            newBall.updateParameters(drawView.viewWidth, drawView.viewHeight);
            drawView.balls.add(newBall);
        }
    }

    public void onSensorChanged(SensorEvent event) {
        if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER && gameStart) {
            if(!viewChanged){
                setGameView();
                viewChanged=true;
            }
            float[] accValues = new float[3];
            System.arraycopy(event.values, 0, accValues, 0, 3);
            float x = accValues[0];
            drawView.updateBase(x);

            if (lives > -1 && !hasWon) {
                livesText.setText("Lives: " + lives);
                scoreText.setText("Score: " + score);
            } else if (hasWon) {
                livesText.setText("You won!");
                addButton.setEnabled(false);
                drawView.balls.clear();
            } else if(!hasWon){
                livesText.setText("You lost!");
                addButton.setEnabled(false);
                DrawView.balls.clear();
            }
        }
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {}
}