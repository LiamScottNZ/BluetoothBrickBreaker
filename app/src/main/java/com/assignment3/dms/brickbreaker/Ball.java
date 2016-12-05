package com.assignment3.dms.brickbreaker;

import android.graphics.Color;
import android.graphics.RectF;
import java.io.Serializable;

public class Ball implements Serializable {

    public float x, y, velX, velY, radius;
    public int viewWidth, viewHeight;
    private float baseWidth, baseHeight;
    private boolean ignoreBounce;
    public int color;
    public String message;
    static final long serialVersionUID =696969696L;

    public Ball(float x, float y) {
        this.x = x;
        this.y = y;
        color = Color.WHITE;
        ignoreBounce = false;
        message="";
    }

    public void updateParameters(int viewWidth, int viewHeight) {
        this.viewWidth = viewWidth;
        this.viewHeight = viewHeight;
        radius = Math.min(viewWidth, viewHeight)/20.0f;
        velY *= -1;
    }

    public void giveViewParameters(int viewWidth, int viewHeight) {
        this.viewWidth = viewWidth;
        this.viewHeight = viewHeight;
        velX = viewWidth * 0.01f;
        velY = viewHeight * 0.009f;
        velY = velY * -1;
        radius = Math.min(viewWidth, viewHeight)/20.0f;
        baseWidth = 150;
        baseHeight = 50;
    }

    //Allows the ball to bounce off the side of the base, causes issues on some devices
    public void updateBallThea(float baseX, float baseY){
        x += velX;
        y += velY;
        RectF baseRect= new RectF(baseX-baseWidth,baseY-baseHeight, baseX+baseWidth,baseY+baseHeight);
        RectF ballRect= new RectF(x-radius,y-radius,x+radius,y+radius);
        if (x < radius) {
            x = 2.0f * radius - x;
            velX *= -1.0f;
        } else if (x + radius > viewWidth) {
            x = 2.0f * (viewWidth - radius) - x;
            velX *= -1.0f;
        }
        if (y < radius && velY<0) {
            if(DrawView.isMultiplayer) {
                Ball ball = this;
                DrawView.balls.remove(ball);
                MultiPlayer.sendBall(ball);
            } else {
                y = 2.0f * radius - y;
                velY *= -1.0f;
            }
        } else if (y>viewHeight) {
            if(DrawView.isMultiplayer) {
                Ball ball = this;
                MultiPlayer.lives--;

                if (MultiPlayer.lives==-1){
                    this.message="noLives";
                    MultiPlayer.sendBall(this);
                }
                DrawView.balls.remove(ball);
            }
            else{
                resetBall(baseX, baseY);
            }

        } else if (RectF.intersects(baseRect,ballRect)) {
            bounceBall(baseX);
        }
    }

    //Doesn't allow ball to bounce off the side of the base, causes issues on some devices
    public void updateBallLiam(float baseX, float baseY){
        RectF baseRect = new RectF(baseX-baseWidth, baseY-baseHeight, baseX+baseWidth, baseY+baseHeight);
        RectF ballRect = new RectF(x-radius, y-radius, x+radius, y+radius);
        x += velX;
        y += velY;
        if (x < radius) {
            x = 2.0f * radius - x;
            velX *= -1.0f;
        } else if (x + radius > viewWidth) {
            x = 2.0f * (viewWidth - radius) - x;
            velX *= -1.0f;
        }
        if (y < radius && velY<0) {
            if(DrawView.isMultiplayer) {
                Ball ball = this;
                DrawView.balls.remove(ball);
                MultiPlayer.sendBall(ball);
            } else {
                y = 2.0f * radius - y;
                velY *= -1.0f;
            }
        } else if (y>viewHeight) {
            if(DrawView.isMultiplayer) {
                Ball ball = this;
                MultiPlayer.lives--;
                if (MultiPlayer.lives==-1){
                    this.message="noLives";
                    MultiPlayer.sendBall(this);
                }
                DrawView.balls.remove(ball);
            }
            else{
                SinglePlayer.lives--;
                resetBall(baseX, baseY);
            }

        } else if (y+radius > (baseY-baseHeight)) {
            if(x>baseX-baseWidth && x<baseX+baseWidth && !ignoreBounce) {
                bounceBall(baseX);
            } else {
                ignoreBounce = true;
            }
        }
    }

    public void bounceBall(float baseX) {
        float difference = 0;
        if(baseX<x)
            difference = x-baseX;
        else if(baseX>x)
            difference = baseX-x;
        velY *= -1f;

        if(difference<25)
            changeXVel(0.0025f, baseX);
        else if(difference<50) {
            changeXVel(0.005f, baseX);
        } else if(difference<100) {
            changeXVel(0.0075f, baseX);
        } else if(difference<130){
            changeXVel(0.01f, baseX);
        } else {
            changeXVel(0.0125f, baseX);
        }
    }

    public void changeXVel(float dif, float baseX) {
        velX = viewWidth * dif;
        if(baseX<x) {
            if(velX<0)
                velX *= -1;
        } else {
            if(velX>0)
                velX *= -1;
        }
    }

    public void resetBall(float baseX, float baseY) {
        x = baseX;
        y = baseY - (2 * baseHeight);
        velX = 0;
        velY = viewHeight * 0.009f;
        velY *= -1f;
        radius = Math.min(viewWidth, viewHeight) / 20.0f;
        ignoreBounce = false;
    }

    public String toString() {
        super.toString();
        return "(x,y) = "+x+","+y+" (velX,velY) = "+velX+","+velY;
    }
}
