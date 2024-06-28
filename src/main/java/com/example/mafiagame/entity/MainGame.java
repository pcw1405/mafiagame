package com.example.mafiagame.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;

@Entity
@Getter
@Table(name="maingame")
@Setter
@ToString
@NoArgsConstructor
public class MainGame implements Game {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "game_id")
    private Long id;

    private String player1;
    private String player2;
    private String winner;
    private String loser;
    private long draw;
    private int player1Wins = 0;
    private int player2Wins = 0;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setPlayer1(String player1) {
        this.player1 = player1;
    }

    @Override
    public void setPlayer2(String player2) {
        this.player2 = player2;
    }



    @Override
    public String getPlayer1() {
        return player1;
    }

    @Override
    public String getPlayer2() {
        return player2;
    }

    public int getPlayer1Wins() {
        return player1Wins;
    }

    public void setPlayer1Wins(int player1Wins) {
        this.player1Wins = player1Wins;
    }

    public int getPlayer2Wins() {
        return player2Wins;
    }

    public void setPlayer2Wins(int player2Wins) {
        this.player2Wins = player2Wins;
    }

    @Override
    public void setWinner(String winner) {
        this.winner = winner;
    }

    @Override
    public void setLoser(String loser) {
        this.loser = loser;
    }
    // Getters and setters for the fields
}
