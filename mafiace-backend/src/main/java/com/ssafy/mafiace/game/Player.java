package com.ssafy.mafiace.game;

import com.ssafy.mafiace.db.entity.User;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Player {

    private User user;
    private String nickname;
    private String role;
    private boolean isAlive;
    private int saveCount ;
    private int killCount;
    private int investigateCount;

    public Player(User user){
        this.user = user;
        this.nickname = user.getNickname();
        this.isAlive = true;
        this.saveCount = 0;
        this.killCount = 0;
        this.investigateCount = 0;
    }

    public void setDead(){
        this.isAlive = false;
    }

    public User getUser(){
        return this.user;
    }

    public void addSaveCount(){
        this.saveCount ++;
    }

    public void addInvestigateCount(){
        this.investigateCount ++;
    }

    public void addKillCount(){
        this.killCount ++;
    }
}
