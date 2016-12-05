package com.assignment3.dms.brickbreaker.levels;

/**
 * Created by Liam on 2/06/2016.
 */
public class Level4 extends LevelBase {
    public Level4() {
        this.lives = 3;
        this.rows = 4;
        this.cols = 5;
        this.brickLayout = new int[cols][rows];
        for(int x = 0; x < cols; x++){
            for(int y = 0; y < rows; y++){
                brickLayout[x][y] = lives;
            }
        }
    }
}
