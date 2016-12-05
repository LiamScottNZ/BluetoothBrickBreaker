package com.assignment3.dms.brickbreaker;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.shapes.OvalShape;
import android.util.AttributeSet;
import android.view.View;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.os.Handler;
import com.assignment3.dms.brickbreaker.levels.LevelBase;

public class DrawView extends View {
    public float posX, posY;
    public int baseWidth, baseHeight;
    public int viewWidth, viewHeight;
    private ShapeDrawable baseDrawable;
    public static ShapeDrawable ballDrawable;
    private Timer timer;
    private Handler myHandler = new Handler();
    public static List<Ball> balls;
    public static boolean isMultiplayer;
    private Context context;
    public static Brick[][] bricks;
    public static ShapeDrawable[][] brickDrawables;
    private static final int EDGE_BUFFER_SIZE = 10, HEIGHT_BUFFER_SIZE = 200;
    public static LevelBase level;

    private Runnable uiRunnable = new Runnable() {
        @Override
        public void run() {
            DrawView.this.updateBall();
        }
    };

    private TimerTask task = new TimerTask() {
        @Override
        public void run() {
            myHandler.post(uiRunnable);
        }
    };

    public DrawView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init();
        timer = new Timer();
    }

    public DrawView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.context = context;
        init();
    }

    public static void updateLevel(){
        for(int x = 0; x < level.getCols(); x++){
            for (int y = 0; y < level.getRows(); y++){
                ShapeDrawable s = brickDrawables[x][y];
                s.getPaint().setColor(bricks[x][y].getColour());
            }
        }
    }

    public boolean loadLevel(LevelBase level) {
        this.level = level;
        bricks = new Brick[level.getCols()][level.getRows()];
        int USABLE_HEIGHT = 0;
        int blockWidth = (viewWidth / level.getCols()) - 10;
        int blockHeight = ((USABLE_HEIGHT - ((2 * EDGE_BUFFER_SIZE) + (level.getRows() * HEIGHT_BUFFER_SIZE))) / (level.getRows())); //usable space / num of bricks
        blockWidth = (viewWidth / level.getCols()) - (EDGE_BUFFER_SIZE * 2);
        blockHeight = ((viewHeight / 3) / (level.getRows()));
        int[][] brickLayout = level.getBrickLayout();
        brickDrawables = new ShapeDrawable[level.getCols()][level.getRows()];
        for (int x = 0; x < level.getCols(); x++) {
            for (int y = 0; y < level.getRows(); y++) {
                Brick b = new Brick((x * EDGE_BUFFER_SIZE) + (x * blockWidth) + EDGE_BUFFER_SIZE,
                        (y * (EDGE_BUFFER_SIZE + blockHeight)),
                        blockWidth, blockHeight,
                        brickLayout[x][y]);
                ShapeDrawable s = new ShapeDrawable(new RectShape());
                s.setBounds(EDGE_BUFFER_SIZE + EDGE_BUFFER_SIZE + b.getPosX(), b.getPosY()+HEIGHT_BUFFER_SIZE, EDGE_BUFFER_SIZE + EDGE_BUFFER_SIZE + b.getPosX() + b.getWidth(), HEIGHT_BUFFER_SIZE+b.getPosY() + b.getHeight());
                s.getPaint().setColor(b.getColour());
                bricks[x][y] = b;
                brickDrawables[x][y] = s;
            }
        }
        return true;
    }

    private void init() {
        baseWidth = 150;
        baseHeight = 50;
        baseDrawable = new ShapeDrawable(new RectShape());
        baseDrawable.getPaint().setColor(Color.LTGRAY);
        ballDrawable = new ShapeDrawable(new OvalShape());
        ballDrawable.getPaint().setColor(MultiPlayer.currentColour);
        balls = Collections.synchronizedList(new ArrayList<Ball>());
    }

    protected void onSizeChanged(int w, int h, int oldW, int oldH) {
        viewWidth = w;
        viewHeight = h;
        posX = viewWidth * 0.5f;
        posY = viewHeight * 0.9f;
        Ball ball = new Ball(posX,(posY-(2*baseHeight)));
        ball.giveViewParameters(viewWidth, viewHeight);
        ball.resetBall(posX, posY);
        balls.add(ball);
        loadLevel(level);
    }

    //Updates position for each ball
    public void updateBall() {
        testCollision();
        for(int i=0; i<balls.size(); i++) {
            Ball current = balls.get(i);
            current.updateBallLiam(posX, posY);
        }
        invalidate();
    }

    //Update the base position based off accelerometer value
    public void updateBase(float xVal) {
        if (posX < baseWidth) {
            posX = baseWidth+0.1f;
        }
        else if (posX+baseWidth > viewWidth) {
            posX = (viewWidth-baseWidth)-0.1f;
        } else {
            posX -= xVal*5;
        }
    }

    private void testCollision() {
        for(int i=0; i<DrawView.balls.size(); i++) {
             Ball b = DrawView.balls.get(i);
             Rect ballBoundsX;
             Rect ballBoundsY;
             int ballX = (int)b.x;
             int ballY = (int)b.y;
             int left = (int)(b.x-b.radius);
             int right = (int)(b.x+b.radius);
             int top = (int)(b.y-b.radius);
             int bottom = (int)(b.y+b.radius);
             if(b.velX>0) {
                 ballBoundsX = new Rect(right, ballY, right, ballY);
             } else {
                 ballBoundsX = new Rect(left, ballY, left, ballY);
             }
             if(b.velY<0) {
                 ballBoundsY = new Rect(ballX, top, ballX, top);
             } else {
                 ballBoundsY = new Rect(ballX, bottom, ballX, bottom);
             }

             for(int x = 0; x < DrawView.level.getCols(); x++){
                 for(int y = 0; y < DrawView.level.getRows(); y++){
                     Brick brick = DrawView.bricks[x][y];
                     if(DrawView.brickDrawables[x][y].copyBounds().intersect(ballBoundsX) && brick.getLives()>0){
                         if(brick.hit()){
                             b.velX *= -1;
                             increaseScore();
                             DrawView.updateLevel();
                             return;
                         }
                     } else if(DrawView.brickDrawables[x][y].copyBounds().intersect(ballBoundsY) && brick.getLives()>0){
                         if(brick.hit()){
                         b.velY *= -1;
                             increaseScore();
                         DrawView.updateLevel();
                         return;
                         }
                     }
                 }
             }
        }
    }

    public void increaseScore() {
        if(isMultiplayer)
            MultiPlayer.score++;
        else
            SinglePlayer.score++;
    }

    //Draws the base and the balls on the canvas with ShapeDrawable objects
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for(int i=0; i<balls.size(); i++) {
            Ball b = balls.get(i);
            ballDrawable.setBounds((int)(b.x - b.radius),(int)(b.y - b.radius),(int)(b.x + b.radius),(int)(b.y + b.radius));
            ballDrawable.getPaint().setColor(b.color);
            ballDrawable.draw(canvas);
        }
        baseDrawable.setBounds((int)posX-baseWidth, (int)posY-baseHeight, (int)posX+baseWidth, (int)posY+baseHeight);
        baseDrawable.draw(canvas);

        for(int x = 0; x < level.getCols(); x++) {
            for (int y = 0; y < level.getRows(); y++) {
                brickDrawables[x][y].draw(canvas);
            }
        }
    }

    public void resume() {
        SharedPreferences sharedpreferences = context.getSharedPreferences("SharedPreferences", Context.MODE_PRIVATE);
        String mode = sharedpreferences.getString("GameMode", "Error");
        if(mode.equals("Multi"))
            isMultiplayer = true;
        else if(mode.equals("Single"))
            isMultiplayer = false;
        timer.scheduleAtFixedRate(task,500,20);
    }

    public void pause() {
        timer.cancel();
    }

    public void destroy() {
        timer.cancel();
    }
}
