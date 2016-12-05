package com.assignment3.dms.brickbreaker;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MediaPlayer mediaPlayer = MediaPlayer.create(this, R.raw.main_theme);
        mediaPlayer.setLooping(true);
        mediaPlayer.start();
    }

    public void startSinglePlayer(View v){
        Intent intent= new Intent(this,SinglePlayer.class);
        startActivity(intent);
    }
    public void startMultiplayer(View v){
        Intent intent = new Intent(this,ConnectPlayers.class);
        startActivity(intent);
    }
}
