package com.assignment3.dms.brickbreaker.levels;

import com.google.gson.Gson;

/**
 * Created by Nick on 5/26/2016.
 */
public class LevelFactory {

    /**
     * Allows us to build levels from JSON
     * @param JSON level as json
     * @return
     */
    public LevelBase fromGSON(String JSON){
        return (new Gson()).fromJson(JSON, LevelBase.class);
    }

    public String toJSON(LevelBase level){
        return (new Gson()).toJson(level);
    }
}
