package com.assignment3.dms.brickbreaker.levels;

/**
 * Created by Liam on 2/06/2016.
 */
public class Level5 extends LevelBase {
    public Level5() {
        this.lives = 4;
        this.rows = 3;
        this.cols = 5;
        this.brickLayout = new int[cols][rows];
        for(int x = 0; x < cols; x++){
            for(int y = 0; y < rows; y++){
                brickLayout[x][y] = lives;
            }
        }
    }
}
