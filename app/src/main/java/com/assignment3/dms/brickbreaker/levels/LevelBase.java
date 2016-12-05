package com.assignment3.dms.brickbreaker.levels;

import com.assignment3.dms.brickbreaker.Brick;


/**
 * Created by Nick on 5/26/2016.
 *
 * Holds all the information needed to setup a level
 */
public class LevelBase {
    public static final String versionTag = "pre-alpha-1";  //Version tag should be changed each time this file changes

    private String levelVersionTag = versionTag; //need to have this in json
    protected int rows, cols;
    protected int lives;
    protected int[][] brickLayout;

    public String getLevelVersionTag() {
        return levelVersionTag;
    }

    public int getRows() {
        return rows;
    }

    public int getCols() {
        return cols;
    }

    public int getLives() {
        return lives;
    }

    public int[][] getBrickLayout() {
        return brickLayout;
    }
}


