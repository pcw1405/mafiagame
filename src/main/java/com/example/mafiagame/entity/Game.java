package com.example.mafiagame.entity;

public interface Game {
    Long getId();
    void setPlayer1(String player1);
    void setPlayer2(String player2);
    String getPlayer1();
    String getPlayer2();
}
