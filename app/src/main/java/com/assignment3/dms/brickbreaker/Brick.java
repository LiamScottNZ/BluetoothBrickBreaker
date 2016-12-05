package com.assignment3.dms.brickbreaker;

import android.graphics.Color;
import static android.graphics.Color.rgb;

public class Brick {

    private int posX, posY, width, height, lives;
    private long time;

    public Brick(int posX, int posY, int width, int height, int lives){
        this.posX = posX;
        this.posY = posY;
        this.width = width;
        this.height = height;
        this.lives = lives;
    }

    public int getColour(){
        switch (lives){
            case 0:
                return Color.TRANSPARENT;
            case 1:
                return 	rgb(0,0,128);
            case 2:
                return	rgb(75,0,130);
            case 3:
                return	rgb(123,104,238);
            case 4:
                return	rgb(0,191,255);
        }
        return  rgb(0,0,0);
    }

    public boolean hit(){
        if(lives > 0  && (time + 35) < System.currentTimeMillis()) {
            this.lives--;
            time = System.currentTimeMillis();
            return true;
        } else {
            return false;
        }
    }

    public int getLives() {
        return lives;
    }

    public void setLives(int lives) {
        this.lives = lives;
    }

    public int getPosX() {
        return posX;
    }

    public void setPosX(int posX) {
        this.posX = posX;
    }

    public int getPosY() {
        return posY;
    }

    public void setPosY(int posY) {
        this.posY = posY;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }
}
