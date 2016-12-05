package com.assignment3.dms.brickbreaker;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import com.assignment3.dms.brickbreaker.levels.Level1;

public class SinglePlayer extends Activity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private DrawView drawView;
    private TextView livesText, scoreText;
    public static int lives, score;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_singleplayer);
        //Set the game mode to single player in the shared preferences
        SharedPreferences sharedpreferences = getSharedPreferences("SharedPreferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putString("GameMode", "Single");
        editor.commit();
        lives = 10;
        score = 0;
        drawView = (DrawView)findViewById(R.id.ball);
        livesText = (TextView)findViewById(R.id.livestext);
        scoreText = (TextView)findViewById(R.id.scoretext);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        drawView.loadLevel(new Level1());
    }

    public void onSensorChanged(SensorEvent event) {
        if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float[] accValues = new float[3];
            System.arraycopy(event.values, 0, accValues, 0, 3);
            float x = accValues[0];
            drawView.updateBase(x);
            if(lives<0) {
                livesText.setText("No More Lives!");
                drawView.balls.clear();
            } else {
                livesText.setText("Lives: "+lives);
            }
            scoreText.setText("Score: "+score);
        }
    }

    public void restartGame(View view) {
        lives = 10;
        score = 0;
        Ball ball = new Ball(drawView.posX, (drawView.posY - (2 * drawView.baseHeight)));
        ball.giveViewParameters(drawView.viewWidth, drawView.viewHeight);
        ball.resetBall(drawView.posX, drawView.posY);
        drawView.loadLevel(new Level1());
        drawView.balls.clear();
        drawView.balls.add(ball);
    }

    public void onAccuracyChanged(Sensor sensor, int x) {
    }

    public void onResume() {
        super.onResume();
        try {
            drawView.resume();
        } catch(IllegalStateException e) {
            finish();
        }
    }

    public void onPause() {
        super.onPause();
        drawView.pause();
    }

    public void onDestroy() {
        super.onDestroy();
        drawView.destroy();
    }
}
