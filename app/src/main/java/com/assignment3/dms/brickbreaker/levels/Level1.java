package com.assignment3.dms.brickbreaker.levels;

public class Level1 extends LevelBase {
    public Level1() {
        this.lives = 1;
        this.rows = 2;
        this.cols = 5;
        this.brickLayout = new int[cols][rows];
        for(int x = 0; x < cols; x++){
            for(int y = 0; y < rows; y++){
                brickLayout[x][y] = lives;
            }
        }
    }
}
