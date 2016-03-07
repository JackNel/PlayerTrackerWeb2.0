package com.theironyard;

/**
 * Created by Jack on 3/1/16.
 */
public class Player {
    int id;
    int user_id;
    String name;
    String team;
    String position;
    String creator;
    boolean isCreator;


    public Player() {
    }

    public Player(int id, int user_id, String name, String team, String position, String creator, boolean isCreator) {
        this.id = id;
        this.user_id = user_id;
        this.name = name;
        this.team = team;
        this.position = position;
        this.creator = creator;
        this.isCreator = isCreator;
    }
}

